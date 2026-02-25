export type BackendTarget = 'nest' | 'spring' | 'express' | 'django' | 'ktor';

const BACKEND_TARGETS: BackendTarget[] = ['nest', 'spring', 'express', 'django', 'ktor'];

const DEFAULT_TARGET: BackendTarget = 'nest';
const DEFAULT_BASE_PATH = '/api/v1';

function normalizeTarget(raw?: string): BackendTarget {
  if (!raw) return DEFAULT_TARGET;
  const candidate = raw.toLowerCase() as BackendTarget;
  return BACKEND_TARGETS.includes(candidate) ? candidate : DEFAULT_TARGET;
}

export function getBackendTarget(): BackendTarget {
  return normalizeTarget(process.env.NEXT_PUBLIC_BACKEND_TARGET);
}

function fromTargetEnv(target: BackendTarget): string | undefined {
  const byTarget: Record<BackendTarget, string | undefined> = {
    nest: process.env.NEXT_PUBLIC_API_URL_NEST,
    spring: process.env.NEXT_PUBLIC_API_URL_SPRING,
    express: process.env.NEXT_PUBLIC_API_URL_EXPRESS,
    django: process.env.NEXT_PUBLIC_API_URL_DJANGO,
    ktor: process.env.NEXT_PUBLIC_API_URL_KTOR,
  };
  return byTarget[target];
}

export function resolveApiBaseUrl(): string {
  const target = getBackendTarget();
  return fromTargetEnv(target) || process.env.NEXT_PUBLIC_API_URL || DEFAULT_BASE_PATH;
}
