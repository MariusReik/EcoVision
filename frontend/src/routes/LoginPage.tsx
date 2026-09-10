import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import { ErrorBanner } from '../components/ErrorBanner'
import { useAuth } from '../auth/useAuth'

export function LoginPage() {
  const { signIn } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (response) => {
      signIn(response.token, response.user)
      const redirectTo = (location.state as { from?: Location })?.from?.pathname ?? '/'
      navigate(redirectTo, { replace: true })
    },
  })

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    mutation.mutate({ email, password })
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-eco-50 px-4">
      <div className="w-full max-w-sm rounded-xl border border-eco-200 bg-white p-6 shadow-sm">
        <h1 className="mb-1 text-xl font-semibold text-eco-700">Welcome back</h1>
        <p className="mb-6 text-sm text-slate-500">Sign in to log your activities.</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
            Email
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
            Password
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
            />
          </label>

          <ErrorBanner error={mutation.error} />

          <button
            type="submit"
            disabled={mutation.isPending}
            className="rounded-md bg-eco-600 px-3 py-2 text-sm font-semibold text-white hover:bg-eco-700 disabled:opacity-60"
          >
            {mutation.isPending ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="mt-4 text-center text-sm text-slate-500">
          No account yet?{' '}
          <Link to="/register" className="font-medium text-eco-600 hover:underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}
