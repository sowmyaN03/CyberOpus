import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '../../api/addresses'
import type { AddressResponse, AddressRequest } from '../../types'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'
import type { AxiosError } from 'axios'
import type { ErrorResponse } from '../../types'

const blank: AddressRequest = {
  fullName: '', line1: '', line2: '', city: '', state: '', postalCode: '', country: 'US', phone: '',
}

export default function AddressesPage() {
  const [addresses, setAddresses] = useState<AddressResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [form, setForm] = useState<AddressRequest>(blank)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const navigate = useNavigate()

  async function load() {
    setLoading(true)
    try {
      setAddresses(await getAddresses())
    } catch {
      setError('Failed to load addresses.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  function openAdd() { setForm(blank); setEditId(null); setFormError(''); setShowForm(true) }

  function openEdit(addr: AddressResponse) {
    setForm({
      fullName: addr.fullName, line1: addr.line1, line2: addr.line2 ?? '',
      city: addr.city, state: addr.state, postalCode: addr.postalCode,
      country: addr.country, phone: addr.phone,
    })
    setEditId(addr.id)
    setFormError('')
    setShowForm(true)
  }

  async function handleDelete(id: number) {
    if (!confirm('Delete this address?')) return
    await deleteAddress(id)
    await load()
  }

  async function handleDefault(id: number) {
    await setDefaultAddress(id)
    await load()
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      if (editId) {
        await updateAddress(editId, form)
      } else {
        await createAddress(form)
      }
      setShowForm(false)
      await load()
    } catch (err) {
      const axiosErr = err as AxiosError<ErrorResponse>
      setFormError(axiosErr.response?.data?.message ?? 'Failed to save address.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <LoadingSpinner />

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ color: '#1a3a5c', margin: 0 }}>My Addresses</h1>
        <button
          onClick={openAdd}
          style={{ background: '#e67e22', color: '#fff', border: 'none', borderRadius: '6px', padding: '0.5rem 1.25rem', cursor: 'pointer', fontWeight: 600 }}
        >
          + Add Address
        </button>
      </div>

      {error && <ErrorMessage message={error} onRetry={load} />}

      {showForm && (
        <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.5rem', marginBottom: '1.5rem' }}>
          <h3 style={{ margin: '0 0 1rem', color: '#1a3a5c' }}>{editId ? 'Edit Address' : 'New Address'}</h3>
          {formError && <div style={{ background: '#fef2f2', color: '#b91c1c', padding: '0.6rem 0.8rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.875rem' }}>{formError}</div>}
          <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            {(
              [
                ['fullName', 'Full Name', 'text', true],
                ['phone', 'Phone', 'tel', true],
                ['line1', 'Address Line 1', 'text', true],
                ['line2', 'Address Line 2 (optional)', 'text', false],
                ['city', 'City', 'text', true],
                ['state', 'State', 'text', true],
                ['postalCode', 'Postal Code', 'text', true],
                ['country', 'Country', 'text', true],
              ] as [keyof AddressRequest, string, string, boolean][]
            ).map(([key, label, type, required]) => (
              <label key={key} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500, color: '#1f2328' }}>
                {label}
                <input
                  type={type}
                  value={form[key] ?? ''}
                  onChange={(e) => setForm((prev) => ({ ...prev, [key]: e.target.value }))}
                  required={required}
                  style={{ padding: '0.5rem 0.75rem', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '0.875rem' }}
                />
              </label>
            ))}
            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
              <button type="button" onClick={() => setShowForm(false)} style={{ padding: '0.5rem 1rem', border: '1px solid #d1d5db', borderRadius: '6px', background: '#fff', cursor: 'pointer' }}>
                Cancel
              </button>
              <button type="submit" disabled={saving} style={{ padding: '0.5rem 1.25rem', background: '#1a3a5c', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}>
                {saving ? 'Saving…' : 'Save'}
              </button>
            </div>
          </form>
        </div>
      )}

      {addresses.length === 0 && !showForm && (
        <p style={{ color: '#57606a', textAlign: 'center', padding: '2rem' }}>No addresses yet. Add one above.</p>
      )}

      {addresses.map((addr) => (
        <div key={addr.id} style={{ background: '#fff', border: `1px solid ${addr.isDefault ? '#1a3a5c' : '#e5e7eb'}`, borderRadius: '8px', padding: '1rem 1.25rem', marginBottom: '0.75rem', position: 'relative' }}>
          {addr.isDefault && (
            <span style={{ position: 'absolute', top: '0.75rem', right: '0.75rem', background: '#1a3a5c', color: '#fff', fontSize: '0.7rem', padding: '0.15rem 0.5rem', borderRadius: '4px' }}>
              Default
            </span>
          )}
          <p style={{ margin: '0 0 0.25rem', fontWeight: 600 }}>{addr.fullName}</p>
          <p style={{ margin: '0 0 0.1rem', fontSize: '0.875rem', color: '#57606a' }}>{addr.line1}{addr.line2 ? `, ${addr.line2}` : ''}</p>
          <p style={{ margin: '0 0 0.1rem', fontSize: '0.875rem', color: '#57606a' }}>{addr.city}, {addr.state} {addr.postalCode}, {addr.country}</p>
          <p style={{ margin: '0 0 0.75rem', fontSize: '0.875rem', color: '#57606a' }}>{addr.phone}</p>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button onClick={() => openEdit(addr)} style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem', border: '1px solid #d1d5db', borderRadius: '4px', cursor: 'pointer', background: '#fff' }}>Edit</button>
            <button onClick={() => handleDelete(addr.id)} style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem', border: 'none', background: '#fee2e2', color: '#dc2626', borderRadius: '4px', cursor: 'pointer' }}>Delete</button>
            {!addr.isDefault && (
              <button onClick={() => handleDefault(addr.id)} style={{ fontSize: '0.8rem', padding: '0.3rem 0.7rem', border: '1px solid #1a3a5c', borderRadius: '4px', cursor: 'pointer', background: '#fff', color: '#1a3a5c' }}>Set Default</button>
            )}
          </div>
        </div>
      ))}

      <div style={{ marginTop: '1.5rem' }}>
        <button onClick={() => navigate('/checkout')} style={{ background: '#e67e22', color: '#fff', border: 'none', borderRadius: '6px', padding: '0.6rem 1.5rem', cursor: 'pointer', fontWeight: 600 }}>
          Go to Checkout →
        </button>
      </div>
    </div>
  )
}
