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

  if (loading) return (
    <div className="page-center" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <span className="sr-only">Loading dashboard data...</span>
    </div>
  )

  const streak = data?.streak
  const level = data?.level
  const fomo = data?.fomo

  return (
    <section className="dashboard" aria-labelledby="dashboard-heading">
      <div className="page-header">
        <h1 id="dashboard-heading">Dashboard</h1>
        <button
          className="btn btn-outline"
          onClick={handleGenerate}
          disabled={recoLoading}
          aria-busy={recoLoading}
          aria-label={recoLoading ? 'Generating nudges, please wait' : 'Generate personalized nudges'}
        >
          {recoLoading ? 'Generating...' : 'Generate Nudges'}
        </button>
      </div>

      <section className="gamification-banner" aria-label="Your gamification progress">
        <div className="gami-tree-section">
          <TreeAnimation level={level?.level || 1} points={level?.points || 0} isChampion={level?.level >= 5} />
        </div>
        <div className="gami-stats-section">
          <div className="gami-streak" aria-label={`Current streak: ${streak?.currentStreak || 0} days`}>
            <span className="streak-fire" aria-hidden="true">{streak?.currentStreak >= 3 ? '🔥' : streak?.currentStreak >= 1 ? '✨' : '🌱'}</span>
            <div className="streak-info">
              <span className="streak-count">{streak?.currentStreak || 0} day streak</span>
              <span className="streak-label">{streak?.label || 'Start Today!'}</span>
              <span className="streak-multiplier">{streak?.pointsMultiplier?.toFixed(1)}x point multiplier</span>
            </div>
          </div>
          <div className="gami-divider" aria-hidden="true" />
          <div className="gami-level" aria-label={`Level ${level?.level || 1}: ${level?.title || 'Eco Seedling'}`}>
            <span className="level-icon" aria-hidden="true">{level?.icon || '🌱'}</span>
            <div className="level-info">
              <span className="level-title">Level {level?.level || 1} — {level?.title || 'Eco Seedling'}</span>
              <div className="level-bar-track" role="progressbar" aria-valuenow={level?.points || 0} aria-valuemin={0} aria-valuemax={(level?.points || 0) + (level?.pointsToNext || 0)} aria-label={`Level progress: ${level?.points || 0} of ${(level?.points || 0) + (level?.pointsToNext || 0)} CarbonCoins`}>
                <div className="level-bar-fill" style={{ width: `${level?.pointsToNext > 0 ? Math.min(100 - (level.pointsToNext / Math.max(level.points + level.pointsToNext, 1)) * 100, 100) : 100}%` }} />
              </div>
              <span className="level-points">{level?.points || 0} CarbonCoins</span>
            </div>
          </div>
          <div className="gami-divider" aria-hidden="true" />
          <div className="gami-percentile" aria-label={`You are in the top ${level?.percentile || 100}% of users`}>
            <span className="percentile-badge">Top {level?.percentile || 100}%</span>
            <span className="percentile-label">of {fomo?.totalUsers || 0} users</span>
          </div>
        </div>
      </section>

      {fomo && (
        <section className="fomo-bar" aria-label="Community activity status">
          <span aria-label={`${fomo.usersActiveToday} users active today`}>
            <span aria-hidden="true">👥</span> {fomo.usersActiveToday} users active today
          </span>
          <span aria-label={`${fomo.totalUsers} total CarbonBuddy users`}>
            <span aria-hidden="true">📊</span> {fomo.totalUsers} total CarbonBuddy users
          </span>
          {fomo.streakDanger && (
            <span className="fomo-danger" role="alert">{fomo.streakDanger}</span>
          )}
        </section>
      )}

      <div className="metric-grid" role="list" aria-label="Carbon emission metrics">
        <MetricCard title="Daily CO₂" value={`${data?.daily?.totalCarbonKg?.toFixed(2) || '0'} kg`} subtitle={data?.daily?.totalCarbonKg > 0 ? `${(data.daily.totalCarbonKg * 10).toFixed(0)} pts earned` : ''} color="var(--green-500)" />
        <MetricCard title="Weekly CO₂" value={`${data?.weekly?.totalCarbonKg?.toFixed(2) || '0'} kg`} subtitle={data?.weekly?.changePercent ? `${data.weekly.changePercent > 0 ? '↑' : '↓'} ${Math.abs(data.weekly.changePercent).toFixed(1)}% vs last week` : ''} color="var(--blue-500)" />
        <MetricCard title="Monthly CO₂" value={`${data?.monthly?.totalCarbonKg?.toFixed(2) || '0'} kg`} subtitle="Tracked this month" color="var(--amber-500)" />
        <MetricCard title="CarbonCoins" value={`${data?.level?.points || 0}`} subtitle={`Level ${level?.level || 1} — ${level?.title || 'Seedling'}`} color="var(--green-600)" />
      </div>

      <div className="dashboard-grid">
        <section className="card" aria-labelledby="chart-heading">
          <h2 id="chart-heading">Carbon by Category</h2>
          {data?.breakdown?.length > 0 ? (
            <CarbonChart data={data.breakdown} />
          ) : (
            <p className="empty-state">No data yet. Log activities to see breakdown.</p>
          )}
        </section>

        <section className="card" aria-labelledby="benchmark-heading">
          <h2 id="benchmark-heading">Benchmark</h2>
          {data?.benchmarks?.map((b, i) => (
            <div key={i} className="benchmark-row">
              <div className="benchmark-label">{b.label}</div>
              <div className="benchmark-bars">
                <div className="benchmark-item">
                  <span className="benchmark-value">You: {b.userValue.toFixed(2)} kg</span>
                  <div className="bar-track" role="img" aria-label={`Your ${b.label}: ${b.userValue.toFixed(2)} kg CO₂`}>
                    <div className="bar-fill user" style={{ width: `${Math.min((b.userValue / Math.max(b.userValue, b.averageValue, 1)) * 100, 100)}%` }} />
                  </div>
                </div>
                <div className="benchmark-item">
                  <span className="benchmark-value">Avg: {b.averageValue.toFixed(2)} kg</span>
                  <div className="bar-track" role="img" aria-label={`Average ${b.label}: ${b.averageValue.toFixed(2)} kg CO₂`}>
                    <div className="bar-fill avg" style={{ width: `${Math.min((b.averageValue / Math.max(b.userValue, b.averageValue, 1)) * 100, 100)}%` }} />
                  </div>
                </div>
              </div>
            </div>
          )) || <p className="empty-state">No benchmark data</p>}
        </section>
      </div>

      <section className="card" aria-labelledby="nudges-heading">
        <div className="card-header-row">
          <h2 id="nudges-heading">Active Nudges</h2>
          <button
            className="btn btn-link"
            onClick={() => navigate('/recommendations')}
            aria-label="View all nudges and recommendations"
          >
            View all →
          </button>
        </div>
        {data?.recommendations?.length > 0 ? (
          <div className="reco-mini-list" role="list">
            {data.recommendations.slice(0, 3).map(r => (
              <div key={r.id} className="reco-mini-item" role="listitem">
                <div className="reco-mini-icon" aria-hidden="true">🎯</div>
                <div className="reco-mini-body">
                  <div className="reco-mini-title">{r.title}</div>
                  <div className="reco-mini-meta">Save {r.estimatedSavingsKg} kg CO₂ · {r.completionCount > 0 && `${r.completionCount} people completed this`}</div>
                </div>
                <span
                  className={`badge ${r.status === 'COMPLETED' ? 'badge-success' : 'badge-pending'}`}
                  aria-label={`Status: ${r.status === 'COMPLETED' ? 'Completed' : 'Pending'}`}
                >
                  {r.status}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <p className="empty-state">No active nudges. Click "Generate Nudges" above.</p>
        )}
      </section>
    </section>
  )
}

function MetricCard({ title, value, subtitle, color }) {
  return (
    <article className="metric-card" style={{ borderTopColor: color }} role="listitem" aria-label={`${title}: ${value}${subtitle ? `, ${subtitle}` : ''}`}>
      <div className="metric-title">{title}</div>
      <div className="metric-value">{value}</div>
      {subtitle && <div className="metric-subtitle">{subtitle}</div>}
    </article>
  )
}
