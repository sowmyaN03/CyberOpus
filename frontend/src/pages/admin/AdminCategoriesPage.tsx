import { useEffect, useState } from 'react'
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from '../../api/categories'
import type { CategoryResponse } from '../../types'
import type { CategoryRequest } from '../../api/categories'
import AdminTable from '../../components/admin/AdminTable'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'
import { formatDate } from '../../utils/format'
import type { AxiosError } from 'axios'
import type { ErrorResponse } from '../../types'

export default function AdminCategoriesPage() {
  const [items, setItems] = useState<CategoryResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [form, setForm] = useState<CategoryRequest>({ name: '', description: '' })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  async function load() {
    setLoading(true)
    try { setItems(await getCategories()) }
    catch { setError('Failed to load categories.') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  function openAdd() { setForm({ name: '', description: '' }); setEditId(null); setFormError(''); setShowForm(true) }
  function openEdit(item: CategoryResponse) { setForm({ name: item.name, description: item.description ?? '' }); setEditId(item.id); setFormError(''); setShowForm(true) }

  async function handleDelete(item: CategoryResponse) {
    await deleteCategory(item.id)
    await load()
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      if (editId) { await updateCategory(editId, form) }
      else { await createCategory(form) }
      setShowForm(false)
      await load()
    } catch (err) {
      const axiosErr = err as AxiosError<ErrorResponse>
      setFormError(axiosErr.response?.data?.message ?? 'Save failed.')
    } finally { setSaving(false) }
  }

  if (loading) return <LoadingSpinner />

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ color: '#1a3a5c', margin: 0 }}>Categories</h1>
        <button onClick={openAdd} style={{ background: '#e67e22', color: '#fff', border: 'none', borderRadius: '6px', padding: '0.5rem 1.25rem', cursor: 'pointer', fontWeight: 600 }}>+ Add</button>
      </div>
      {error && <ErrorMessage message={error} onRetry={load} />}
      {showForm && (
        <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.5rem', marginBottom: '1.5rem' }}>
          <h3 style={{ margin: '0 0 1rem', color: '#1a3a5c' }}>{editId ? 'Edit' : 'New'} Category</h3>
          {formError && <div style={{ background: '#fef2f2', color: '#b91c1c', padding: '0.6rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.875rem' }}>{formError}</div>}
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Name *
              <input type="text" value={form.name} onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))} required style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Description
              <input type="text" value={form.description ?? ''} onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))} style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
              <button type="button" onClick={() => setShowForm(false)} style={{ padding: '0.5rem 1rem', border: '1px solid #d1d5db', borderRadius: '6px', background: '#fff', cursor: 'pointer' }}>Cancel</button>
              <button type="submit" disabled={saving} style={{ padding: '0.5rem 1.25rem', background: '#1a3a5c', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}>
                {saving ? 'Saving…' : 'Save'}
              </button>
            </div>
          </form>
        </div>
      )}
      <AdminTable
        columns={[
          { label: 'Name', render: (c) => c.name },
          { label: 'Description', render: (c) => c.description ?? '—' },
          { label: 'Created', render: (c) => formatDate(c.createdAt) },
        ]}
        rows={items}
        keyFn={(c) => c.id}
        onEdit={openEdit}
        onDelete={handleDelete}
      />
    </div>
  )
}
