import { useState, useEffect } from 'react'
import { leaderboard } from '../api'
import CelebrationConfetti from '../components/CelebrationConfetti'

const MEDAL_EMOJI = ['🥇', '🥈', '🥉']

export default function Leaderboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const res = await leaderboard.get()
        setData(res)
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return <div className="page-center"><div className="spinner" /></div>

  const isUserFirst = data?.leaderboard?.[0]?.isMe

  return (
    <div className="page">
      <CelebrationConfetti active={isUserFirst} />

      <div className="page-header">
        <h1>Leaderboard 🏆</h1>
      </div>
      <p className="page-desc">
        {data?.totalUsers || 0} users racing to shrink their footprint.
        {isUserFirst ? ' You\'re #1 — Champion! 🎉' : ` You're rank #${data?.myRank || '?'}.`}
      </p>

      <div className="leaderboard-stats">
        <div className="stat-card stat-card-pop">
          <span className="stat-value">#{data?.myRank || '?'}</span>
          <span className="stat-label">Your Rank</span>
        </div>
        <div className="stat-card stat-card-pop" style={{ animationDelay: '0.1s' }}>
          <span className="stat-value">{data?.myPoints || 0}</span>
          <span className="stat-label">CarbonCoins</span>
        </div>
        <div className="stat-card stat-card-pop" style={{ animationDelay: '0.2s' }}>
          <span className="stat-value">{data?.totalUsers || 0}</span>
          <span className="stat-label">Total Users</span>
        </div>
      </div>

      <div className="card">
        <h2>Top Carbon Champions</h2>
        <div className="leaderboard-list">
          {data?.leaderboard?.map((entry, idx) => (
            <div
              key={entry.rank}
              className={`leaderboard-entry ${entry.isMe ? 'is-me' : ''} ${entry.rank === 1 ? 'rank-one' : ''}`}
              style={{ animationDelay: `${idx * 0.08}s` }}
            >
              {entry.rank === 1 && <div className="crown-emoji">👑</div>}
              <div className="lb-rank">
                {entry.rank <= 3
                  ? <span className="medal">{MEDAL_EMOJI[entry.rank - 1]}</span>
                  : <span className="rank-num">#{entry.rank}</span>}
              </div>
              <div className="lb-avatar">{entry.levelIcon}</div>
              <div className="lb-info">
                <span className="lb-name">
                  {entry.rank === 1 && <span className="crown-icon">👑</span>}
                  {entry.name} {entry.isMe ? '(You)' : ''}
                </span>
                <span className="lb-level">{entry.level}</span>
              </div>
              <div className="lb-stats">
                <span className="lb-points">{entry.points.toLocaleString()} pts</span>
                <span className="lb-streak">🔥 {entry.streak}d streak</span>
              </div>
              {entry.rank === 1 && (
                <div className="rank-one-fruits">
                  <span>🍎</span><span>🍊</span><span>🍋</span><span>🍇</span>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
