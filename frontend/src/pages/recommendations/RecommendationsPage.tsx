import { useEffect, useState } from 'react'
import { getRecommendations } from '../../api/recommendations'
import type { BookResponse } from '../../types'
import BookGrid from '../../components/book/BookGrid'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'

export default function RecommendationsPage() {
  const [books, setBooks] = useState<BookResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function load() {
    setLoading(true)
    try {
      setBooks(await getRecommendations(12))
    } catch {
      setError('Failed to load recommendations.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <h1 style={{ color: '#1a3a5c', marginBottom: '0.5rem' }}>Recommended for You</h1>
      <p style={{ color: '#57606a', marginBottom: '1.5rem' }}>Books picked based on your reading history.</p>
      {error && <ErrorMessage message={error} onRetry={load} />}
      {loading ? <LoadingSpinner /> : <BookGrid books={books} />}
    </div>
  )
}
