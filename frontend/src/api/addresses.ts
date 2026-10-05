import client from './client'
import type { AddressResponse, AddressRequest } from '../types'

export async function getAddresses(): Promise<AddressResponse[]> {
  const res = await client.get<AddressResponse[]>('/api/addresses')
  return res.data
}

export async function createAddress(data: AddressRequest): Promise<AddressResponse> {
  const res = await client.post<AddressResponse>('/api/addresses', data)
  return res.data
}

export async function updateAddress(id: number, data: AddressRequest): Promise<AddressResponse> {
  const res = await client.put<AddressResponse>(`/api/addresses/${id}`, data)
  return res.data
}

export async function deleteAddress(id: number): Promise<void> {
  await client.delete(`/api/addresses/${id}`)
}

export async function setDefaultAddress(id: number): Promise<AddressResponse> {
  const res = await client.patch<AddressResponse>(`/api/addresses/${id}/default`)
  return res.data
}
