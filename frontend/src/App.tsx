import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import Navbar from './components/layout/Navbar'
import Footer from './components/layout/Footer'
import ProtectedRoute from './routes/ProtectedRoute'
import AdminRoute from './routes/AdminRoute'

// Public
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import HomePage from './pages/catalogue/HomePage'
import BooksPage from './pages/catalogue/BooksPage'
import BookDetailPage from './pages/catalogue/BookDetailPage'

// Authenticated
import CartPage from './pages/cart/CartPage'
import AddressesPage from './pages/addresses/AddressesPage'
import CheckoutPage from './pages/checkout/CheckoutPage'
import OrdersPage from './pages/orders/OrdersPage'
import OrderDetailPage from './pages/orders/OrderDetailPage'
import RewardsPage from './pages/rewards/RewardsPage'
import RecommendationsPage from './pages/recommendations/RecommendationsPage'

// Admin
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminBooksPage from './pages/admin/AdminBooksPage'
import AdminCategoriesPage from './pages/admin/AdminCategoriesPage'
import AdminBrandsPage from './pages/admin/AdminBrandsPage'

import './styles/global.css'

function AppRoutes() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          {/* Public */}
          <Route path="/" element={<HomePage />} />
          <Route path="/books" element={<BooksPage />} />
          <Route path="/books/:id" element={<BookDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Authenticated */}
          <Route path="/cart" element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
          <Route path="/addresses" element={<ProtectedRoute><AddressesPage /></ProtectedRoute>} />
          <Route path="/checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
          <Route path="/orders/:id" element={<ProtectedRoute><OrderDetailPage /></ProtectedRoute>} />
          <Route path="/rewards" element={<ProtectedRoute><RewardsPage /></ProtectedRoute>} />
          <Route path="/recommendations" element={<ProtectedRoute><RecommendationsPage /></ProtectedRoute>} />

          {/* Admin */}
          <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
          <Route path="/admin/books" element={<AdminRoute><AdminBooksPage /></AdminRoute>} />
          <Route path="/admin/categories" element={<AdminRoute><AdminCategoriesPage /></AdminRoute>} />
          <Route path="/admin/brands" element={<AdminRoute><AdminBrandsPage /></AdminRoute>} />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <AppRoutes />
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
