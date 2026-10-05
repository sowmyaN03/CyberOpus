# CyberOpus Frontend

React + TypeScript + Vite frontend for the CyberOpus e-bookstore.

---

## Requirements

- Node.js 18+
- The CyberOpus Spring Boot backend running on port **8080**

---

## Setup

```bash
# From the workspace root (CyberOpus/)
cd frontend
npm install
```

---

## Environment

Copy `.env.example` to `.env` and adjust if needed:

```bash
cp .env.example .env
```

`.env.example`:
```
VITE_API_BASE_URL=http://localhost:8080
```

> The dev server proxies `/api` requests to `http://localhost:8080`, so the `VITE_API_BASE_URL` env var is only needed for direct API calls outside the proxy (e.g. production builds).

---

## Running

```bash
# Start the dev server (http://localhost:3000)
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

---

## Tests

```bash
# Run all tests
npm test

# Watch mode
npm run test:watch
```

Tests live in `src/test/` and cover:
- `utils/format.ts` — currency, date, status formatters
- `utils/auth.ts` — JWT token helpers
- `components/common/LoadingSpinner` — render test
- `components/book/BookCard` — render test
- `pages/auth/LoginPage` — form render test

---

## Seed Credentials

| Role     | Email                   | Password      |
|----------|-------------------------|---------------|
| CUSTOMER | alice@demo.com          | Password123!  |
| CUSTOMER | bob@demo.com            | Password123!  |
| ADMIN    | admin@cyberopus.com     | Admin123!     |

---

## Project Structure

```
src/
├── api/              Axios API modules (one per backend resource)
├── components/       Shared UI components
│   ├── admin/
│   ├── book/
│   ├── cart/
│   ├── common/
│   └── layout/
├── context/          AuthContext, CartContext
├── hooks/            useAuth, useCart
├── pages/            Page components organised by route group
│   ├── admin/
│   ├── addresses/
│   ├── auth/
│   ├── cart/
│   ├── catalogue/
│   ├── checkout/
│   ├── orders/
│   ├── recommendations/
│   └── rewards/
├── routes/           ProtectedRoute, AdminRoute
├── styles/           global.css
├── test/             Vitest + Testing Library tests
├── types/            TypeScript interfaces matching backend DTOs
└── utils/            format.ts, auth.ts (pure helpers)
```

---

## Key Business Rules

| Rule | Detail |
|------|--------|
| Auth | JWT stored in `localStorage` as `cyberopus_token`, sent as `Authorization: Bearer` |
| Test payment failure | Set cardholder name to `FAIL_TEST` |
| Reward earn rate | 10 pts per $1 spent |
| Reward redeem rate | 100 pts = $1 off |
| Max redemption | 20% of order subtotal |
| Cancel window | 48 h after placing (enforced by backend) |
| Buy Again | Adds item to cart — does not place a new order |

---

## Backend

The Spring Boot backend runs on port **8080** and exposes 40 REST endpoints under `/api`. See the main project `README.md` for backend setup instructions.
