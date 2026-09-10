import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { getMe, patchMe } from '../api/user'
import type { AccountingBasis, User } from '../api/types'
import { ErrorBanner } from '../components/ErrorBanner'
import { useAuth } from '../auth/useAuth'
import { REGION_OPTIONS } from '../regions'

export function SettingsPage() {
  const { data: me, isLoading, error } = useQuery({ queryKey: ['me'], queryFn: getMe })

  if (isLoading) {
    return <p className="text-sm text-slate-500">Loading settings...</p>
  }

  if (error || !me) {
    return <ErrorBanner error={error ?? new Error('Could not load your profile')} />
  }

  return <SettingsForm me={me} />
}

function SettingsForm({ me }: { me: User }) {
  const { updateUser } = useAuth()
  const queryClient = useQueryClient()

  // Seeded once from the loaded profile (SettingsPage only renders this once `me` is
  // available), then owned locally - no effect needed to keep it in sync afterwards.
  const [displayName, setDisplayName] = useState(me.displayName)
  const [region, setRegion] = useState(me.region)
  const [accountingBasis, setAccountingBasis] = useState<AccountingBasis>(me.accountingBasis)

  const mutation = useMutation({
    mutationFn: patchMe,
    onSuccess: (updated) => {
      updateUser(updated)
      queryClient.setQueryData(['me'], updated)
    },
  })

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    mutation.mutate({ displayName, region, accountingBasis })
  }

  return (
    <div className="max-w-md">
      <h1 className="mb-1 text-xl font-semibold text-eco-700">Settings</h1>
      <p className="mb-6 text-sm text-slate-500">
        Region and accounting basis decide which emission factor applies to new activities. Past
        entries never change - ARCHITECTURE.md section 5.
      </p>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4 rounded-xl border border-eco-200 bg-white p-6">
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
        </label>

        <fieldset className="flex flex-col gap-1 text-sm font-medium text-slate-700">
          <legend className="mb-1">Electricity accounting basis</legend>
          <label className="flex items-center gap-2 font-normal">
            <input
              type="radio"
              name="accountingBasis"
              checked={accountingBasis === 'LOCATION'}
              onChange={() => setAccountingBasis('LOCATION')}
            />
            Location-based (physical grid mix)
          </label>
          <label className="flex items-center gap-2 font-normal">
            <input
              type="radio"
              name="accountingBasis"
              checked={accountingBasis === 'MARKET'}
              onChange={() => setAccountingBasis('MARKET')}
            />
            Market-based (residual mix after guarantees of origin)
          </label>
          <span className="text-xs font-normal text-slate-400">
            Only affects electricity. See ARCHITECTURE.md section 6 for why these can differ a lot
            for Norwegian consumers.
          </span>
        </fieldset>

        <ErrorBanner error={mutation.error} />

        <button
          type="submit"
          disabled={mutation.isPending}
          className="self-start rounded-md bg-eco-600 px-4 py-2 text-sm font-semibold text-white hover:bg-eco-700 disabled:opacity-60"
        >
          {mutation.isPending ? 'Saving...' : 'Save changes'}
        </button>
        {mutation.isSuccess && <p className="text-sm text-eco-700">Saved.</p>}
      </form>
    </div>
  )
}
