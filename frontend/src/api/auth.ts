import client from './client'
import type { AuthResponse, UserResponse } from '../types'

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  phone?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const res = await client.post<AuthResponse>('/api/auth/register', data)
  return res.data
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await client.post<AuthResponse>('/api/auth/login', data)
  return res.data
}

export async function getMe(): Promise<UserResponse> {
  const res = await client.get<UserResponse>('/api/auth/me')
  return res.data
}
