import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getBooks } from '../../api/books'
import { getCategories } from '../../api/categories'
import type { BookResponse, CategoryResponse } from '../../types'
import BookGrid from '../../components/book/BookGrid'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import styles from './HomePage.module.css'

export default function HomePage() {
  const [books, setBooks] = useState<BookResponse[]>([])
  const [categories, setCategories] = useState<CategoryResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      getBooks({ size: 8, sort: 'createdAt,desc' }),
      getCategories(),
    ]).then(([booksPage, cats]) => {
      setBooks(booksPage.content)
      setCategories(cats)
    }).finally(() => setLoading(false))
  }, [])

  return (
    <div>
      {/* Hero */}
      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <h1 className={styles.heroTitle}>Your Next Great Read Awaits</h1>
          <p className={styles.heroSubtitle}>
            Explore thousands of e-books across every genre — delivered instantly.
          </p>
          <Link to="/books" className={styles.heroCta}>
            Browse Books
          </Link>
        </div>
      </section>

      <div className={styles.container}>
        {/* Categories */}
        {categories.length > 0 && (
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>Browse by Category</h2>
            <div className={styles.categoryChips}>
              {categories.map((cat) => (
                <Link key={cat.id} to={`/books?categoryId=${cat.id}`} className={styles.chip}>
                  {cat.name}
                </Link>
              ))}
            </div>
          </section>
        )}

        {/* Recent Books */}
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>Recently Added</h2>
            <Link to="/books" className={styles.viewAll}>View All →</Link>
          </div>
          {loading ? <LoadingSpinner /> : <BookGrid books={books} />}
        </section>
      </div>
    </div>
  )
}
