import { apiRequest } from './client'
import type { PatchMeRequest, User } from './types'

export function getMe(): Promise<User> {
  return apiRequest<User>('/api/me')
}

export function patchMe(request: PatchMeRequest): Promise<User> {
  return apiRequest<User>('/api/me', { method: 'PATCH', body: request })
}
