import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const DEMO_USERS = [
  { email: 'rohan@hyderabad.college', password: 'Rohan123!', name: 'Rohan Sharma', points: 450, level: 'Seedling' },
  { email: 'priya@techcorp.com', password: 'Priya123!', name: 'Priya Singh', points: 1280, level: 'Sprout' },
  { email: 'arjun@green.org', password: 'Arjun123!', name: 'Arjun Patel', points: 2100, level: 'Tree' },
  { email: 'neha@eco.in', password: 'Neha123!', name: 'Neha Gupta', points: 5600, level: 'Guardian' },
]

export default function Login() {
  const { login } = useAuth()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(form.email, form.password)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const fillDemo = (u) => setForm({ email: u.email, password: u.password })

  return (
    <div className="auth-page">
      <article className="auth-card" aria-labelledby="login-heading">
        <header className="auth-header">
          <span className="auth-icon" aria-hidden="true">🌱</span>
          <h1 id="login-heading">CarbonBuddy</h1>
          <p>Sign in to your account</p>
        </header>
        {error && (
          <div className="alert alert-error" role="alert" aria-live="assertive">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="login-email">Email</label>
            <input
              id="login-email"
              type="email"
              value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })}
              required
              placeholder="email@example.com"
              autoComplete="email"
              aria-describedby={error ? 'login-error' : undefined}
            />
          </div>
          <div className="form-group">
            <label htmlFor="login-password">Password</label>
            <input
              id="login-password"
              type="password"
              value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              required
              placeholder="Enter password"
              autoComplete="current-password"
              aria-describedby={error ? 'login-error' : undefined}
            />
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-full"
            disabled={loading}
            aria-busy={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <section className="demo-section" aria-labelledby="demo-label">
          <span className="demo-label" id="demo-label">Demo Accounts</span>
          <div className="demo-grid" role="list">
            {DEMO_USERS.map(u => (
              <button
                key={u.email}
                className="demo-card"
                onClick={() => fillDemo(u)}
                type="button"
                role="listitem"
                aria-label={`Use demo account: ${u.name}, ${u.level} level with ${u.points} points`}
              >
                <div className="demo-name">{u.name}</div>
                <div className="demo-email">{u.email}</div>
                <div className="demo-meta">{u.level} · {u.points} points</div>
              </button>
            ))}
          </div>
        </section>
        <footer className="auth-footer">
          Don't have an account? <Link to="/register" aria-label="Go to registration page">Register</Link>
        </footer>
      </article>
    </div>
  )
}
