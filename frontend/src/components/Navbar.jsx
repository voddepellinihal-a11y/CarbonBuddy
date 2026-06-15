import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const links = [
  { to: '/', label: 'Dashboard', icon: '📊' },
  { to: '/activities', label: 'Activities', icon: '🚗' },
  { to: '/utility-bills', label: 'Bills', icon: '💡' },
  { to: '/recommendations', label: 'Nudges', icon: '🎯' },
  { to: '/rewards', label: 'Store', icon: '🎁' },
  { to: '/leaderboard', label: 'Leaderboard', icon: '🏆' },
]

export default function Navbar() {
  const { user, logout } = useAuth()

  return (
    <nav className="navbar">
      <div className="nav-brand">
        <span className="brand-icon">🌱</span>
        <span className="brand-text">CarbonBuddy</span>
      </div>
      <div className="nav-links">
        {links.map(l => (
          <NavLink key={l.to} to={l.to} className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'} end={l.to === '/'}>
            <span>{l.icon}</span> {l.label}
          </NavLink>
        ))}
      </div>
      <div className="nav-user">
        <span className="user-name">{user?.name}</span>
        <button onClick={logout} className="btn-logout">Logout</button>
      </div>
    </nav>
  )
}
