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
    <header>
      <nav className="navbar" role="navigation" aria-label="Main navigation">
        <div className="nav-brand">
          <span className="brand-icon" aria-hidden="true">🌱</span>
          <span className="brand-text">CarbonBuddy</span>
        </div>
        <ul className="nav-links" role="list">
          {links.map(l => (
            <li key={l.to} role="listitem">
              <NavLink
                to={l.to}
                className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}
                end={l.to === '/'}
                aria-current={({ isActive }) => isActive ? 'page' : undefined}
              >
                <span aria-hidden="true">{l.icon}</span> {l.label}
              </NavLink>
            </li>
          ))}
        </ul>
        <div className="nav-user">
          <span className="user-name" aria-label={`Logged in as ${user?.name}`}>{user?.name}</span>
          <button
            onClick={logout}
            className="btn-logout"
            aria-label="Log out of your account"
          >
            Logout
          </button>
        </div>
      </nav>
    </header>
  )
}
