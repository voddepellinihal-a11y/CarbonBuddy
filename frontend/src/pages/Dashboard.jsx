import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { analytics, recommendations } from '../api'
import CarbonChart from '../components/CarbonChart'
import TreeAnimation from '../components/TreeAnimation'

export default function Dashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [recoLoading, setRecoLoading] = useState(false)
  const navigate = useNavigate()

  const load = async () => {
    try {
      setLoading(true)
      const d = await analytics.dashboard()
      setData(d)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleGenerate = async () => {
    setRecoLoading(true)
    try {
      await recommendations.generate()
      await load()
    } catch (err) {
      console.error(err)
    } finally {
      setRecoLoading(false)
    }
  }

  if (loading) return <div className="page-center"><div className="spinner" /></div>

  const streak = data?.streak
  const level = data?.level
  const fomo = data?.fomo

  return (
    <div className="dashboard">
      <div className="page-header">
        <h1>Dashboard</h1>
        <button className="btn btn-outline" onClick={handleGenerate} disabled={recoLoading}>
          {recoLoading ? 'Generating...' : 'Generate Nudges'}
        </button>
      </div>

      {/* === GAMIFICATION BANNER === */}
      <div className="gamification-banner">
        <div className="gami-tree-section">
          <TreeAnimation level={level?.level || 1} points={level?.points || 0} isChampion={level?.level >= 5} />
        </div>
        <div className="gami-stats-section">
          <div className="gami-streak">
            <span className="streak-fire">{streak?.currentStreak >= 3 ? '🔥' : streak?.currentStreak >= 1 ? '✨' : '🌱'}</span>
            <div className="streak-info">
              <span className="streak-count">{streak?.currentStreak || 0} day streak</span>
              <span className="streak-label">{streak?.label || 'Start Today!'}</span>
              <span className="streak-multiplier">{streak?.pointsMultiplier?.toFixed(1)}x point multiplier</span>
            </div>
          </div>
          <div className="gami-divider" />
          <div className="gami-level">
            <span className="level-icon">{level?.icon || '🌱'}</span>
            <div className="level-info">
              <span className="level-title">Level {level?.level || 1} — {level?.title || 'Eco Seedling'}</span>
              <div className="level-bar-track">
                <div className="level-bar-fill" style={{ width: `${level?.pointsToNext > 0 ? Math.min(100 - (level.pointsToNext / Math.max(level.points + level.pointsToNext, 1)) * 100, 100) : 100}%` }} />
              </div>
              <span className="level-points">{level?.points || 0} CarbonCoins</span>
            </div>
          </div>
          <div className="gami-divider" />
          <div className="gami-percentile">
            <span className="percentile-badge">Top {level?.percentile || 100}%</span>
            <span className="percentile-label">of {fomo?.totalUsers || 0} users</span>
          </div>
        </div>
      </div>

      {/* === FOMO BAR === */}
      {fomo && (
        <div className="fomo-bar">
          <span>👥 {fomo.usersActiveToday} users active today</span>
          <span>📊 {fomo.totalUsers} total CarbonBuddy users</span>
          {fomo.streakDanger && <span className="fomo-danger">{fomo.streakDanger}</span>}
        </div>
      )}

      <div className="metric-grid">
        <MetricCard title="Daily CO₂" value={`${data?.daily?.totalCarbonKg?.toFixed(2) || '0'} kg`} subtitle={data?.daily?.totalCarbonKg > 0 ? `${(data.daily.totalCarbonKg * 10).toFixed(0)} pts earned` : ''} color="var(--green-500)" />
        <MetricCard title="Weekly CO₂" value={`${data?.weekly?.totalCarbonKg?.toFixed(2) || '0'} kg`} subtitle={data?.weekly?.changePercent ? `${data.weekly.changePercent > 0 ? '↑' : '↓'} ${Math.abs(data.weekly.changePercent).toFixed(1)}% vs last week` : ''} color="var(--blue-500)" />
        <MetricCard title="Monthly CO₂" value={`${data?.monthly?.totalCarbonKg?.toFixed(2) || '0'} kg`} subtitle="Tracked this month" color="var(--amber-500)" />
        <MetricCard title="CarbonCoins" value={`${data?.level?.points || 0}`} subtitle={`Level ${level?.level || 1} — ${level?.title || 'Seedling'}`} color="var(--green-600)" />
      </div>

      <div className="dashboard-grid">
        <div className="card">
          <h2>Carbon by Category</h2>
          {data?.breakdown?.length > 0 ? <CarbonChart data={data.breakdown} /> : <p className="empty-state">No data yet. Log activities to see breakdown.</p>}
        </div>

        <div className="card">
          <h2>Benchmark</h2>
          {data?.benchmarks?.map((b, i) => (
            <div key={i} className="benchmark-row">
              <div className="benchmark-label">{b.label}</div>
              <div className="benchmark-bars">
                <div className="benchmark-item">
                  <span className="benchmark-value">You: {b.userValue.toFixed(2)} kg</span>
                  <div className="bar-track"><div className="bar-fill user" style={{ width: `${Math.min((b.userValue / Math.max(b.userValue, b.averageValue, 1)) * 100, 100)}%` }} /></div>
                </div>
                <div className="benchmark-item">
                  <span className="benchmark-value">Avg: {b.averageValue.toFixed(2)} kg</span>
                  <div className="bar-track"><div className="bar-fill avg" style={{ width: `${Math.min((b.averageValue / Math.max(b.userValue, b.averageValue, 1)) * 100, 100)}%` }} /></div>
                </div>
              </div>
            </div>
          )) || <p className="empty-state">No benchmark data</p>}
        </div>
      </div>

      <div className="card">
        <div className="card-header-row">
          <h2>Active Nudges</h2>
          <button className="btn btn-link" onClick={() => navigate('/recommendations')}>View all →</button>
        </div>
        {data?.recommendations?.length > 0 ? (
          <div className="reco-mini-list">
            {data.recommendations.slice(0, 3).map(r => (
              <div key={r.id} className="reco-mini-item">
                <div className="reco-mini-icon">🎯</div>
                <div className="reco-mini-body">
                  <div className="reco-mini-title">{r.title}</div>
                  <div className="reco-mini-meta">Save {r.estimatedSavingsKg} kg CO₂ · {r.completionCount > 0 && `${r.completionCount} people completed this`}</div>
                </div>
                <span className={`badge ${r.status === 'COMPLETED' ? 'badge-success' : 'badge-pending'}`}>{r.status}</span>
              </div>
            ))}
          </div>
        ) : <p className="empty-state">No active nudges. Click "Generate Nudges" above.</p>}
      </div>
    </div>
  )
}

function MetricCard({ title, value, subtitle, color }) {
  return (
    <div className="metric-card" style={{ borderTopColor: color }}>
      <div className="metric-title">{title}</div>
      <div className="metric-value">{value}</div>
      {subtitle && <div className="metric-subtitle">{subtitle}</div>}
    </div>
  )
}
