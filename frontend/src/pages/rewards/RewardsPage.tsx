import { useEffect, useState } from 'react'
import { getRewards } from '../../api/rewards'
import type { RewardResponse } from '../../types'
import { formatDate, formatPoints } from '../../utils/format'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'

const TYPE_COLOR: Record<string, string> = {
  EARN: '#16a34a',
  REDEEM: '#dc2626',
  REFUND: '#2563eb',
}

export default function RewardsPage() {
  const [data, setData] = useState<RewardResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function load() {
    setLoading(true)
    try {
      setData(await getRewards())
    } catch {
      setError('Failed to load rewards.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  if (loading) return <LoadingSpinner />

  return (
    <div style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <h1 style={{ color: '#1a3a5c', marginBottom: '0.5rem' }}>Reward Points</h1>

      {error && <ErrorMessage message={error} onRetry={load} />}

      {data && (
        <>
          <div style={{ background: 'linear-gradient(135deg, #1a3a5c, #2c5282)', color: '#fff', borderRadius: '12px', padding: '2rem', marginBottom: '2rem', textAlign: 'center' }}>
            <p style={{ margin: '0 0 0.5rem', opacity: 0.8, fontSize: '0.9rem' }}>Current Balance</p>
            <p style={{ margin: 0, fontSize: '3rem', fontWeight: 800 }}>{data.balance.toLocaleString()}</p>
            <p style={{ margin: '0.5rem 0 0', opacity: 0.8, fontSize: '0.85rem' }}>points · 100 pts = $1.00 discount</p>
          </div>

          <div style={{ background: '#f7f8fa', borderRadius: '8px', padding: '1rem', marginBottom: '1.5rem', fontSize: '0.875rem', color: '#57606a' }}>
            <strong>How it works:</strong> Earn 10 pts per $1 spent · Redeem 100 pts for $1 off · Max 20% of order subtotal
          </div>

          <h2 style={{ color: '#1a3a5c', fontSize: '1.1rem', marginBottom: '1rem' }}>Transaction History</h2>
          {data.transactions.length === 0 ? (
            <p style={{ color: '#9ca3af', textAlign: 'center' }}>No transactions yet.</p>
          ) : (
            data.transactions.map((tx) => (
              <div key={tx.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '0.875rem 1rem', marginBottom: '0.5rem' }}>
                <div>
                  <p style={{ margin: '0 0 0.15rem', fontWeight: 500 }}>{tx.description}</p>
                  <p style={{ margin: 0, fontSize: '0.8rem', color: '#9ca3af' }}>{formatDate(tx.createdAt)}</p>
                </div>
                <span style={{ fontWeight: 700, color: TYPE_COLOR[tx.type], whiteSpace: 'nowrap' }}>
                  {tx.type === 'REDEEM' ? '−' : '+'}{formatPoints(Math.abs(tx.points))}
                </span>
              </div>
            ))
          )}
        </>
      )}
    </div>
  )
}
