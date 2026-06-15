const API_BASE = '/api'

async function request(path, options = {}) {
  const token = localStorage.getItem('token')
  const headers = { 'Content-Type': 'application/json', ...options.headers }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers })
  if (res.status === 403) {
    localStorage.removeItem('token')
    window.location.href = '/login'
    throw new Error('Unauthorized')
  }
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }))
    throw new Error(err.error || err.message || 'Request failed')
  }
  return res.json()
}

export const auth = {
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
}

export const activities = {
  create: (data) => request('/activities', { method: 'POST', body: JSON.stringify(data) }),
}

export const utilityBills = {
  create: (data) => request('/utility-bills', { method: 'POST', body: JSON.stringify(data) }),
}

export const analytics = {
  dashboard: () => request('/analytics/dashboard'),
}

export const recommendations = {
  list: () => request('/recommendations'),
  generate: () => request('/recommendations/generate', { method: 'POST' }),
  complete: (id) => request(`/recommendations/${id}/complete`, { method: 'POST' }),
}

export const leaderboard = {
  get: () => request('/leaderboard'),
}
