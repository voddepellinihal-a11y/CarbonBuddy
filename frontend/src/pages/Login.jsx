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
      <div className="auth-card">
        <div className="auth-header">
          <span className="auth-icon">🌱</span>
          <h1>CarbonBuddy</h1>
          <p>Sign in to your account</p>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required placeholder="email@example.com" />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required placeholder="Enter password" />
          </div>
          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <div className="demo-section">
          <span className="demo-label">⚡ Demo Accounts</span>
          <div className="demo-grid">
            {DEMO_USERS.map(u => (
              <div key={u.email} className="demo-card" onClick={() => fillDemo(u)}>
                <div className="demo-name">{u.name}</div>
                <div className="demo-email">{u.email}</div>
                <div className="demo-meta">{u.level} · {u.points} 🪙</div>
              </div>
            ))}
          </div>
        </div>
        <p className="auth-footer">
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  )
}
