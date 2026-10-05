import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import LoginPage from '../../pages/auth/LoginPage'

// Minimal mocks to isolate the component
vi.mock('../../api/auth', () => ({ login: vi.fn() }))
vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({ login: vi.fn() }),
}))

function renderPage() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  )
}

describe('LoginPage', () => {
  it('renders heading', () => {
    renderPage()
    expect(screen.getByRole('heading', { name: /sign in/i })).toBeInTheDocument()
  })

  it('renders email input', () => {
    renderPage()
    expect(screen.getByRole('textbox', { name: /email/i })).toBeInTheDocument()
  })

  it('renders submit button', () => {
    renderPage()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('renders link to register page', () => {
    renderPage()
    expect(screen.getByRole('link', { name: /register/i })).toBeInTheDocument()
  })
})
