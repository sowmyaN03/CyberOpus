import { useState } from 'react'
import type { ReactNode } from 'react'

interface Column<T> {
  label: string
  render: (row: T) => ReactNode
  width?: string
}

interface Props<T> {
  columns: Column<T>[]
  rows: T[]
  onEdit?: (row: T) => void
  onDelete?: (row: T) => void
  keyFn: (row: T) => string | number
}

export default function AdminTable<T>({ columns, rows, onEdit, onDelete, keyFn }: Props<T>) {
  const [confirmId, setConfirmId] = useState<string | number | null>(null)

  return (
    <div style={{ overflowX: 'auto' }}>
      <table
        style={{
          width: '100%',
          borderCollapse: 'collapse',
          background: '#fff',
          fontSize: '0.875rem',
        }}
      >
        <thead>
          <tr style={{ background: '#f7f8fa', borderBottom: '2px solid #e5e7eb' }}>
            {columns.map((col, i) => (
              <th
                key={i}
                style={{
                  padding: '0.75rem 1rem',
                  textAlign: 'left',
                  fontWeight: 600,
                  color: '#1f2328',
                  width: col.width,
                }}
              >
                {col.label}
              </th>
            ))}
            {(onEdit || onDelete) && (
              <th style={{ padding: '0.75rem 1rem', textAlign: 'right', width: '120px' }}>
                Actions
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const id = keyFn(row)
            return (
              <tr
                key={id}
                style={{ borderBottom: '1px solid #e5e7eb' }}
              >
                {columns.map((col, i) => (
                  <td key={i} style={{ padding: '0.75rem 1rem', color: '#1f2328' }}>
                    {col.render(row)}
                  </td>
                ))}
                {(onEdit || onDelete) && (
                  <td style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>
                    {confirmId === id ? (
                      <span>
                        <button
                          onClick={() => {
                            onDelete?.(row)
                            setConfirmId(null)
                          }}
                          style={{
                            marginRight: '0.5rem',
                            background: '#dc2626',
                            color: '#fff',
                            border: 'none',
                            borderRadius: '4px',
                            padding: '0.25rem 0.6rem',
                            cursor: 'pointer',
                            fontSize: '0.8rem',
                          }}
                        >
                          Yes, delete
                        </button>
                        <button
                          onClick={() => setConfirmId(null)}
                          style={{
                            background: '#f7f8fa',
                            border: '1px solid #d1d5db',
                            borderRadius: '4px',
                            padding: '0.25rem 0.6rem',
                            cursor: 'pointer',
                            fontSize: '0.8rem',
                          }}
                        >
                          Cancel
                        </button>
                      </span>
                    ) : (
                      <>
                        {onEdit && (
                          <button
                            onClick={() => onEdit(row)}
                            style={{
                              marginRight: '0.5rem',
                              background: '#1a3a5c',
                              color: '#fff',
                              border: 'none',
                              borderRadius: '4px',
                              padding: '0.25rem 0.6rem',
                              cursor: 'pointer',
                              fontSize: '0.8rem',
                            }}
                          >
                            Edit
                          </button>
                        )}
                        {onDelete && (
                          <button
                            onClick={() => setConfirmId(id)}
                            style={{
                              background: '#fee2e2',
                              color: '#dc2626',
                              border: 'none',
                              borderRadius: '4px',
                              padding: '0.25rem 0.6rem',
                              cursor: 'pointer',
                              fontSize: '0.8rem',
                            }}
                          >
                            Delete
                          </button>
                        )}
                      </>
                    )}
                  </td>
                )}
              </tr>
            )
          })}
        </tbody>
      </table>
      {rows.length === 0 && (
        <p style={{ textAlign: 'center', padding: '2rem', color: '#57606a' }}>No records found.</p>
      )}
    </div>
  )
}
