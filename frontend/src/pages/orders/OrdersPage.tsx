import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { getOrders } from '../../api/orders'
import type { OrderResponse, PageResponse } from '../../types'
import { formatCurrency, formatDate, formatOrderStatus } from '../../utils/format'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'
import Pagination from '../../components/common/Pagination'

const STATUS_COLOR: Record<string, string> = {
  PENDING: '#d97706',
  PROCESSING: '#2563eb',
  SHIPPED: '#7c3aed',
  DELIVERED: '#16a34a',
  CANCELLED: '#dc2626',
}

export default function OrdersPage() {
  const [page, setPage] = useState<PageResponse<OrderResponse> | null>(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setPage(await getOrders({ page: currentPage, size: 10 }))
    } catch {
      setError('Failed to load orders.')
    } finally {
      setLoading(false)
    }
  }, [currentPage])

  useEffect(() => { load() }, [load])

  if (loading) return <LoadingSpinner />

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <h1 style={{ color: '#1a3a5c', marginBottom: '1.5rem' }}>My Orders</h1>
      {error && <ErrorMessage message={error} onRetry={load} />}
      {page?.content.length === 0 && (
        <p style={{ color: '#57606a', textAlign: 'center', padding: '2rem' }}>
          No orders yet. <Link to="/books" style={{ color: '#e67e22' }}>Start shopping</Link>.
        </p>
      )}
      {page?.content.map((order) => (
        <Link
          key={order.id}
          to={`/orders/${order.id}`}
          style={{ display: 'block', textDecoration: 'none', color: 'inherit' }}
        >
          <div
            style={{
              background: '#fff',
              border: '1px solid #e5e7eb',
              borderRadius: '8px',
              padding: '1rem 1.25rem',
              marginBottom: '0.75rem',
              transition: 'box-shadow 0.15s',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <p style={{ margin: '0 0 0.2rem', fontWeight: 600, color: '#1a3a5c' }}>#{order.orderNumber}</p>
                <p style={{ margin: '0', fontSize: '0.875rem', color: '#57606a' }}>
                  {order.items.length} item{order.items.length !== 1 ? 's' : ''} · Placed {formatDate(order.placedAt)}
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span
                  style={{
                    display: 'inline-block',
                    padding: '0.2rem 0.6rem',
                    borderRadius: '4px',
                    fontSize: '0.75rem',
                    fontWeight: 600,
                    background: `${STATUS_COLOR[order.status]}1a`,
                    color: STATUS_COLOR[order.status],
                  }}
                >
                  {formatOrderStatus(order.status)}
                </span>
                <p style={{ margin: '0.3rem 0 0', fontWeight: 700, color: '#1a3a5c' }}>
                  {formatCurrency(order.totalAmount)}
                </p>
              </div>
            </div>
          </div>
        </Link>
      ))}
      {page && (
        <Pagination
          currentPage={page.number}
          totalPages={page.totalPages}
          onPageChange={setCurrentPage}
        />
      )}
    </div>
  )
}
