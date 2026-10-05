export default function Footer() {
  return (
    <footer
      style={{
        background: '#1a3a5c',
        color: 'rgba(255,255,255,0.7)',
        textAlign: 'center',
        padding: '1.5rem',
        marginTop: 'auto',
        fontSize: '0.875rem',
      }}
    >
      © {new Date().getFullYear()} CyberOpus. All rights reserved.
    </footer>
  )
}
