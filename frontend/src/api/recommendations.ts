import client from './client'
import type { BookResponse } from '../types'

export async function getRecommendations(limit = 10): Promise<BookResponse[]> {
  const res = await client.get<BookResponse[]>('/api/recommendations', { params: { limit } })
  return res.data
}
