interface Props {
  message: string
  onRetry?: () => void
}

export default function ErrorMessage({ message, onRetry }: Props) {
  return (
    <div
      style={{
        padding: '1rem',
        background: '#fef2f2',
        border: '1px solid #fecaca',
        borderRadius: '6px',
        color: '#b91c1c',
        margin: '1rem 0',
      }}
      role="alert"
    >
      <strong>Error:</strong> {message}
      {onRetry && (
        <button
          onClick={onRetry}
          style={{
            marginLeft: '1rem',
            padding: '0.25rem 0.75rem',
            background: '#1a3a5c',
            color: '#fff',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          Retry
        </button>
      )}
    </div>
  )
}
