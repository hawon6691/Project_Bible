import { Injectable } from '@nestjs/common';

export interface CircuitBreakerOptions {
  failureThreshold: number;
  openTimeoutMs: number;
  halfOpenSuccessThreshold: number;
}

export type CircuitStatus = 'CLOSED' | 'OPEN' | 'HALF_OPEN';

export interface CircuitBreakerSnapshot {
  name: string;
  status: CircuitStatus;
  failureCount: number;
  successCount: number;
  nextAttemptAt: Date | null;
  lastFailureReason: string | null;
  options: CircuitBreakerOptions;
}

interface CircuitBreakerState {
  status: CircuitStatus;
  failureCount: number;
  successCount: number;
  nextAttemptAt: Date | null;
  lastFailureReason: string | null;
}

export interface CircuitHealthStat {
  success: number;
  failure: number;
  lastTunedAt: number;
}

@Injectable()
export class ResilienceService {
  private readonly states = new Map<string, CircuitBreakerState>();
  private readonly adaptiveOptions = new Map<string, CircuitBreakerOptions>();
  private readonly healthStats = new Map<string, CircuitHealthStat>();
  private readonly autoTuneEnabled = (process.env.RESILIENCE_AUTO_TUNE_ENABLED ?? 'true') === 'true';
  private readonly autoTuneMinSamples = this.readInt(process.env.RESILIENCE_AUTO_TUNE_MIN_SAMPLES, 20);
  private readonly autoTuneCooldownMs = this.readInt(process.env.RESILIENCE_AUTO_TUNE_COOLDOWN_MS, 60_000);

  async execute<T>(name: string, action: () => Promise<T>, options: CircuitBreakerOptions): Promise<T> {
    const state = this.getOrCreateState(name);
    const effectiveOptions = this.getEffectiveOptions(name, options);
    const now = new Date();

    if (state.status === 'OPEN') {
      if (state.nextAttemptAt && now < state.nextAttemptAt) {
        throw new Error('CIRCUIT_OPEN');
      }
      state.status = 'HALF_OPEN';
      state.successCount = 0;
    }

    try {
      const result = await action();
      this.onSuccess(state, effectiveOptions);
      this.recordOutcome(name, true, effectiveOptions);
      return result;
    } catch (error) {
      this.onFailure(state, error, effectiveOptions);
      this.recordOutcome(name, false, effectiveOptions);
      throw error;
    }
  }

  getSnapshots() {
    return [...this.states.entries()].map(([name, state]) => ({
      name,
      status: state.status,
      failureCount: state.failureCount,
      successCount: state.successCount,
      nextAttemptAt: state.nextAttemptAt,
      lastFailureReason: state.lastFailureReason,
      options: this.getEffectiveOptions(name, this.getDefaultOptions()),
    }));
  }

  getSnapshot(name: string): CircuitBreakerSnapshot {
    const state = this.getOrCreateState(name);
    return {
      name,
      status: state.status,
      failureCount: state.failureCount,
      successCount: state.successCount,
      nextAttemptAt: state.nextAttemptAt,
      lastFailureReason: state.lastFailureReason,
      options: this.getEffectiveOptions(name, this.getDefaultOptions()),
    };
  }

  getAdaptivePolicies() {
    return [...this.adaptiveOptions.entries()].map(([name, options]) => ({
      name,
      options,
      stats: this.healthStats.get(name) ?? { success: 0, failure: 0, lastTunedAt: 0 },
    }));
  }

  reset(name: string) {
    this.states.set(name, {
      status: 'CLOSED',
      failureCount: 0,
      successCount: 0,
      nextAttemptAt: null,
      lastFailureReason: null,
    });
    return this.getSnapshot(name);
  }

  private recordOutcome(name: string, isSuccess: boolean, options: CircuitBreakerOptions) {
    const stat = this.healthStats.get(name) ?? { success: 0, failure: 0, lastTunedAt: 0 };
    if (isSuccess) {
      stat.success += 1;
    } else {
      stat.failure += 1;
    }

    // 장기 실행 시 통계가 과도하게 커지지 않도록 완만하게 감쇠시킨다.
    if (stat.success + stat.failure > 500) {
      stat.success = Math.floor(stat.success * 0.5);
      stat.failure = Math.floor(stat.failure * 0.5);
    }

    this.healthStats.set(name, stat);
    this.tuneOptions(name, stat, options);
  }

  private tuneOptions(name: string, stat: CircuitHealthStat, options: CircuitBreakerOptions) {
    if (!this.autoTuneEnabled) {
      return;
    }

    const total = stat.success + stat.failure;
    if (total < this.autoTuneMinSamples) {
      return;
    }

    const now = Date.now();
    if (now - stat.lastTunedAt < this.autoTuneCooldownMs) {
      return;
    }

    const failureRate = stat.failure / total;
    const next = { ...options };

    if (failureRate >= 0.4) {
      next.failureThreshold = Math.max(2, options.failureThreshold - 1);
      next.openTimeoutMs = Math.min(120_000, Math.round(options.openTimeoutMs * 1.5));
    } else if (failureRate <= 0.1) {
      next.failureThreshold = Math.min(12, options.failureThreshold + 1);
      next.openTimeoutMs = Math.max(2_000, Math.round(options.openTimeoutMs * 0.8));
    } else {
      return;
    }

    this.adaptiveOptions.set(name, next);
    stat.lastTunedAt = now;
  }

  private getEffectiveOptions(name: string, fallback: CircuitBreakerOptions): CircuitBreakerOptions {
    return this.adaptiveOptions.get(name) ?? fallback;
  }

  private getDefaultOptions(): CircuitBreakerOptions {
    return {
      failureThreshold: 3,
      openTimeoutMs: 5_000,
      halfOpenSuccessThreshold: 2,
    };
  }

  private readInt(raw: string | undefined, fallback: number) {
    const parsed = Number(raw);
    if (!Number.isFinite(parsed)) {
      return fallback;
    }
    return Math.trunc(parsed);
  }

  private onSuccess(state: CircuitBreakerState, options: CircuitBreakerOptions) {
    if (state.status === 'HALF_OPEN') {
      state.successCount += 1;
      if (state.successCount >= options.halfOpenSuccessThreshold) {
        state.status = 'CLOSED';
        state.failureCount = 0;
        state.successCount = 0;
        state.nextAttemptAt = null;
        state.lastFailureReason = null;
      }
      return;
    }

    state.failureCount = 0;
    state.lastFailureReason = null;
  }

  private onFailure(state: CircuitBreakerState, error: unknown, options: CircuitBreakerOptions) {
    state.failureCount += 1;
    state.successCount = 0;
    state.lastFailureReason = error instanceof Error ? error.message : 'Unknown error';

    if (state.failureCount >= options.failureThreshold) {
      state.status = 'OPEN';
      state.nextAttemptAt = new Date(Date.now() + options.openTimeoutMs);
    } else if (state.status === 'HALF_OPEN') {
      state.status = 'OPEN';
      state.nextAttemptAt = new Date(Date.now() + options.openTimeoutMs);
    }
  }

  private getOrCreateState(name: string): CircuitBreakerState {
    let state = this.states.get(name);
    if (!state) {
      state = {
        status: 'CLOSED',
        failureCount: 0,
        successCount: 0,
        nextAttemptAt: null,
        lastFailureReason: null,
      };
      this.states.set(name, state);
    }

    return state;
  }
}
