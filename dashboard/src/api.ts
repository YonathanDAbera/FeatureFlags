export type Environment = 'development' | 'staging' | 'production'

export type FeatureFlag = {
  environment: Environment
  key: string
  enabled: boolean
  rolloutPercentage: number
}

export type Evaluation = {
  environment: Environment
  flagKey: string
  userId: string
  enabled: boolean
  rolloutPercentage: number
  reason: string
  bucket: number | null
  matchedRuleId: number | null
}

export type TargetingRule = {
  id: number
  environment: Environment
  flagKey: string
  userId: string
  priority: number
}

export type AuditEvent = {
  id: number
  environment: Environment
  flagKey: string
  action: string
  actor: string
  occurredAt: string
  previousState: { enabled: boolean; rolloutPercentage: number } | null
  newState: { enabled: boolean; rolloutPercentage: number }
  details: string | null
}

type RequestOptions = RequestInit & { actor?: string }

export async function request<T>(path: string, token: string, options: RequestOptions = {}) {
  const response = await fetch(`/api/v1${path}`, {
    ...options,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(options.actor ? { 'X-Actor': options.actor } : {}),
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const detail = await response.text()
    throw new Error(detail || `Request failed (${response.status})`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export const flagApi = {
  list: (environment: Environment, token: string) =>
    request<FeatureFlag[]>(`/environments/${environment}/flags`, token),
  audit: (environment: Environment, flagKey: string, token: string) =>
    request<AuditEvent[]>(`/environments/${environment}/flags/${flagKey}/audit-events`, token),
  evaluate: (environment: Environment, flagKey: string, userId: string, token: string) =>
    request<Evaluation>(`/environments/${environment}/flags/${flagKey}/evaluate?userId=${encodeURIComponent(userId)}`, token),
  update: (environment: Environment, flagKey: string, body: Pick<FeatureFlag, 'enabled' | 'rolloutPercentage'>, token: string, actor: string) =>
    request<FeatureFlag>(`/environments/${environment}/flags/${flagKey}`, token, {
      method: 'PATCH', body: JSON.stringify(body), actor,
    }),
  create: (environment: Environment, body: Omit<FeatureFlag, 'environment'>, token: string, actor: string) =>
    request<FeatureFlag>(`/environments/${environment}/flags`, token, {
      method: 'POST', body: JSON.stringify(body), actor,
    }),
  rules: (environment: Environment, flagKey: string, token: string) =>
    request<TargetingRule[]>(`/environments/${environment}/flags/${flagKey}/targeting-rules`, token),
  addRule: (environment: Environment, flagKey: string, body: Pick<TargetingRule, 'userId' | 'priority'>, token: string, actor: string) =>
    request<TargetingRule>(`/environments/${environment}/flags/${flagKey}/targeting-rules`, token, {
      method: 'POST', body: JSON.stringify(body), actor,
    }),
  removeRule: (environment: Environment, flagKey: string, ruleId: number, token: string, actor: string) =>
    request<void>(`/environments/${environment}/flags/${flagKey}/targeting-rules/${ruleId}`, token, { method: 'DELETE', actor }),
}
