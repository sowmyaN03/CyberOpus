import client from './client'
import type { RewardResponse } from '../types'

export async function getRewards(): Promise<RewardResponse> {
  const res = await client.get<RewardResponse>('/api/rewards')
  return res.data
}
