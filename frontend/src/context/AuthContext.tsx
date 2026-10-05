import React, { createContext, useState, useEffect, useCallback } from 'react'
import type { UserResponse } from '../types'
import { getMe } from '../api/auth'
import { getToken, setToken, removeToken } from '../utils/auth'

interface AuthContextValue {
  user: UserResponse | null
  token: string | null
  loading: boolean
  login: (token: string, user: UserResponse) => void
  logout: () => void
  refreshUser: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue>({
  user: null,
  token: null,
  loading: true,
  login: () => {},
  logout: () => {},
  refreshUser: async () => {},
})

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [token, setTokenState] = useState<string | null>(getToken())
  const [loading, setLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    try {
      const me = await getMe()
      setUser(me)
    } catch {
      setUser(null)
      removeToken()
      setTokenState(null)
    }
  }, [])

  useEffect(() => {
    const saved = getToken()
    if (saved) {
      refreshUser().finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [refreshUser])

  function login(accessToken: string, userData: UserResponse) {
    setToken(accessToken)
    setTokenState(accessToken)
    setUser(userData)
  }

  function logout() {
    removeToken()
    setTokenState(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}
