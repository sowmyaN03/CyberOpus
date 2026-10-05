import client from './client'
import type { BrandResponse, PageResponse, BookResponse } from '../types'

export interface BrandRequest {
  name: string
  description?: string
}

export async function getBrands(): Promise<BrandResponse[]> {
  const res = await client.get<BrandResponse[]>('/api/brands')
  return res.data
}

export async function getBrandBooks(
  id: number,
  params?: { page?: number; size?: number },
): Promise<PageResponse<BookResponse>> {
  const res = await client.get<PageResponse<BookResponse>>(`/api/brands/${id}/books`, { params })
  return res.data
}

export async function createBrand(data: BrandRequest): Promise<BrandResponse> {
  const res = await client.post<BrandResponse>('/api/brands', data)
  return res.data
}

export async function updateBrand(id: number, data: BrandRequest): Promise<BrandResponse> {
  const res = await client.put<BrandResponse>(`/api/brands/${id}`, data)
  return res.data
}

export async function deleteBrand(id: number): Promise<void> {
  await client.delete(`/api/brands/${id}`)
}
