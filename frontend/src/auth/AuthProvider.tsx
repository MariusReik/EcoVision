import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { getToken, setToken as persistToken } from '../api/client'
import { getMe } from '../api/user'
import type { User } from '../api/types'
import { AuthContext, type AuthContextValue } from './AuthContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [hasToken, setHasToken] = useState<boolean>(() => getToken() !== null)
  const [isLoading, setIsLoading] = useState<boolean>(() => getToken() !== null)

  const signOut = useCallback(() => {
    persistToken(null)
    setHasToken(false)
    setUser(null)
  }, [])

  // A token surviving a page refresh has no user object attached to it - fetch one
  // before treating the session as usable, and drop a token the server no longer honors.
  useEffect(() => {
    if (!hasToken || user) {
      return
    }
    getMe()
      .then(setUser)
      .catch(() => signOut())
      .finally(() => setIsLoading(false))
  }, [hasToken, user, signOut])

  const signIn = useCallback((token: string, nextUser: User) => {
    persistToken(token)
    setHasToken(true)
    setUser(nextUser)
    setIsLoading(false)
  }, [])

  const updateUser = useCallback((nextUser: User) => {
    setUser(nextUser)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ user, isAuthenticated: hasToken, isLoading, signIn, signOut, updateUser }),
    [user, hasToken, isLoading, signIn, signOut, updateUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
