import { Link } from 'react-router-dom'
import type { BookResponse } from '../../types'
import { formatCurrency } from '../../utils/format'
import { useCart } from '../../hooks/useCart'
import { useAuth } from '../../hooks/useAuth'
import { useState } from 'react'
import styles from './BookCard.module.css'

interface Props {
  book: BookResponse
}

export default function BookCard({ book }: Props) {
  const { addToCart } = useCart()
  const { user } = useAuth()
  const [adding, setAdding] = useState(false)

  async function handleAddToCart(e: React.MouseEvent) {
    e.preventDefault()
    if (!user) return
    setAdding(true)
    try {
      await addToCart(book.id)
    } finally {
      setAdding(false)
    }
  }

  return (
    <Link to={`/books/${book.id}`} className={styles.card}>
      <div className={styles.imageWrapper}>
        {book.imageUrl ? (
          <img src={book.imageUrl} alt={book.title} className={styles.image} />
        ) : (
          <div className={styles.imagePlaceholder}>📖</div>
        )}
      </div>
      <div className={styles.body}>
        <div className={styles.category}>{book.category?.name ?? 'General'}</div>
        <h3 className={styles.title}>{book.title}</h3>
        <p className={styles.author}>{book.author}</p>
        <div className={styles.footer}>
          <span className={styles.price}>{formatCurrency(book.price)}</span>
          {user && (
            <button
              className={styles.addBtn}
              onClick={handleAddToCart}
              disabled={adding || !book.available}
              aria-label={`Add ${book.title} to cart`}
            >
              {adding ? '...' : !book.available ? 'Out of Stock' : '+ Cart'}
            </button>
          )}
        </div>
      </div>
    </Link>
  )
}
