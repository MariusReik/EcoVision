import { useQuery } from '@tanstack/react-query'
import { getActivityTypes } from '../../api/activities'

export function useActivityTypes() {
  return useQuery({ queryKey: ['activityTypes'], queryFn: getActivityTypes })
}
