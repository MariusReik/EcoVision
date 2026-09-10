import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { createActivity } from '../../api/activities'
import { ErrorBanner } from '../../components/ErrorBanner'
import { useActivityTypes } from './useActivityTypes'

const QUANTITY_PATTERN = /^\d+(\.\d+)?$/
const today = () => new Date().toISOString().slice(0, 10)

export function ActivityForm() {
  const queryClient = useQueryClient()
  const { data: activityTypes, isLoading: typesLoading, error: typesError } = useActivityTypes()

  const [activityTypeCode, setActivityTypeCode] = useState('')
  const [quantity, setQuantity] = useState('')
  const [occurredOn, setOccurredOn] = useState(today)
  const [note, setNote] = useState('')

  const selectedType = activityTypes?.find((t) => t.code === activityTypeCode)

  const mutation = useMutation({
    mutationFn: createActivity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['activities'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] })
      setQuantity('')
      setNote('')
    },
  })

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!activityTypeCode || !QUANTITY_PATTERN.test(quantity)) {
      return
    }
    mutation.mutate({ activityTypeCode, quantity, occurredOn, note: note || null })
  }

  if (typesLoading) {
    return <p className="text-sm text-slate-500">Loading activity types...</p>
  }

  if (typesError || !activityTypes) {
    return <ErrorBanner error={typesError ?? new Error('Could not load activity types')} />
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4 rounded-xl border border-eco-200 bg-white p-6">
      <h2 className="text-base font-semibold text-eco-700">Log an activity</h2>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
          Activity type
          <select
            required
            value={activityTypeCode}
            onChange={(e) => setActivityTypeCode(e.target.value)}
            className="rounded-md border border-slate-300 bg-white px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
          >
            <option value="" disabled>
              Select one...
            </option>
            {activityTypes.map((type) => (
              <option key={type.code} value={type.code}>
                {type.displayName}
              </option>
            ))}
          </select>
        </label>

        <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
          Quantity{selectedType ? ` (${selectedType.unit})` : ''}
          <input
            required
            inputMode="decimal"
            placeholder={selectedType ? `e.g. 10 ${selectedType.unit}` : 'Pick a type first'}
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
          Date
          <input
            type="date"
            required
            max={today()}
            value={occurredOn}
            onChange={(e) => setOccurredOn(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
          Note (optional)
          <input
            maxLength={280}
            value={note}
            onChange={(e) => setNote(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-eco-500 focus:outline-none"
          />
        </label>
      </div>

      <ErrorBanner error={mutation.error} />
      {mutation.isSuccess && (
        <p className="text-sm text-eco-700">
          Logged {Number(mutation.data.emissionsKg).toFixed(2)} kg CO2e.
        </p>
      )}

      <button
        type="submit"
        disabled={mutation.isPending}
        className="self-start rounded-md bg-eco-600 px-4 py-2 text-sm font-semibold text-white hover:bg-eco-700 disabled:opacity-60"
      >
        {mutation.isPending ? 'Logging...' : 'Log activity'}
      </button>
    </form>
  )
}
