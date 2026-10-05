interface Props {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}

export default function Pagination({ currentPage, totalPages, onPageChange }: Props) {
  if (totalPages <= 1) return null

  const pages = Array.from({ length: totalPages }, (_, i) => i)

  return (
    <div
      style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center', padding: '1.5rem 0' }}
    >
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        style={{
          padding: '0.4rem 0.8rem',
          border: '1px solid #d1d5db',
          borderRadius: '4px',
          cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
          opacity: currentPage === 0 ? 0.5 : 1,
          background: '#fff',
        }}
      >
        ‹ Prev
      </button>
      {pages.map((p) => (
        <button
          key={p}
          onClick={() => onPageChange(p)}
          style={{
            padding: '0.4rem 0.8rem',
            border: '1px solid #d1d5db',
            borderRadius: '4px',
            cursor: 'pointer',
            background: p === currentPage ? '#1a3a5c' : '#fff',
            color: p === currentPage ? '#fff' : '#1f2328',
          }}
        >
          {p + 1}
        </button>
      ))}
      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
        style={{
          padding: '0.4rem 0.8rem',
          border: '1px solid #d1d5db',
          borderRadius: '4px',
          cursor: currentPage === totalPages - 1 ? 'not-allowed' : 'pointer',
          opacity: currentPage === totalPages - 1 ? 0.5 : 1,
          background: '#fff',
        }}
      >
        Next ›
      </button>
    </div>
  )
}
