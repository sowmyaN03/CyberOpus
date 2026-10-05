import client from './client'
import type { CategoryResponse, PageResponse, BookResponse } from '../types'

export interface CategoryRequest {
  name: string
  description?: string
}

export async function getCategories(): Promise<CategoryResponse[]> {
  const res = await client.get<CategoryResponse[]>('/api/categories')
  return res.data
}

export async function getCategoryBooks(
  id: number,
  params?: { page?: number; size?: number },
): Promise<PageResponse<BookResponse>> {
  const res = await client.get<PageResponse<BookResponse>>(`/api/categories/${id}/books`, {
    params,
  })
  return res.data
}

export async function createCategory(data: CategoryRequest): Promise<CategoryResponse> {
  const res = await client.post<CategoryResponse>('/api/categories', data)
  return res.data
}

export async function updateCategory(id: number, data: CategoryRequest): Promise<CategoryResponse> {
  const res = await client.put<CategoryResponse>(`/api/categories/${id}`, data)
  return res.data
}

export async function deleteCategory(id: number): Promise<void> {
  await client.delete(`/api/categories/${id}`)
}
