import { apiRequest } from './client'
import type { AuthResponse, LoginRequest, RegisterRequest } from './types'

export function register(request: RegisterRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>('/api/auth/register', { method: 'POST', body: request, auth: false })
}

export function login(request: LoginRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>('/api/auth/login', { method: 'POST', body: request, auth: false })
}
