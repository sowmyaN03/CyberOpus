import client from './client'
import type { BookResponse, PageResponse, BookRequest } from '../types'

export interface BookSearchParams {
  q?: string
  categoryId?: number
  brandId?: number
  minPrice?: number
  maxPrice?: number
  page?: number
  size?: number
  sort?: string
}

export async function getBooks(params?: BookSearchParams): Promise<PageResponse<BookResponse>> {
  const res = await client.get<PageResponse<BookResponse>>('/api/books', { params })
  return res.data
}

export async function getBook(id: number): Promise<BookResponse> {
  const res = await client.get<BookResponse>(`/api/books/${id}`)
  return res.data
}

export async function searchBooks(params: BookSearchParams): Promise<PageResponse<BookResponse>> {
  const res = await client.get<PageResponse<BookResponse>>('/api/books/search', { params })
  return res.data
}

export async function getRelatedBooks(id: number): Promise<BookResponse[]> {
  const res = await client.get<BookResponse[]>(`/api/books/${id}/related`)
  return res.data
}

export async function createBook(data: BookRequest): Promise<BookResponse> {
  const res = await client.post<BookResponse>('/api/books', data)
  return res.data
}

export async function updateBook(id: number, data: BookRequest): Promise<BookResponse> {
  const res = await client.put<BookResponse>(`/api/books/${id}`, data)
  return res.data
}

export async function deleteBook(id: number): Promise<void> {
  await client.delete(`/api/books/${id}`)
}
