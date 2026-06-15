import { useState } from 'react'
import { utilityBills } from '../api'

export default function UtilityBills() {
  const [form, setForm] = useState({ totalKwh: '', utilityType: 'electricity', billingStart: '', billingEnd: '', allocationCount: 1 })
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const perPerson = form.totalKwh && form.allocationCount ? (parseFloat(form.totalKwh) / parseInt(form.allocationCount)).toFixed(1) : '—'
  const carbonPerPerson = perPerson !== '—' ? (parseFloat(perPerson) * 0.82).toFixed(2) : '—'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const payload = {
        totalKwh: parseFloat(form.totalKwh),
        utilityType: form.utilityType,
        billingStart: form.billingStart,
        billingEnd: form.billingEnd,
        allocationCount: parseInt(form.allocationCount),
      }
      const res = await utilityBills.create(payload)
      setResult(res)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const update = (field, value) => setForm({ ...form, [field]: value })

  return (
    <div className="page">
      <h1>Utility Bills</h1>
      <p className="page-desc">Upload your electricity bill and split it among roommates (FR-3.x).</p>

      <div className="content-grid">
        <div className="card">
          <h2>Add Utility Bill</h2>
          {error && <div className="alert alert-error">{error}</div>}
          {result && <div className="alert alert-success">Bill processed! ID: {result.id}, Status: {result.status}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Utility Type</label>
              <select value={form.utilityType} onChange={e => update('utilityType', e.target.value)}>
                <option value="electricity">Electricity</option>
                <option value="gas">Gas</option>
                <option value="water">Water</option>
              </select>
            </div>
            <div className="form-group">
              <label>Total Consumption (kWh)</label>
              <input type="number" step="0.1" min="0" value={form.totalKwh} onChange={e => update('totalKwh', e.target.value)} required placeholder="e.g. 450" />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Billing Start</label>
                <input type="date" value={form.billingStart} onChange={e => update('billingStart', e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Billing End</label>
                <input type="date" value={form.billingEnd} onChange={e => update('billingEnd', e.target.value)} required />
              </div>
            </div>
            <div className="form-group">
              <label>Roommate Split (number of people)</label>
              <input type="number" min="1" max="20" value={form.allocationCount} onChange={e => update('allocationCount', e.target.value)} />
            </div>

            <div className="carbon-estimate">
              <div>Per person: <strong>{perPerson} kWh</strong></div>
              <div>Carbon per person: <strong>{carbonPerPerson} kg CO₂</strong></div>
            </div>
            <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
              {loading ? 'Processing...' : 'Submit Bill'}
            </button>
          </form>
        </div>

        <div className="card">
          <h2>How it works</h2>
          <ol className="info-list">
            <li>Enter the total kWh from your utility bill</li>
            <li>Set the billing period dates</li>
            <li>Split among roommates (FR-3.2)</li>
            <li>We calculate your share: <code>total / roommates × 0.82 kg/kWh</code></li>
            <li>For OCR scanning, POST to <code>/api/utility-bills</code> with scanned text</li>
          </ol>
        </div>
      </div>
    </div>
  )
}
