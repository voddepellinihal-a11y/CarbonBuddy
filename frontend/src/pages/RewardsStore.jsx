import { useState, useEffect, useRef, useCallback } from 'react'
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
  const modalRef = useRef(null)
  const closeButtonRef = useRef(null)
  const previousFocusRef = useRef(null)

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
    previousFocusRef.current = document.activeElement
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

  const dismissResult = useCallback(() => {
    setResult(null)
    if (previousFocusRef.current) {
      previousFocusRef.current.focus()
    }
  }, [])

  useEffect(() => {
    if (result && closeButtonRef.current) {
      closeButtonRef.current.focus()
    }
  }, [result])

  useEffect(() => {
    if (!result) return
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        dismissResult()
      }
      if (e.key === 'Tab' && modalRef.current) {
        const focusable = modalRef.current.querySelectorAll(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        )
        const first = focusable[0]
        const last = focusable[focusable.length - 1]
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault()
          last.focus()
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault()
          first.focus()
        }
      }
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [result, dismissResult])

  const handleTabClick = (newTab) => {
    setTab(newTab)
  }

  const handleTabKeyDown = (e) => {
    const tabs = ['store', 'history']
    const currentIndex = tabs.indexOf(tab)
    let newIndex = currentIndex
    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
      e.preventDefault()
      newIndex = (currentIndex + 1) % tabs.length
    } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
      e.preventDefault()
      newIndex = (currentIndex - 1 + tabs.length) % tabs.length
    } else if (e.key === 'Home') {
      e.preventDefault()
      newIndex = 0
    } else if (e.key === 'End') {
      e.preventDefault()
      newIndex = tabs.length - 1
    }
    if (newIndex !== currentIndex) {
      handleTabClick(tabs[newIndex])
      document.getElementById(`tab-${tabs[newIndex]}`)?.focus()
    }
  }

  if (loading) return (
    <div className="page-center" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <span className="sr-only">Loading rewards store...</span>
    </div>
  )

  return (
    <div className="page">
      <div className="page-header">
        <h1 id="rewards-heading">
          Rewards Store <span aria-hidden="true">🎁</span>
        </h1>
        <div className="store-balance-badge" aria-label={`Your balance: ${balance} CarbonCoins`}>
          <span className="balance-icon" aria-hidden="true">🪙</span>
          <span className="balance-amount">{balance}</span>
          <span className="balance-label">CarbonCoins</span>
        </div>
      </div>
      <p className="page-desc">Redeem your CarbonCoins for eco-friendly rewards. Every coin earned = 1 kg CO₂ you helped save!</p>

      {result && (
        <div
          className="modal-overlay"
          onClick={dismissResult}
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-title"
          aria-describedby="modal-desc"
          ref={modalRef}
          tabIndex="-1"
        >
          <div className="modal-card" onClick={e => e.stopPropagation()}>
            {result.error ? (
              <>
                <div className="modal-icon" aria-hidden="true">😅</div>
                <h2 id="modal-title">Oops!</h2>
                <p id="modal-desc">{result.error}</p>
                <button
                  className="btn btn-primary btn-full"
                  onClick={dismissResult}
                  ref={closeButtonRef}
                  aria-label="Close error dialog"
                >
                  OK
                </button>
              </>
            ) : (
              <>
                <div className="modal-icon celebration" aria-hidden="true">{result.icon}</div>
                <h2 id="modal-title">Congratulations!</h2>
                <p id="modal-desc" className="modal-item-name">You redeemed <strong>{result.item}</strong></p>
                <div className="modal-code-section">
                  <span className="modal-code-label">Your coupon code:</span>
                  <div className="modal-code" aria-label={`Coupon code: ${result.redemptionCode}`}>{result.redemptionCode}</div>
                  <span className="modal-code-hint">Show this code at checkout</span>
                </div>
                <div className="modal-balance">Remaining balance: <strong>{result.newBalance} 🪙</strong></div>
                <button
                  className="btn btn-primary btn-full"
                  onClick={dismissResult}
                  ref={closeButtonRef}
                  aria-label="Close congratulations dialog"
                >
                  Awesome!
                </button>
              </>
            )}
          </div>
        </div>
      )}

      <div className="store-tabs" role="tablist" aria-label="Store sections" onKeyDown={handleTabKeyDown}>
        <button
          id="tab-store"
          className="store-tab"
          role="tab"
          aria-selected={tab === 'store'}
          aria-controls="tabpanel-store"
          tabIndex={tab === 'store' ? 0 : -1}
          onClick={() => handleTabClick('store')}
        >
          <span aria-hidden="true">🛍️</span> Store
        </button>
        <button
          id="tab-history"
          className="store-tab"
          role="tab"
          aria-selected={tab === 'history'}
          aria-controls="tabpanel-history"
          tabIndex={tab === 'history' ? 0 : -1}
          onClick={() => handleTabClick('history')}
        >
          <span aria-hidden="true">📜</span> History
        </button>
      </div>

      {tab === 'store' && (
        <div
          id="tabpanel-store"
          role="tabpanel"
          aria-labelledby="tab-store"
          tabIndex="0"
        >
          <div className="store-grid" role="list" aria-label="Available rewards">
            {items.map((item, idx) => (
              <article
                key={item.id}
                className={`store-card card ${!item.affordable ? 'store-card-locked' : ''}`}
                style={{ animationDelay: `${idx * 0.08}s` }}
                role="listitem"
                aria-label={`${item.name}: ${item.description}, costs ${item.cost} CarbonCoins, ${item.affordable ? 'affordable' : 'need more coins'}`}
              >
                <div className="store-icon" aria-hidden="true">{item.icon}</div>
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
                  aria-busy={redeeming === item.id}
                  aria-label={redeeming === item.id ? `Redeeming ${item.name}` : item.affordable ? `Redeem ${item.name} for ${item.cost} CarbonCoins` : `Cannot redeem ${item.name}, need more coins`}
                >
                  {redeeming === item.id ? 'Redeeming...' : item.affordable ? 'Redeem' : 'Need More Coins'}
                </button>
              </article>
            ))}
          </div>
        </div>
      )}

      {tab === 'history' && (
        <div
          id="tabpanel-history"
          role="tabpanel"
          aria-labelledby="tab-history"
          tabIndex="0"
        >
          <section className="card" aria-labelledby="history-heading">
            <h2 id="history-heading">Redemption History</h2>
            {history.length === 0 ? (
              <p className="empty-state">No redemptions yet. Start earning CarbonCoins by logging activities and completing nudges!</p>
            ) : (
              <div className="history-list" role="list" aria-label="Redemption history">
                {history.map(h => (
                  <div key={h.id} className="history-item" role="listitem" aria-label={`${h.item}, redeemed on ${new Date(h.date).toLocaleDateString()}, cost ${h.points} CarbonCoins`}>
                    <span className="history-icon" aria-hidden="true">🎁</span>
                    <div className="history-info">
                      <span className="history-item-name">{h.item}</span>
                      <span className="history-date">{new Date(h.date).toLocaleDateString()}</span>
                    </div>
                    <span className="history-cost">-{h.points} 🪙</span>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>
      )}
    </div>
  )
}
