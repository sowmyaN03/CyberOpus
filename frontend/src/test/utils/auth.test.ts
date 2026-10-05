import { describe, it, expect, beforeEach } from 'vitest'
import {
  TOKEN_KEY,
  getToken,
  setToken,
  removeToken,
  isAuthenticated,
  isTokenExpired,
} from '../../utils/auth'

beforeEach(() => {
  localStorage.clear()
})

describe('token helpers', () => {
  it('getToken returns null when not set', () => {
    expect(getToken()).toBeNull()
  })

  it('setToken stores value', () => {
    setToken('abc123')
    expect(localStorage.getItem(TOKEN_KEY)).toBe('abc123')
  })

  it('getToken reads stored value', () => {
    localStorage.setItem(TOKEN_KEY, 'mytoken')
    expect(getToken()).toBe('mytoken')
  })

  it('removeToken clears value', () => {
    localStorage.setItem(TOKEN_KEY, 'x')
    removeToken()
    expect(getToken()).toBeNull()
  })

  it('isAuthenticated returns false when no token', () => {
    expect(isAuthenticated()).toBe(false)
  })

  it('isAuthenticated returns true when token present', () => {
    setToken('sometoken')
    expect(isAuthenticated()).toBe(true)
  })
})

describe('isTokenExpired', () => {
  it('returns true for an obviously expired token', () => {
    // Build a JWT with exp in 2020 (past)
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).replace(/=/g, '')
    const payload = btoa(JSON.stringify({ exp: 1577836800, sub: '1' })).replace(/=/g, '')
    const token = `${header}.${payload}.fakesig`
    expect(isTokenExpired(token)).toBe(true)
  })

  it('returns false for a future token', () => {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).replace(/=/g, '')
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600, sub: '1' })).replace(/=/g, '')
    const token = `${header}.${payload}.fakesig`
    expect(isTokenExpired(token)).toBe(false)
  })

  it('returns true for malformed token', () => {
    expect(isTokenExpired('not.a.token')).toBe(true)
  })
})
