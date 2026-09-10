import { createContext } from 'react'
import type { User } from '../api/types'

export interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  /** True only while a stored token is being resolved to a user on first load. */
  isLoading: boolean
  signIn: (token: string, user: User) => void
  signOut: () => void
  updateUser: (user: User) => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
