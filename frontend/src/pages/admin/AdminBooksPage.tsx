import { useEffect, useState } from 'react'
import { getCategories } from '../../api/categories'
import { getBrands } from '../../api/brands'
import {
  getBooks,
  createBook,
  updateBook,
  deleteBook,
} from '../../api/books'
import type { BookResponse, CategoryResponse, BrandResponse, BookRequest } from '../../types'
import AdminTable from '../../components/admin/AdminTable'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'
import { formatCurrency } from '../../utils/format'
import type { AxiosError } from 'axios'
import type { ErrorResponse } from '../../types'

const blankBook: BookRequest = {
  title: '', author: '', description: '', isbn: '', price: 0, stockQuantity: 0, imageUrl: '', categoryId: undefined, brandId: undefined,
}

export default function AdminBooksPage() {
  const [books, setBooks] = useState<BookResponse[]>([])
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [brands, setBrands] = useState<BrandResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [form, setForm] = useState<BookRequest>(blankBook)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  async function load() {
    setLoading(true)
    try {
      const [bPage, cats, brs] = await Promise.all([getBooks({ size: 100 }), getCategories(), getBrands()])
      setBooks(bPage.content)
      setCategories(cats)
      setBrands(brs)
    } catch {
      setError('Failed to load data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  function openAdd() { setForm(blankBook); setEditId(null); setFormError(''); setShowForm(true) }

  function openEdit(book: BookResponse) {
    setForm({
      title: book.title, author: book.author, description: book.description ?? '',
      isbn: book.isbn ?? '', price: book.price, stockQuantity: book.stockQuantity,
      imageUrl: book.imageUrl ?? '', categoryId: book.category?.id, brandId: book.brand?.id,
    })
    setEditId(book.id)
    setFormError('')
    setShowForm(true)
  }

  async function handleDelete(book: BookResponse) {
    await deleteBook(book.id)
    await load()
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      if (editId) {
        await updateBook(editId, form)
      } else {
        await createBook(form)
      }
      setShowForm(false)
      await load()
    } catch (err) {
      const axiosErr = err as AxiosError<ErrorResponse>
      setFormError(axiosErr.response?.data?.message ?? 'Save failed.')
    } finally {
      setSaving(false)
    }
  }

  function field(key: keyof BookRequest) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
      const value = key === 'price' || key === 'stockQuantity' ? Number(e.target.value) :
        (key === 'categoryId' || key === 'brandId') ? (e.target.value ? Number(e.target.value) : undefined) :
        e.target.value
      setForm((prev) => ({ ...prev, [key]: value }))
    }
  }

  if (loading) return <LoadingSpinner />

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ color: '#1a3a5c', margin: 0 }}>Books</h1>
        <button onClick={openAdd} style={{ background: '#e67e22', color: '#fff', border: 'none', borderRadius: '6px', padding: '0.5rem 1.25rem', cursor: 'pointer', fontWeight: 600 }}>
          + Add Book
        </button>
      </div>

      {error && <ErrorMessage message={error} onRetry={load} />}

      {showForm && (
        <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '1.5rem', marginBottom: '1.5rem' }}>
          <h3 style={{ margin: '0 0 1rem', color: '#1a3a5c' }}>{editId ? 'Edit Book' : 'New Book'}</h3>
          {formError && <div style={{ background: '#fef2f2', color: '#b91c1c', padding: '0.6rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.875rem' }}>{formError}</div>}
          <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Title *
              <input type="text" value={form.title} onChange={field('title')} required style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Author *
              <input type="text" value={form.author} onChange={field('author')} required style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Price *
              <input type="number" step="0.01" min="0" value={form.price} onChange={field('price')} required style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Stock *
              <input type="number" min="0" value={form.stockQuantity} onChange={field('stockQuantity')} required style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              ISBN
              <input type="text" value={form.isbn ?? ''} onChange={field('isbn')} style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Image URL
              <input type="url" value={form.imageUrl ?? ''} onChange={field('imageUrl')} style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }} />
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Category
              <select value={form.categoryId ?? ''} onChange={field('categoryId')} style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }}>
                <option value="">None</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500 }}>
              Publisher
              <select value={form.brandId ?? ''} onChange={field('brandId')} style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px' }}>
                <option value="">None</option>
                {brands.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
            </label>
            <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.875rem', fontWeight: 500, gridColumn: '1 / -1' }}>
              Description
              <textarea value={form.description ?? ''} onChange={field('description')} rows={3} style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '6px', resize: 'vertical' }} />
            </label>
            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
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
          { label: 'Title', render: (b) => b.title },
          { label: 'Author', render: (b) => b.author },
          { label: 'Price', render: (b) => formatCurrency(b.price) },
          { label: 'Stock', render: (b) => b.stockQuantity },
          { label: 'Category', render: (b) => b.category?.name ?? '—' },
        ]}
        rows={books}
        keyFn={(b) => b.id}
        onEdit={openEdit}
        onDelete={handleDelete}
      />
    </div>
  )
}
