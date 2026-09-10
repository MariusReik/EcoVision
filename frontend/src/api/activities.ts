import { apiRequest } from './client'
import type { Activity, ActivityPage, ActivityType, CreateActivityRequest } from './types'

export function getActivityTypes(): Promise<ActivityType[]> {
  return apiRequest<ActivityType[]>('/api/activity-types')
}

export function createActivity(request: CreateActivityRequest): Promise<Activity> {
  return apiRequest<Activity>('/api/activities', { method: 'POST', body: request })
}

export function listActivities(params: { from?: string; to?: string; cursor?: string } = {}): Promise<ActivityPage> {
  return apiRequest<ActivityPage>('/api/activities', { query: { ...params, limit: 20 } })
}

export function deleteActivity(id: string): Promise<void> {
  return apiRequest<void>(`/api/activities/${id}`, { method: 'DELETE' })
}
