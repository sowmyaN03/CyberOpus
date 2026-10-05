import type { BookResponse } from '../../types'
import BookCard from './BookCard'

interface Props {
  books: BookResponse[]
}

export default function BookGrid({ books }: Props) {
  if (books.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: '#57606a', padding: '2rem' }}>No books found.</p>
    )
  }

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
        gap: '1.25rem',
      }}
    >
      {books.map((book) => (
        <BookCard key={book.id} book={book} />
      ))}
    </div>
  )
}
