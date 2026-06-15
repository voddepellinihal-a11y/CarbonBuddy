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
      <div className="auth-card">
        <div className="auth-header">
          <span className="auth-icon">🌱</span>
          <h1>Join CarbonBuddy</h1>
          <p>Track your carbon footprint</p>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group"><label>Name</label><input type="text" value={form.name} onChange={e => update('name', e.target.value)} required placeholder="Rohan Sharma" /></div>
          <div className="form-group"><label>Email</label><input type="email" value={form.email} onChange={e => update('email', e.target.value)} required placeholder="rohan@hyderabad.college" /></div>
          <div className="form-group"><label>Password (min 8 chars)</label><input type="password" value={form.password} onChange={e => update('password', e.target.value)} required minLength={8} /></div>
          <div className="form-row">
            <div className="form-group"><label>Age</label><input type="number" value={form.age} onChange={e => update('age', e.target.value)} /></div>
            <div className="form-group"><label>City</label><input type="text" value={form.municipality} onChange={e => update('municipality', e.target.value)} placeholder="Hyderabad" /></div>
          </div>
          <div className="form-group"><label>Default Transit</label>
            <select value={form.defaultTransitMode} onChange={e => update('defaultTransitMode', e.target.value)}>
              <option value="METRO">Metro</option>
              <option value="BUS">Bus</option>
              <option value="CAR_PETROL">Car (Petrol)</option>
              <option value="SCOOTER_PETROL">Scooter (Petrol)</option>
              <option value="BIKE">Bicycle</option>
              <option value="WALK">Walk</option>
              <option value="RIDESHARE">Rideshare</option>
            </select>
          </div>
          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>
        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
