import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
    isActive ? 'bg-eco-600 text-white' : 'text-slate-600 hover:bg-eco-100'
  }`

export function AppShell() {
  const { user, signOut } = useAuth()

  return (
    <div className="min-h-screen">
      <header className="border-b border-eco-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-6">
            <span className="text-lg font-semibold text-eco-700">EcoVision</span>
            <nav className="flex gap-1">
              <NavLink to="/" end className={navLinkClass}>
                Dashboard
              </NavLink>
              <NavLink to="/settings" className={navLinkClass}>
                Settings
              </NavLink>
            </nav>
          </div>
          <div className="flex items-center gap-3 text-sm text-slate-600">
            {user && <span>{user.displayName}</span>}
            <button
              type="button"
              onClick={signOut}
              className="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-600 hover:bg-slate-100"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
