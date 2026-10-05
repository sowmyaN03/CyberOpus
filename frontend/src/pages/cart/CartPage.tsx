import { Link } from 'react-router-dom'
import { useCart } from '../../hooks/useCart'
import { formatCurrency } from '../../utils/format'
import LoadingSpinner from '../../components/common/LoadingSpinner'

export default function CartPage() {
  const { cart, items, loading, updateItem, removeItem } = useCart()

  if (loading) return <LoadingSpinner />

  if (!items.length) {
    return (
      <div style={{ maxWidth: '800px', margin: '4rem auto', padding: '0 1.5rem', textAlign: 'center' }}>
        <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🛒</div>
        <h2 style={{ color: '#1a3a5c', marginBottom: '1rem' }}>Your cart is empty</h2>
        <Link to="/books" style={{ color: '#e67e22', textDecoration: 'none', fontWeight: 600 }}>
          Continue Shopping →
        </Link>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <h1 style={{ color: '#1a3a5c', marginBottom: '1.5rem' }}>Shopping Cart</h1>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '2rem' }}>
        {/* Items */}
        <div>
          {items.map((item) => (
            <div
              key={item.itemId}
              style={{
                display: 'flex',
                gap: '1rem',
                background: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                padding: '1rem',
                marginBottom: '0.75rem',
              }}
            >
              <div
                style={{
                  width: '70px',
                  height: '90px',
                  background: '#f3f4f6',
                  borderRadius: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '2rem',
                  flexShrink: 0,
                }}
              >
                {item.imageUrl ? (
                  <img src={item.imageUrl} alt={item.bookTitle} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '4px' }} />
                ) : '📖'}
              </div>
              <div style={{ flex: 1 }}>
                <Link to={`/books/${item.bookId}`} style={{ color: '#1a3a5c', fontWeight: 600, textDecoration: 'none' }}>
                  {item.bookTitle}
                </Link>
                <p style={{ color: '#57606a', fontSize: '0.875rem', margin: '0.2rem 0' }}>{item.bookAuthor}</p>
                {item.priceAtAdd !== item.currentPrice && (
                  <p style={{ color: '#d97706', fontSize: '0.8rem' }}>
                    ⚠ Price changed from {formatCurrency(item.priceAtAdd)} to {formatCurrency(item.currentPrice)}
                  </p>
                )}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
                  <button
                    onClick={() => updateItem(item.itemId, item.quantity - 1)}
                    disabled={item.quantity <= 1}
                    style={{ padding: '0.2rem 0.6rem', border: '1px solid #d1d5db', borderRadius: '4px', cursor: 'pointer', background: '#fff' }}
                  >
                    −
                  </button>
                  <span style={{ minWidth: '2rem', textAlign: 'center' }}>{item.quantity}</span>
                  <button
                    onClick={() => updateItem(item.itemId, item.quantity + 1)}
                    style={{ padding: '0.2rem 0.6rem', border: '1px solid #d1d5db', borderRadius: '4px', cursor: 'pointer', background: '#fff' }}
                  >
                    +
                  </button>
                  <button
                    onClick={() => removeItem(item.itemId)}
                    style={{ marginLeft: 'auto', background: 'none', border: 'none', color: '#dc2626', cursor: 'pointer', fontSize: '0.875rem' }}
                  >
                    Remove
                  </button>
                </div>
              </div>
              <div style={{ textAlign: 'right', fontWeight: 700, color: '#1a3a5c' }}>
                {formatCurrency(item.lineTotal)}
              </div>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div>
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.5rem', position: 'sticky', top: '80px' }}>
            <h3 style={{ margin: '0 0 1rem', color: '#1a3a5c' }}>Order Summary</h3>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: '#57606a' }}>
              <span>Subtotal ({cart?.itemCount ?? 0} items)</span>
              <span>{formatCurrency(cart?.subtotal ?? 0)}</span>
            </div>
            <hr style={{ border: 'none', borderTop: '1px solid #e5e7eb', margin: '1rem 0' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '1.1rem', color: '#1a3a5c' }}>
              <span>Subtotal</span>
              <span>{formatCurrency(cart?.subtotal ?? 0)}</span>
            </div>
            <Link
              to="/checkout"
              style={{
                display: 'block',
                marginTop: '1.25rem',
                background: '#e67e22',
                color: '#fff',
                textAlign: 'center',
                padding: '0.75rem',
                borderRadius: '6px',
                fontWeight: 600,
                textDecoration: 'none',
              }}
            >
              Proceed to Checkout
            </Link>
            <Link
              to="/books"
              style={{ display: 'block', textAlign: 'center', marginTop: '0.75rem', color: '#57606a', fontSize: '0.875rem', textDecoration: 'none' }}
            >
              Continue Shopping
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
