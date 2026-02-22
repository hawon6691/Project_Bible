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
}

interface CircuitBreakerState {
  status: CircuitStatus;
  failureCount: number;
  successCount: number;
  nextAttemptAt: Date | null;
  lastFailureReason: string | null;
}

@Injectable()
export class ResilienceService {
  private readonly states = new Map<string, CircuitBreakerState>();

  async execute<T>(name: string, action: () => Promise<T>, options: CircuitBreakerOptions): Promise<T> {
    const state = this.getOrCreateState(name);
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
      this.onSuccess(state, options);
      return result;
    } catch (error) {
      this.onFailure(state, error, options);
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
    };
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
