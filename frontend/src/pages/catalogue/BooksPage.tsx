import { useEffect, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import { searchBooks } from '../../api/books'
import { getCategories } from '../../api/categories'
import { getBrands } from '../../api/brands'
import type { BookResponse, CategoryResponse, BrandResponse, PageResponse } from '../../types'
import BookGrid from '../../components/book/BookGrid'
import Pagination from '../../components/common/Pagination'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import ErrorMessage from '../../components/common/ErrorMessage'
import styles from './BooksPage.module.css'

export default function BooksPage() {
  const [searchParams, setSearchParams] = useSearchParams()

  const [page, setPage] = useState<PageResponse<BookResponse> | null>(null)
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [brands, setBrands] = useState<BrandResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const q = searchParams.get('q') ?? ''
  const categoryId = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined
  const brandId = searchParams.get('brandId') ? Number(searchParams.get('brandId')) : undefined
  const pageNum = Number(searchParams.get('page') ?? '0')

  const fetchBooks = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await searchBooks({ q: q || undefined, categoryId, brandId, page: pageNum, size: 20 })
      setPage(data)
    } catch {
      setError('Failed to load books.')
    } finally {
      setLoading(false)
    }
  }, [q, categoryId, brandId, pageNum])

  useEffect(() => {
    fetchBooks()
  }, [fetchBooks])

  useEffect(() => {
    Promise.all([getCategories(), getBrands()]).then(([cats, brs]) => {
      setCategories(cats)
      setBrands(brs)
    })
  }, [])

  function setParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams)
    if (value) {
      next.set(key, value)
    } else {
      next.delete(key)
    }
    next.delete('page')
    setSearchParams(next)
  }

  return (
    <div className={styles.layout}>
      {/* Sidebar */}
      <aside className={styles.sidebar}>
        <h3 className={styles.filterTitle}>Filters</h3>

        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Category</label>
          <select
            className={styles.select}
            value={categoryId ?? ''}
            onChange={(e) => setParam('categoryId', e.target.value)}
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Publisher</label>
          <select
            className={styles.select}
            value={brandId ?? ''}
            onChange={(e) => setParam('brandId', e.target.value)}
          >
            <option value="">All Publishers</option>
            {brands.map((b) => (
              <option key={b.id} value={b.id}>{b.name}</option>
            ))}
          </select>
        </div>

        <button
          className={styles.clearBtn}
          onClick={() => setSearchParams(new URLSearchParams())}
        >
          Clear Filters
        </button>
      </aside>

      {/* Main */}
      <main className={styles.main}>
        <div className={styles.searchBar}>
          <input
            type="search"
            placeholder="Search books by title, author…"
            className={styles.searchInput}
            defaultValue={q}
            onKeyDown={(e) => {
              if (e.key === 'Enter') setParam('q', (e.target as HTMLInputElement).value)
            }}
            onBlur={(e) => setParam('q', e.target.value)}
          />
          <span className={styles.searchHint}>Press Enter to search</span>
        </div>

        {page && (
          <p className={styles.resultCount}>
            {page.totalElements} book{page.totalElements !== 1 ? 's' : ''} found
          </p>
        )}

        {error && <ErrorMessage message={error} onRetry={fetchBooks} />}
        {loading ? (
          <LoadingSpinner />
        ) : (
          <>
            <BookGrid books={page?.content ?? []} />
            {page && (
              <Pagination
                currentPage={page.number}
                totalPages={page.totalPages}
                onPageChange={(p) => {
                  const next = new URLSearchParams(searchParams)
                  next.set('page', String(p))
                  setSearchParams(next)
                }}
              />
            )}
          </>
        )}
      </main>
    </div>
  )
}
