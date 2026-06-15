import { useState, useEffect } from 'react'
import { recommendations } from '../api'
import { useNavigate } from 'react-router-dom'

export default function Recommendations() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [completing, setCompleting] = useState(null)
  const navigate = useNavigate()

  const load = async () => {
    try {
      setLoading(true)
      const res = await recommendations.list()
      setData(res)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleGenerate = async () => {
    setGenerating(true)
    try {
      await recommendations.generate()
      await load()
    } catch (err) {
      console.error(err)
    } finally {
      setGenerating(false)
    }
  }

  const handleComplete = async (id) => {
    setCompleting(id)
    try {
      await recommendations.complete(id)
      await load()
    } catch (err) {
      console.error(err)
    } finally {
      setCompleting(null)
    }
  }

  if (loading) return <div className="page-center"><div className="spinner" /></div>

  return (
    <div className="page">
      <div className="page-header">
        <h1>Nudges & Recommendations</h1>
        <button className="btn btn-primary" onClick={handleGenerate} disabled={generating}>
          {generating ? 'Generating...' : 'Generate New'}
        </button>
      </div>
      <p className="page-desc">Personalized behavioral recommendations based on your carbon profile (FR-6.x).</p>

      {data.length === 0 ? (
        <div className="card empty-card">
          <p>No recommendations yet. Click "Generate New" to analyze your carbon data and get personalized nudges.</p>
        </div>
      ) : (
        <div className="reco-grid">
          {data.map(r => (
            <div key={r.id} className={`reco-card card ${r.status === 'COMPLETED' ? 'completed' : ''}`}>
              <div className="reco-header">
                <span className="reco-icon">{r.category === 'TRANSPORT' ? '🚗' : r.category === 'FOOD' ? '🍽️' : r.category === 'UTILITY' ? '💡' : '🌱'}</span>
                <span className={`badge ${r.status === 'COMPLETED' ? 'badge-success' : 'badge-pending'}`}>{r.status}</span>
              </div>
              <h3>{r.title}</h3>
              <p>{r.description}</p>
              <div className="reco-metrics">
                <div className="reco-metric">
                  <span className="reco-metric-value">{r.estimatedSavingsKg} kg</span>
                  <span className="reco-metric-label">CO₂ savings</span>
                </div>
                {r.estimatedSavingsPercent && (
                  <div className="reco-metric">
                    <span className="reco-metric-value">{r.estimatedSavingsPercent}%</span>
                    <span className="reco-metric-label">Reduction</span>
                  </div>
                )}
              </div>
              {r.status === 'PENDING' && (
                <button className="btn btn-success btn-full" onClick={() => handleComplete(r.id)} disabled={completing === r.id}>
                  {completing === r.id ? 'Completing...' : 'Complete & Earn Credits'}
                </button>
              )}
              {r.status === 'COMPLETED' && r.completedAt && (
                <div className="reco-completed-date">Completed</div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
