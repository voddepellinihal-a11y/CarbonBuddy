import { useState } from 'react'
import { activities } from '../api'

const TRANSIT_OPTIONS = [
  { value: 'METRO', label: 'Metro', factor: 0.035 },
  { value: 'BUS', label: 'Bus', factor: 0.089 },
  { value: 'CAR_PETROL', label: 'Car (Petrol)', factor: 0.192 },
  { value: 'CAR_DIESEL', label: 'Car (Diesel)', factor: 0.171 },
  { value: 'SCOOTER_PETROL', label: 'Scooter (Petrol)', factor: 0.052 },
  { value: 'RIDESHARE', label: 'Rideshare', factor: 0.085 },
  { value: 'BIKE', label: 'Bicycle', factor: 0 },
  { value: 'WALK', label: 'Walk', factor: 0 },
  { value: 'AUTO', label: 'Auto Rickshaw', factor: 0.07 },
]

export default function Activities() {
  const [form, setForm] = useState({ distanceKm: '', transitMode: 'METRO', activityStart: '', isManual: false })
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const selectedFactor = TRANSIT_OPTIONS.find(t => t.value === form.transitMode)?.factor || 0
  const estimatedCarbon = form.distanceKm ? (parseFloat(form.distanceKm) * selectedFactor).toFixed(3) : '—'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const payload = {
        distanceKm: parseFloat(form.distanceKm),
        transitMode: form.transitMode,
        activityStart: form.activityStart || new Date().toISOString(),
        activityEnd: form.activityStart || new Date().toISOString(),
        isManual: form.isManual,
      }
      const res = await activities.create(payload)
      setResult(res)
      setForm({ distanceKm: '', transitMode: 'METRO', activityStart: '', isManual: false })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const update = (field, value) => setForm({ ...form, [field]: value })

  return (
    <div className="page">
      <h1 id="activities-heading">Log Activity</h1>
      <p className="page-desc">Record a transport trip to track your carbon footprint.</p>

      <div className="content-grid">
        <section className="card" aria-labelledby="new-trip-heading">
          <h2 id="new-trip-heading">New Trip</h2>
          {error && (
            <div className="alert alert-error" role="alert" aria-live="assertive">
              {error}
            </div>
          )}
          {result && (
            <div className="alert alert-success" role="status" aria-live="polite">
              Trip logged! ID: {result.id}
            </div>
          )}
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="activity-transit">Transit Mode</label>
              <select
                id="activity-transit"
                value={form.transitMode}
                onChange={e => update('transitMode', e.target.value)}
                aria-label="Select transit mode for your trip"
              >
                {TRANSIT_OPTIONS.map(t => (
                  <option key={t.value} value={t.value}>{t.label} ({t.factor} kg/km)</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="activity-distance">Distance (km)</label>
              <input
                id="activity-distance"
                type="number"
                step="0.1"
                min="0"
                value={form.distanceKm}
                onChange={e => update('distanceKm', e.target.value)}
                required
                placeholder="e.g. 12.5"
                aria-describedby="distance-estimate"
              />
            </div>
            <div className="form-group">
              <label htmlFor="activity-datetime">Date &amp; Time</label>
              <input
                id="activity-datetime"
                type="datetime-local"
                value={form.activityStart}
                onChange={e => update('activityStart', e.target.value)}
              />
            </div>
            <div className="form-group checkbox-group">
              <label htmlFor="activity-manual">
                <input
                  id="activity-manual"
                  type="checkbox"
                  checked={form.isManual}
                  onChange={e => update('isManual', e.target.checked)}
                />
                Manual entry (not auto-detected)
              </label>
            </div>
            <div className="carbon-estimate" id="distance-estimate" aria-live="polite">
              Estimated CO₂: <strong>{estimatedCarbon} kg</strong>
            </div>
            <button
              type="submit"
              className="btn btn-primary btn-full"
              disabled={loading}
              aria-busy={loading}
            >
              {loading ? 'Logging...' : 'Log Activity'}
            </button>
          </form>
        </section>

        <section className="card" aria-labelledby="factors-heading">
          <h2 id="factors-heading">Emission Factors</h2>
          <table className="factor-table" aria-label="Emission factors by transit mode">
            <thead>
              <tr>
                <th scope="col">Mode</th>
                <th scope="col">kg CO₂/km</th>
              </tr>
            </thead>
            <tbody>
              {TRANSIT_OPTIONS.map(t => (
                <tr key={t.value} className={t.factor === 0 ? 'zero-emission' : ''}>
                  <td>{t.label}</td>
                  <td>{t.factor}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </div>
  )
}
