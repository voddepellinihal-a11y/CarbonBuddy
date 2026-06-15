import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'

const API_BASE = '/api'

async function api(path, options = {}) {
  const token = localStorage.getItem('token')
  const headers = { 'Content-Type': 'application/json', ...options.headers }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(`${API_BASE}${path}`, { ...options, headers })
  if (!res.ok) { const e = await res.json().catch(() => ({ error: res.statusText })); throw new Error(e.error || e.message) }
  return res.json()
}

export default function RewardsStore() {
  const { user } = useAuth()
  const [items, setItems] = useState([])
  const [balance, setBalance] = useState(0)
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [redeeming, setRedeeming] = useState(null)
  const [result, setResult] = useState(null)
  const [tab, setTab] = useState('store')

  const load = async () => {
    setLoading(true)
    try {
      const [storeRes, histRes] = await Promise.all([api('/rewards/store'), api('/rewards/history')])
      setItems(storeRes.items)
      setBalance(storeRes.balance)
      setHistory(histRes)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleRedeem = async (itemId) => {
    setRedeeming(itemId)
    setResult(null)
    try {
      const res = await api(`/rewards/redeem/${itemId}`, { method: 'POST' })
      setResult(res)
      setBalance(res.newBalance)
      setItems(prev => prev.map(i => ({ ...i, affordable: res.newBalance >= i.cost })))
      const [histRes] = await Promise.all([api('/rewards/history')])
      setHistory(histRes)
    } catch (err) {
      setResult({ error: err.message })
    } finally {
      setRedeeming(null)
    }
  }

  const dismissResult = () => setResult(null)

  if (loading) return <div className="page-center"><div className="spinner" /></div>

  return (
    <div className="page">
      <div className="page-header">
        <h1>Rewards Store 🎁</h1>
        <div className="store-balance-badge">
          <span className="balance-icon">🪙</span>
          <span className="balance-amount">{balance}</span>
          <span className="balance-label">CarbonCoins</span>
        </div>
      </div>
      <p className="page-desc">Redeem your CarbonCoins for eco-friendly rewards. Every coin earned = 1 kg CO₂ you helped save!</p>

      {/* Success/Error Modal */}
      {result && (
        <div className="modal-overlay" onClick={dismissResult}>
          <div className="modal-card" onClick={e => e.stopPropagation()}>
            {result.error ? (
              <>
                <div className="modal-icon">😅</div>
                <h2>Oops!</h2>
                <p>{result.error}</p>
                <button className="btn btn-primary btn-full" onClick={dismissResult}>OK</button>
              </>
            ) : (
              <>
                <div className="modal-icon celebration">{result.icon}</div>
                <h2>🎉 Congratulations!</h2>
                <p className="modal-item-name">You redeemed <strong>{result.item}</strong></p>
                <div className="modal-code-section">
                  <span className="modal-code-label">Your coupon code:</span>
                  <div className="modal-code">{result.redemptionCode}</div>
                  <span className="modal-code-hint">Show this code at checkout</span>
                </div>
                <div className="modal-balance">Remaining balance: <strong>{result.newBalance} 🪙</strong></div>
                <button className="btn btn-primary btn-full" onClick={dismissResult}>Awesome!</button>
              </>
            )}
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="store-tabs">
        <button className={`store-tab ${tab === 'store' ? 'active' : ''}`} onClick={() => setTab('store')}>🛍️ Store</button>
        <button className={`store-tab ${tab === 'history' ? 'active' : ''}`} onClick={() => setTab('history')}>📜 History</button>
      </div>

      {tab === 'store' && (
        <div className="store-grid">
          {items.map((item, idx) => (
            <div key={item.id} className={`store-card card ${!item.affordable ? 'store-card-locked' : ''}`} style={{ animationDelay: `${idx * 0.08}s` }}>
              <div className="store-icon">{item.icon}</div>
              <h3>{item.name}</h3>
              <p>{item.description}</p>
              <div className="store-cost">
                <span className="cost-amount">{item.cost}</span>
                <span className="cost-label">CarbonCoins</span>
              </div>
              <button
                className={`btn ${item.affordable ? 'btn-success' : 'btn-outline'} btn-full`}
                disabled={!item.affordable || redeeming === item.id}
                onClick={() => handleRedeem(item.id)}
              >
                {redeeming === item.id ? 'Redeeming...' : item.affordable ? 'Redeem' : 'Need More Coins'}
              </button>
            </div>
          ))}
        </div>
      )}

      {tab === 'history' && (
        <div className="card">
          <h2>Redemption History</h2>
          {history.length === 0 ? (
            <p className="empty-state">No redemptions yet. Start earning CarbonCoins by logging activities and completing nudges!</p>
          ) : (
            <div className="history-list">
              {history.map(h => (
                <div key={h.id} className="history-item">
                  <span className="history-icon">🎁</span>
                  <div className="history-info">
                    <span className="history-item-name">{h.item}</span>
                    <span className="history-date">{new Date(h.date).toLocaleDateString()}</span>
                  </div>
                  <span className="history-cost">-{h.points} 🪙</span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
