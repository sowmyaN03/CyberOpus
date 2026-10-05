interface Props {
  count: number
}

export default function CartIcon({ count }: Props) {
  return (
    <span style={{ position: 'relative', fontSize: '1.3rem' }}>
      🛒
      {count > 0 && (
        <span
          style={{
            position: 'absolute',
            top: '-6px',
            right: '-8px',
            background: '#e67e22',
            color: '#fff',
            borderRadius: '50%',
            fontSize: '0.65rem',
            minWidth: '16px',
            height: '16px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
          }}
        >
          {count > 99 ? '99+' : count}
        </span>
      )}
    </span>
  )
}
