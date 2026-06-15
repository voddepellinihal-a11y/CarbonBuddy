import { createContext, useContext, useState, useEffect } from 'react'
import { auth } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const name = localStorage.getItem('name')
    if (token && name) {
      setUser({ token, name, email: localStorage.getItem('email'), userId: localStorage.getItem('userId') })
    }
    setLoading(false)
  }, [])

  const login = async (email, password) => {
    const res = await auth.login({ email, password })
    localStorage.setItem('token', res.token)
    localStorage.setItem('name', res.name)
    localStorage.setItem('email', res.email)
    localStorage.setItem('userId', res.userId)
    setUser({ token: res.token, name: res.name, email: res.email, userId: res.userId })
    return res
  }

  const register = async (data) => {
    const res = await auth.register(data)
    localStorage.setItem('token', res.token)
    localStorage.setItem('name', res.name)
    localStorage.setItem('email', res.email)
    localStorage.setItem('userId', res.userId)
    setUser({ token: res.token, name: res.name, email: res.email, userId: res.userId })
    return res
  }

  const logout = () => {
    localStorage.clear()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
