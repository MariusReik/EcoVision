import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/auth'
import { ErrorBanner } from '../components/ErrorBanner'
import { useAuth } from '../auth/useAuth'
import { REGION_OPTIONS } from '../regions'

export function RegisterPage() {
  const { signIn } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [region, setRegion] = useState('GLOBAL')

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (response) => {
      signIn(response.token, response.user)
      navigate('/', { replace: true })
    },
  })

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    mutation.mutate({ email, password, displayName, region })
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-eco-50 px-4">
      <div className="w-full max-w-sm rounded-xl border border-eco-200 bg-white p-6 shadow-sm">
        <h1 className="mb-1 text-xl font-semibold text-eco-700">Create your account</h1>
        <p className="mb-6 text-sm text-slate-500">Track the footprint of what you do.</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
            Display name
            <input
              required
              maxLength={100}
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
            />
          </label>
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
              minLength={8}
              maxLength={72}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
            Region
            <select
              value={region}
              onChange={(e) => setRegion(e.target.value)}
              className="rounded-md border border-slate-300 bg-white px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
            >
              {REGION_OPTIONS.map((option) => (
                <option key={option.code} value={option.code}>
                  {option.label}
                </option>
              ))}
            </select>
            <span className="text-xs font-normal text-slate-400">
              Determines which emission factors apply to your activities.
            </span>
          </label>

          <ErrorBanner error={mutation.error} />

          <button
            type="submit"
            disabled={mutation.isPending}
            className="rounded-md bg-eco-600 px-3 py-2 text-sm font-semibold text-white hover:bg-eco-700 disabled:opacity-60"
          >
            {mutation.isPending ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="mt-4 text-center text-sm text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-eco-600 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
