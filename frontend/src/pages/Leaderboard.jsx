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

  if (loading) return (
    <div className="page-center" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <span className="sr-only">Loading leaderboard...</span>
    </div>
  )

  const isUserFirst = data?.leaderboard?.[0]?.isMe

  return (
    <div className="page">
      <CelebrationConfetti active={isUserFirst} />

      <div className="page-header">
        <h1 id="leaderboard-heading">
          Leaderboard <span aria-hidden="true">🏆</span>
        </h1>
      </div>
      <p className="page-desc">
        {data?.totalUsers || 0} users racing to shrink their footprint.
        {isUserFirst ? ' You\'re #1 — Champion!' : ` You're rank #${data?.myRank || '?'}.`}
      </p>

      <div className="leaderboard-stats" role="list" aria-label="Your leaderboard statistics">
        <div className="stat-card stat-card-pop" role="listitem">
          <span className="stat-value">#{data?.myRank || '?'}</span>
          <span className="stat-label">Your Rank</span>
        </div>
        <div className="stat-card stat-card-pop" role="listitem" style={{ animationDelay: '0.1s' }}>
          <span className="stat-value">{data?.myPoints || 0}</span>
          <span className="stat-label">CarbonCoins</span>
        </div>
        <div className="stat-card stat-card-pop" role="listitem" style={{ animationDelay: '0.2s' }}>
          <span className="stat-value">{data?.totalUsers || 0}</span>
          <span className="stat-label">Total Users</span>
        </div>
      </div>

      <section className="card" aria-labelledby="champions-heading">
        <h2 id="champions-heading">Top Carbon Champions</h2>
        <ol className="leaderboard-list" aria-label="Leaderboard rankings">
          {data?.leaderboard?.map((entry, idx) => (
            <li
              key={entry.rank}
              className={`leaderboard-entry ${entry.isMe ? 'is-me' : ''} ${entry.rank === 1 ? 'rank-one' : ''}`}
              style={{ animationDelay: `${idx * 0.08}s` }}
              aria-label={`Rank ${entry.rank}: ${entry.name}${entry.isMe ? ' (You)' : ''}, ${entry.points.toLocaleString()} points, ${entry.streak} day streak`}
            >
              {entry.rank === 1 && <div className="crown-emoji" aria-hidden="true">👑</div>}
              <div className="lb-rank">
                {entry.rank <= 3
                  ? <span className="medal" aria-hidden="true">{MEDAL_EMOJI[entry.rank - 1]}</span>
                  : <span className="rank-num">#{entry.rank}</span>}
              </div>
              <div className="lb-avatar" aria-hidden="true">{entry.levelIcon}</div>
              <div className="lb-info">
                <span className="lb-name">
                  {entry.rank === 1 && <span className="crown-icon" aria-hidden="true">👑</span>}
                  {entry.name} {entry.isMe ? '(You)' : ''}
                </span>
                <span className="lb-level">{entry.level}</span>
              </div>
              <div className="lb-stats">
                <span className="lb-points">{entry.points.toLocaleString()} pts</span>
                <span className="lb-streak">
                  <span aria-hidden="true">🔥</span> {entry.streak}d streak
                </span>
              </div>
              {entry.rank === 1 && (
                <div className="rank-one-fruits" aria-hidden="true">
                  <span>🍎</span><span>🍊</span><span>🍋</span><span>🍇</span>
                </div>
              )}
            </li>
          ))}
        </ol>
      </section>
    </div>
  )
}
