import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAddresses } from '../../api/addresses'
import { checkout } from '../../api/checkout'
import { getRewards } from '../../api/rewards'
import { useCart } from '../../hooks/useCart'
import type { AddressResponse, PaymentMethod } from '../../types'
import { formatCurrency } from '../../utils/format'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'
import type { AxiosError } from 'axios'
import type { ErrorResponse } from '../../types'

export default function CheckoutPage() {
  const { cart, items, clearCart } = useCart()
  const navigate = useNavigate()

  const [addresses, setAddresses] = useState<AddressResponse[]>([])
  const [rewardBalance, setRewardBalance] = useState(0)
  const [loading, setLoading] = useState(true)

  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null)
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CREDIT_CARD')
  const [cardHolderName, setCardHolderName] = useState('')
  const [maskedCardNumber, setMaskedCardNumber] = useState('')
  const [rewardPointsToRedeem, setRewardPointsToRedeem] = useState(0)

  const [placing, setPlacing] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([getAddresses(), getRewards()])
      .then(([addrs, rewards]) => {
        setAddresses(addrs)
        const def = addrs.find((a) => a.isDefault)
        setSelectedAddressId(def?.id ?? addrs[0]?.id ?? null)
        setRewardBalance(rewards.balance)
      })
      .finally(() => setLoading(false))
  }, [])

  // Compute display totals from cart
  const subtotal = cart?.subtotal ?? 0
  const maxRedeemable = Math.floor(Math.min(rewardBalance, Math.floor(subtotal * 0.2 * 100)) / 100) * 100
  const rewardDiscount = rewardPointsToRedeem / 100
  const deliveryCharge = 0 // backend decides
  const estimatedTotal = subtotal - rewardDiscount + deliveryCharge

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!selectedAddressId) { setError('Please select a delivery address.'); return }
    setError('')
    setPlacing(true)
    try {
      const order = await checkout({
        addressId: selectedAddressId,
        paymentMethod,
        cardHolderName: cardHolderName || undefined,
        maskedCardNumber: maskedCardNumber || undefined,
        rewardPointsToRedeem: rewardPointsToRedeem || undefined,
      })
      await clearCart()
      navigate(`/orders/${order.id}`, { state: { newOrder: true } })
    } catch (err) {
      const axiosErr = err as AxiosError<ErrorResponse>
      setError(axiosErr.response?.data?.message ?? 'Checkout failed. Please try again.')
    } finally {
      setPlacing(false)
    }
  }

  if (loading) return <LoadingSpinner />

  if (!items.length) {
    return (
      <div style={{ maxWidth: '600px', margin: '4rem auto', padding: '0 1.5rem', textAlign: 'center' }}>
        <h2 style={{ color: '#1a3a5c' }}>Your cart is empty</h2>
        <a href="/books" style={{ color: '#e67e22' }}>Browse Books</a>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <h1 style={{ color: '#1a3a5c', marginBottom: '1.5rem' }}>Checkout</h1>
      {error && <ErrorMessage message={error} />}

      <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 320px', gap: '2rem' }}>
        <div>
          {/* Address */}
          <section style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.25rem' }}>
            <h3 style={{ color: '#1a3a5c', margin: '0 0 1rem' }}>Delivery Address</h3>
            {addresses.length === 0 ? (
              <p style={{ color: '#57606a' }}>No addresses found. <a href="/addresses" style={{ color: '#e67e22' }}>Add one</a>.</p>
            ) : (
              addresses.map((addr) => (
                <label key={addr.id} style={{ display: 'flex', gap: '0.75rem', alignItems: 'flex-start', marginBottom: '0.75rem', cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="address"
                    checked={selectedAddressId === addr.id}
                    onChange={() => setSelectedAddressId(addr.id)}
                    style={{ marginTop: '3px' }}
                  />
                  <span style={{ fontSize: '0.875rem' }}>
                    <strong>{addr.fullName}</strong><br />
                    {addr.line1}{addr.line2 ? `, ${addr.line2}` : ''}<br />
                    {addr.city}, {addr.state} {addr.postalCode}, {addr.country}
                  </span>
                </label>
              ))
            )}
          </section>

          {/* Payment */}
          <section style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.25rem' }}>
            <h3 style={{ color: '#1a3a5c', margin: '0 0 1rem' }}>Payment</h3>
            <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
              {(['CREDIT_CARD', 'DEBIT_CARD'] as PaymentMethod[]).map((m) => (
                <label key={m} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', cursor: 'pointer' }}>
                  <input type="radio" name="paymentMethod" checked={paymentMethod === m} onChange={() => setPaymentMethod(m)} />
                  {m === 'CREDIT_CARD' ? 'Credit Card' : 'Debit Card'}
                </label>
              ))}
            </div>
            <div style={{ display: 'grid', gap: '0.75rem' }}>
              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
                Cardholder Name
                <input
                  type="text"
                  value={cardHolderName}
                  onChange={(e) => setCardHolderName(e.target.value)}
                  placeholder="As on card (use FAIL_TEST to test failure)"
                  required
                  style={{ padding: '0.5rem 0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                />
              </label>
              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
                Card Number (last 4 digits)
                <input
                  type="text"
                  value={maskedCardNumber}
                  onChange={(e) => setMaskedCardNumber(e.target.value)}
                  placeholder="e.g. **** **** **** 1234"
                  style={{ padding: '0.5rem 0.75rem', border: '1px solid #d1d5db', borderRadius: '6px' }}
                />
              </label>
            </div>
          </section>

          {/* Rewards */}
          {rewardBalance > 0 && (
            <section style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.25rem' }}>
              <h3 style={{ color: '#1a3a5c', margin: '0 0 0.5rem' }}>Reward Points</h3>
              <p style={{ fontSize: '0.875rem', color: '#57606a', margin: '0 0 0.75rem' }}>
                Balance: {rewardBalance.toLocaleString()} pts · Max redeemable: {maxRedeemable} pts ({formatCurrency(maxRedeemable / 100)} off)
              </p>
              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
                Points to redeem (multiples of 100)
                <input
                  type="number"
                  min={0}
                  max={maxRedeemable}
                  step={100}
                  value={rewardPointsToRedeem}
                  onChange={(e) => setRewardPointsToRedeem(Number(e.target.value))}
                  style={{ padding: '0.5rem 0.75rem', border: '1px solid #d1d5db', borderRadius: '6px', width: '180px' }}
                />
              </label>
            </section>
          )}
        </div>

        {/* Order Summary */}
        <div>
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.25rem', position: 'sticky', top: '80px' }}>
            <h3 style={{ color: '#1a3a5c', margin: '0 0 1rem' }}>Order Summary</h3>
            {items.map((item) => (
              <div key={item.itemId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.4rem' }}>
                <span style={{ color: '#57606a' }}>{item.bookTitle} ×{item.quantity}</span>
                <span>{formatCurrency(item.lineTotal)}</span>
              </div>
            ))}
            <hr style={{ border: 'none', borderTop: '1px solid #e5e7eb', margin: '0.75rem 0' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.3rem' }}>
              <span>Subtotal</span><span>{formatCurrency(subtotal)}</span>
            </div>
            {rewardPointsToRedeem > 0 && (
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', color: '#16a34a', marginBottom: '0.3rem' }}>
                <span>Reward Discount</span><span>−{formatCurrency(rewardDiscount)}</span>
              </div>
            )}
            <hr style={{ border: 'none', borderTop: '1px solid #e5e7eb', margin: '0.75rem 0' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '1.05rem' }}>
              <span>Estimated Total</span><span style={{ color: '#1a3a5c' }}>{formatCurrency(estimatedTotal)}</span>
            </div>
            <p style={{ fontSize: '0.75rem', color: '#9ca3af', margin: '0.5rem 0 1rem' }}>* Final total determined by server</p>
            <button
              type="submit"
              disabled={placing}
              style={{ width: '100%', padding: '0.75rem', background: '#e67e22', color: '#fff', border: 'none', borderRadius: '6px', fontWeight: 700, fontSize: '1rem', cursor: 'pointer' }}
            >
              {placing ? 'Placing Order…' : 'Place Order'}
            </button>
          </div>
        </div>
      </form>
    </div>
  )
}
