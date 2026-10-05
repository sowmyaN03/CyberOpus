import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../../api/auth'
import { useAuth } from '../../hooks/useAuth'
import type { AxiosError } from 'axios'
import type { ErrorResponse } from '../../types'
import styles from './AuthPage.module.css'

export default function RegisterPage() {
  const { login: setAuth } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const auth = await register({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
        phone: form.phone || undefined,
      })
      setAuth(auth.accessToken, auth.user)
      navigate('/', { replace: true })
    } catch (err) {
      const axiosErr = err as AxiosError<ErrorResponse>
      const fieldErrors = axiosErr.response?.data?.errors
      if (fieldErrors && fieldErrors.length > 0) {
        setError(fieldErrors.map((fe) => `${fe.field}: ${fe.message}`).join('; '))
      } else {
        setError(axiosErr.response?.data?.message ?? 'Registration failed.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>Create Account</h1>
        <p className={styles.subtitle}>Join CyberOpus today</p>
        {error && <div className={styles.error}>{error}</div>}
        <form onSubmit={handleSubmit} className={styles.form}>
          <label className={styles.label}>First Name
            <input name="firstName" type="text" className={styles.input} value={form.firstName} onChange={handleChange} required />
          </label>
          <label className={styles.label}>Last Name
            <input name="lastName" type="text" className={styles.input} value={form.lastName} onChange={handleChange} required />
          </label>
          <label className={styles.label}>Email
            <input name="email" type="email" className={styles.input} value={form.email} onChange={handleChange} required autoComplete="email" />
          </label>
          <label className={styles.label}>Password
            <input name="password" type="password" className={styles.input} value={form.password} onChange={handleChange} required autoComplete="new-password" />
          </label>
          <label className={styles.label}>Phone (optional)
            <input name="phone" type="tel" className={styles.input} value={form.phone} onChange={handleChange} />
          </label>
          <button type="submit" className={styles.btn} disabled={loading}>
            {loading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>
        <p className={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" className={styles.link}>Sign In</Link>
        </p>
      </div>
    </div>
  )
}
