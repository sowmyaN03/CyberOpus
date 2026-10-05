import { describe, it, expect } from 'vitest'
import { formatCurrency, formatDate, formatOrderStatus, formatPaymentMethod, formatPoints } from '../../utils/format'

describe('formatCurrency', () => {
  it('formats positive amount', () => {
    expect(formatCurrency(10)).toBe('$10.00')
  })
  it('formats zero', () => {
    expect(formatCurrency(0)).toBe('$0.00')
  })
  it('formats decimal amount', () => {
    expect(formatCurrency(9.99)).toBe('$9.99')
  })
})

describe('formatOrderStatus', () => {
  it('capitalizes first letter only', () => {
    expect(formatOrderStatus('PENDING')).toBe('Pending')
    expect(formatOrderStatus('DELIVERED')).toBe('Delivered')
  })
})

describe('formatPaymentMethod', () => {
  it('formats CREDIT_CARD', () => {
    expect(formatPaymentMethod('CREDIT_CARD')).toBe('Credit Card')
  })
  it('formats DEBIT_CARD', () => {
    expect(formatPaymentMethod('DEBIT_CARD')).toBe('Debit Card')
  })
})

describe('formatPoints', () => {
  it('appends pts suffix', () => {
    expect(formatPoints(500)).toBe('500 pts')
  })
  it('formats large number', () => {
    expect(formatPoints(1000)).toBe('1,000 pts')
  })
})

describe('formatDate', () => {
  it('returns a non-empty string for valid date', () => {
    const result = formatDate('2024-01-15T00:00:00Z')
    expect(result.length).toBeGreaterThan(0)
    expect(result).toContain('2024')
  })
})
