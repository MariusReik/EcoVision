import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { deleteActivity, listActivities } from '../../api/activities'
import { ErrorBanner } from '../../components/ErrorBanner'
import { useActivityTypes } from './useActivityTypes'

export function ActivityList() {
  const queryClient = useQueryClient()
  const { data, isLoading, error } = useQuery({
    queryKey: ['activities'],
    queryFn: () => listActivities(),
  })
  const { data: activityTypes } = useActivityTypes()

  const deleteMutation = useMutation({
    mutationFn: deleteActivity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['activities'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] })
    },
  })

  const displayNameFor = (code: string) => activityTypes?.find((t) => t.code === code)?.displayName ?? code

  if (isLoading) {
    return <p className="text-sm text-slate-500">Loading activities...</p>
  }

  if (error) {
    return <ErrorBanner error={error} />
  }

  if (!data || data.items.length === 0) {
    return <p className="text-sm text-slate-500">No activities logged yet.</p>
  }

  return (
    <div className="overflow-hidden rounded-xl border border-eco-200 bg-white">
      <table className="w-full text-left text-sm">
        <thead className="bg-eco-50 text-xs font-semibold uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-2">Date</th>
            <th className="px-4 py-2">Activity</th>
            <th className="px-4 py-2">Quantity</th>
            <th className="px-4 py-2">kg CO2e</th>
            <th className="px-4 py-2" />
          </tr>
        </thead>
        <tbody className="divide-y divide-eco-100">
          {data.items.map((activity) => (
            <tr key={activity.id}>
              <td className="px-4 py-2 text-slate-600">{activity.occurredOn}</td>
              <td className="px-4 py-2 text-slate-800">{displayNameFor(activity.activityTypeCode)}</td>
              <td className="px-4 py-2 text-slate-600">{activity.quantity}</td>
              <td className="px-4 py-2 font-medium text-eco-700">{activity.emissionsKg.toFixed(2)}</td>
              <td className="px-4 py-2 text-right">
                <button
                  type="button"
                  onClick={() => deleteMutation.mutate(activity.id)}
                  disabled={deleteMutation.isPending}
                  className="text-xs font-medium text-red-500 hover:underline disabled:opacity-50"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
