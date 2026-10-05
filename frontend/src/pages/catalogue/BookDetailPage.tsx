import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getBook, getRelatedBooks } from '../../api/books'
import type { BookResponse } from '../../types'
import { formatCurrency, formatDate } from '../../utils/format'
import { useCart } from '../../hooks/useCart'
import { useAuth } from '../../hooks/useAuth'
import BookGrid from '../../components/book/BookGrid'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'

export default function BookDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { addToCart } = useCart()
  const { user } = useAuth()

  const [book, setBook] = useState<BookResponse | null>(null)
  const [related, setRelated] = useState<BookResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [adding, setAdding] = useState(false)
  const [added, setAdded] = useState(false)

  useEffect(() => {
    if (!id) return
    const bookId = Number(id)
    setLoading(true)
    setError('')
    Promise.all([getBook(bookId), getRelatedBooks(bookId)])
      .then(([b, rel]) => {
        setBook(b)
        setRelated(rel)
      })
      .catch(() => setError('Book not found.'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleAddToCart() {
    if (!user) { navigate('/login'); return }
    if (!book) return
    setAdding(true)
    try {
      await addToCart(book.id)
      setAdded(true)
      setTimeout(() => setAdded(false), 2000)
    } finally {
      setAdding(false)
    }
  }

  if (loading) return <LoadingSpinner />
  if (error) return (
    <div style={{ maxWidth: '800px', margin: '2rem auto', padding: '0 1.5rem' }}>
      <ErrorMessage message={error} />
    </div>
  )
  if (!book) return null

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <div style={{ display: 'grid', gridTemplateColumns: '200px 1fr', gap: '2rem', marginBottom: '3rem' }}>
        {/* Cover */}
        <div>
          {book.imageUrl ? (
            <img
              src={book.imageUrl}
              alt={book.title}
              style={{ width: '100%', borderRadius: '8px', boxShadow: '0 4px 16px rgba(0,0,0,0.1)' }}
            />
          ) : (
            <div
              style={{
                width: '100%',
                aspectRatio: '3/4',
                background: '#f3f4f6',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '4rem',
              }}
            >
              📖
            </div>
          )}
        </div>

        {/* Info */}
        <div>
          {book.category && (
            <span style={{ fontSize: '0.75rem', color: '#e67e22', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              {book.category.name}
            </span>
          )}
          <h1 style={{ fontSize: '1.8rem', fontWeight: 800, color: '#1a3a5c', margin: '0.25rem 0 0.5rem' }}>
            {book.title}
          </h1>
          <p style={{ color: '#57606a', margin: '0 0 0.5rem' }}>by {book.author}</p>
          {book.brand && (
            <p style={{ fontSize: '0.85rem', color: '#9ca3af', margin: '0 0 1rem' }}>
              Publisher: {book.brand.name}
            </p>
          )}
          {book.description && (
            <p style={{ color: '#1f2328', lineHeight: 1.7, margin: '0 0 1.5rem' }}>{book.description}</p>
          )}
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', margin: '0 0 1rem' }}>
            <span style={{ fontSize: '2rem', fontWeight: 800, color: '#1a3a5c' }}>
              {formatCurrency(book.price)}
            </span>
            {book.tentativeDeliveryDate && (
              <span style={{ fontSize: '0.8rem', color: '#57606a' }}>
                Est. delivery: {formatDate(book.tentativeDeliveryDate)}
              </span>
            )}
          </div>
          {book.stockQuantity <= 5 && book.stockQuantity > 0 && (
            <p style={{ color: '#d97706', fontSize: '0.85rem', margin: '0 0 1rem' }}>
              Only {book.stockQuantity} left in stock!
            </p>
          )}
          <button
            onClick={handleAddToCart}
            disabled={adding || !book.available}
            style={{
              padding: '0.75rem 2rem',
              background: added ? '#16a34a' : book.available ? '#e67e22' : '#9ca3af',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              fontSize: '1rem',
              fontWeight: 600,
              cursor: book.available ? 'pointer' : 'not-allowed',
              transition: 'background 0.15s',
            }}
          >
            {adding ? 'Adding…' : added ? '✓ Added to Cart' : !book.available ? 'Out of Stock' : 'Add to Cart'}
          </button>
          {book.isbn && (
            <p style={{ fontSize: '0.8rem', color: '#9ca3af', marginTop: '1rem' }}>ISBN: {book.isbn}</p>
          )}
        </div>
      </div>

      {/* Related Books */}
      {related.length > 0 && (
        <section>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#1a3a5c', marginBottom: '1rem' }}>
            Related Books
          </h2>
          <BookGrid books={related} />
        </section>
      )}
    </div>
  )
}
