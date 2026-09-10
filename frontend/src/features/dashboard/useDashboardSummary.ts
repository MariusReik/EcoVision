import { useQuery } from '@tanstack/react-query'
import { getDashboardSummary } from '../../api/dashboard'

export function useDashboardSummary(params: { from?: string; to?: string } = {}) {
  return useQuery({
    queryKey: ['dashboardSummary', params],
    queryFn: () => getDashboardSummary(params),
  })
}
