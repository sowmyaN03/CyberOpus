import { Link } from 'react-router-dom'

export default function AdminDashboard() {
  const cards = [
    { to: '/admin/books', label: 'Books', icon: '📚', desc: 'Manage book catalogue' },
    { to: '/admin/categories', label: 'Categories', icon: '🗂️', desc: 'Manage book categories' },
    { to: '/admin/brands', label: 'Publishers', icon: '🏢', desc: 'Manage brand/publishers' },
  ]

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      <h1 style={{ color: '#1a3a5c', marginBottom: '0.5rem' }}>Admin Dashboard</h1>
      <p style={{ color: '#57606a', marginBottom: '2rem' }}>Manage the CyberOpus catalogue.</p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '1.25rem' }}>
        {cards.map((card) => (
          <Link
            key={card.to}
            to={card.to}
            style={{ display: 'block', background: '#fff', border: '1px solid #e5e7eb', borderRadius: '10px', padding: '1.5rem', textDecoration: 'none', color: 'inherit', transition: 'box-shadow 0.15s' }}
          >
            <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>{card.icon}</div>
            <h3 style={{ margin: '0 0 0.25rem', color: '#1a3a5c' }}>{card.label}</h3>
            <p style={{ margin: 0, color: '#57606a', fontSize: '0.875rem' }}>{card.desc}</p>
          </Link>
        ))}
      </div>
    </div>
  )
}
