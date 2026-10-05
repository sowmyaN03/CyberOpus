import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import BookCard from '../../components/book/BookCard'
import type { BookResponse } from '../../types'

// Mock hooks
vi.mock('../../hooks/useCart', () => ({
  useCart: () => ({ addToCart: vi.fn() }),
}))

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({ user: null }),
}))

const book: BookResponse = {
  id: 1,
  title: 'Test Book',
  author: 'Test Author',
  price: 19.99,
  stockQuantity: 5,
  available: true,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
}

function renderCard() {
  return render(
    <MemoryRouter>
      <BookCard book={book} />
    </MemoryRouter>,
  )
}

describe('BookCard', () => {
  it('renders book title', () => {
    renderCard()
    expect(screen.getByText('Test Book')).toBeInTheDocument()
  })

  it('renders book author', () => {
    renderCard()
    expect(screen.getByText('Test Author')).toBeInTheDocument()
  })

  it('renders book price', () => {
    renderCard()
    expect(screen.getByText('$19.99')).toBeInTheDocument()
  })

  it('renders placeholder image when no imageUrl', () => {
    renderCard()
    expect(screen.getByText('📖')).toBeInTheDocument()
  })

  it('links to book detail page', () => {
    renderCard()
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('href', '/books/1')
  })
})
