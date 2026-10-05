import { useEffect, useState } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { getOrder, cancelOrder, buyAgain } from '../../api/orders'
import type { OrderResponse } from '../../types'
import { formatCurrency, formatDate, formatOrderStatus, formatPaymentMethod } from '../../utils/format'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'

const STATUS_COLOR: Record<string, string> = {
  PENDING: '#d97706', PROCESSING: '#2563eb', SHIPPED: '#7c3aed', DELIVERED: '#16a34a', CANCELLED: '#dc2626',
}

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const location = useLocation()
  const navigate = useNavigate()
  const isNew = (location.state as { newOrder?: boolean } | null)?.newOrder

  const [order, setOrder] = useState<OrderResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [cancelling, setCancelling] = useState(false)
  const [cancelMsg, setCancelMsg] = useState('')

  useEffect(() => {
    if (!id) return
    getOrder(Number(id))
      .then(setOrder)
      .catch(() => setError('Order not found.'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleCancel() {
    if (!order) return
    if (!confirm('Are you sure you want to cancel this order?')) return
    setCancelling(true)
    try {
      const updated = await cancelOrder(order.id)
      setOrder(updated)
      setCancelMsg('Order cancelled successfully.')
    } catch {
      setCancelMsg('Unable to cancel — may be past the 48h window.')
    } finally {
      setCancelling(false)
    }
  }

  async function handleBuyAgain(orderItemId: number) {
    if (!order) return
    await buyAgain(order.id, orderItemId)
    navigate('/cart')
  }

  if (loading) return <LoadingSpinner />
  if (error) return <div style={{ maxWidth: '800px', margin: '2rem auto', padding: '0 1.5rem' }}><ErrorMessage message={error} /></div>
  if (!order) return null

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      {isNew && (
        <div style={{ background: '#dcfce7', border: '1px solid #bbf7d0', borderRadius: '8px', padding: '1rem', marginBottom: '1.5rem', color: '#16a34a', fontWeight: 600 }}>
          ✓ Order placed successfully!
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ color: '#1a3a5c', margin: '0 0 0.25rem' }}>Order #{order.orderNumber}</h1>
          <p style={{ color: '#57606a', margin: 0, fontSize: '0.875rem' }}>Placed on {formatDate(order.placedAt)}</p>
        </div>
        <span style={{
          padding: '0.3rem 0.8rem', borderRadius: '4px', fontWeight: 600, fontSize: '0.875rem',
          background: `${STATUS_COLOR[order.status]}1a`, color: STATUS_COLOR[order.status],
        }}>
          {formatOrderStatus(order.status)}
        </span>
      </div>

      {cancelMsg && (
        <div style={{ background: '#fef9c3', border: '1px solid #fde047', borderRadius: '6px', padding: '0.75rem 1rem', marginBottom: '1rem', color: '#854d0e' }}>
          {cancelMsg}
        </div>
      )}

      {/* Items */}
      <section style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.25rem' }}>
        <h3 style={{ color: '#1a3a5c', margin: '0 0 1rem' }}>Items</h3>
        {order.items.map((item) => (
          <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.6rem 0', borderBottom: '1px solid #f3f4f6' }}>
            <div>
              <p style={{ margin: '0 0 0.1rem', fontWeight: 500 }}>{item.bookTitleSnapshot}</p>
              <p style={{ margin: 0, fontSize: '0.8rem', color: '#57606a' }}>{item.bookAuthorSnapshot} · Qty {item.quantity} · {formatCurrency(item.priceSnapshot)} each</p>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span style={{ fontWeight: 600 }}>{formatCurrency(item.lineTotal)}</span>
              <button
                onClick={() => handleBuyAgain(item.id)}
                style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem', background: '#1a3a5c', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
              >
                Buy Again
              </button>
            </div>
          </div>
        ))}
      </section>

      {/* Totals */}
      <section style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.25rem' }}>
        <h3 style={{ color: '#1a3a5c', margin: '0 0 1rem' }}>Order Total</h3>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.4rem', fontSize: '0.875rem' }}>
          <span>Subtotal</span><span>{formatCurrency(order.subtotal)}</span>
        </div>
        {order.rewardDiscount > 0 && (
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.4rem', fontSize: '0.875rem', color: '#16a34a' }}>
            <span>Reward Discount</span><span>−{formatCurrency(order.rewardDiscount)}</span>
          </div>
        )}
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.4rem', fontSize: '0.875rem' }}>
          <span>Delivery</span><span>{formatCurrency(order.deliveryCharge)}</span>
        </div>
        <hr style={{ border: 'none', borderTop: '1px solid #e5e7eb', margin: '0.5rem 0' }} />
        <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '1.05rem' }}>
          <span>Total</span><span style={{ color: '#1a3a5c' }}>{formatCurrency(order.totalAmount)}</span>
        </div>
      </section>

      {/* Payment */}
      {order.payment && (
        <section style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.25rem' }}>
          <h3 style={{ color: '#1a3a5c', margin: '0 0 1rem' }}>Payment</h3>
          <p style={{ margin: '0 0 0.3rem', fontSize: '0.875rem' }}>Method: {formatPaymentMethod(order.payment.paymentMethod)}</p>
          {order.payment.maskedCardNumber && <p style={{ margin: '0 0 0.3rem', fontSize: '0.875rem' }}>Card: {order.payment.maskedCardNumber}</p>}
          <p style={{ margin: '0 0 0.3rem', fontSize: '0.875rem' }}>Status: <strong style={{ color: order.payment.status === 'SUCCESS' ? '#16a34a' : '#dc2626' }}>{order.payment.status}</strong></p>
          {order.payment.transactionRef && <p style={{ margin: 0, fontSize: '0.8rem', color: '#9ca3af' }}>Ref: {order.payment.transactionRef}</p>}
        </section>
      )}

      {/* Delivery Address */}
      <section style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem' }}>
        <h3 style={{ color: '#1a3a5c', margin: '0 0 0.75rem' }}>Delivery Address</h3>
        <p style={{ margin: 0, fontSize: '0.875rem', whiteSpace: 'pre-line', color: '#57606a' }}>{order.addressSnapshot}</p>
      </section>

      {/* Cancel */}
      {(order.status === 'PENDING' || order.status === 'PROCESSING') && (
        <button
          onClick={handleCancel}
          disabled={cancelling}
          style={{ background: '#fee2e2', color: '#dc2626', border: 'none', borderRadius: '6px', padding: '0.6rem 1.5rem', cursor: 'pointer', fontWeight: 600 }}
        >
          {cancelling ? 'Cancelling…' : 'Cancel Order'}
        </button>
      )}
    </div>
  )
}
