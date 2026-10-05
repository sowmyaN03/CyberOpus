import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useCart } from '../../hooks/useCart'
import CartIcon from '../cart/CartIcon'
import styles from './Navbar.module.css'

export default function Navbar() {
  const { user, logout } = useAuth()
  const { itemCount } = useCart()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className={styles.nav}>
      <div className={styles.inner}>
        <Link to="/" className={styles.brand}>
          📚 CyberOpus
        </Link>
        <div className={styles.links}>
          <Link to="/books" className={styles.link}>
            Books
          </Link>
          {user && (
            <>
              <Link to="/recommendations" className={styles.link}>
                For You
              </Link>
              <Link to="/orders" className={styles.link}>
                Orders
              </Link>
              <Link to="/rewards" className={styles.link}>
                Rewards
              </Link>
            </>
          )}
          {user?.role === 'ADMIN' && (
            <Link to="/admin" className={styles.link}>
              Admin
            </Link>
          )}
        </div>
        <div className={styles.actions}>
          {user ? (
            <>
              <Link to="/cart" className={styles.cartBtn} aria-label="Cart">
                <CartIcon count={itemCount} />
              </Link>
              <Link to="/addresses" className={styles.link}>
                {user.firstName}
              </Link>
              <button className={styles.logoutBtn} onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.link}>
                Login
              </Link>
              <Link to="/register" className={styles.registerBtn}>
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}
