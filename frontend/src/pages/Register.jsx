import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { register } = useAuth()
  const [form, setForm] = useState({ email: '', password: '', name: '', age: '', municipality: '', defaultTransitMode: 'METRO' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register({ ...form, age: parseInt(form.age) || 0 })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const update = (field, value) => setForm({ ...form, [field]: value })

  return (
    <div className="auth-page">
      <article className="auth-card" aria-labelledby="register-heading">
        <header className="auth-header">
          <span className="auth-icon" aria-hidden="true">🌱</span>
          <h1 id="register-heading">Join CarbonBuddy</h1>
          <p>Track your carbon footprint</p>
        </header>
        {error && (
          <div className="alert alert-error" role="alert" aria-live="assertive">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="reg-name">Name</label>
            <input
              id="reg-name"
              type="text"
              value={form.name}
              onChange={e => update('name', e.target.value)}
              required
              placeholder="Rohan Sharma"
              autoComplete="name"
            />
          </div>
          <div className="form-group">
            <label htmlFor="reg-email">Email</label>
            <input
              id="reg-email"
              type="email"
              value={form.email}
              onChange={e => update('email', e.target.value)}
              required
              placeholder="rohan@hyderabad.college"
              autoComplete="email"
            />
          </div>
          <div className="form-group">
            <label htmlFor="reg-password">Password (min 8 chars)</label>
            <input
              id="reg-password"
              type="password"
              value={form.password}
              onChange={e => update('password', e.target.value)}
              required
              minLength={8}
              autoComplete="new-password"
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="reg-age">Age</label>
              <input
                id="reg-age"
                type="number"
                value={form.age}
                onChange={e => update('age', e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="reg-city">City</label>
              <input
                id="reg-city"
                type="text"
                value={form.municipality}
                onChange={e => update('municipality', e.target.value)}
                placeholder="Hyderabad"
              />
            </div>
          </div>
          <div className="form-group">
            <label htmlFor="reg-transit">Default Transit</label>
            <select
              id="reg-transit"
              value={form.defaultTransitMode}
              onChange={e => update('defaultTransitMode', e.target.value)}
            >
              <option value="METRO">Metro</option>
              <option value="BUS">Bus</option>
              <option value="CAR_PETROL">Car (Petrol)</option>
              <option value="SCOOTER_PETROL">Scooter (Petrol)</option>
              <option value="BIKE">Bicycle</option>
              <option value="WALK">Walk</option>
              <option value="RIDESHARE">Rideshare</option>
            </select>
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-full"
            disabled={loading}
            aria-busy={loading}
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>
        <footer className="auth-footer">
          Already have an account? <Link to="/login" aria-label="Go to login page">Sign in</Link>
        </footer>
      </article>
    </div>
  )
}
