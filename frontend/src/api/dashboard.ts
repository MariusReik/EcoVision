import { apiRequest } from './client'
import type { DashboardSummary } from './types'

export function getDashboardSummary(params: { from?: string; to?: string } = {}): Promise<DashboardSummary> {
  return apiRequest<DashboardSummary>('/api/dashboard/summary', { query: params })
}
