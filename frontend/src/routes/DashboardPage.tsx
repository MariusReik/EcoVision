import { ActivityForm } from '../features/activities/ActivityForm'
import { ActivityList } from '../features/activities/ActivityList'
import { CategoryBreakdownChart } from '../features/dashboard/CategoryBreakdownChart'
import { EmissionsTrendChart } from '../features/dashboard/EmissionsTrendChart'
import { useDashboardSummary } from '../features/dashboard/useDashboardSummary'
import { ErrorBanner } from '../components/ErrorBanner'

export function DashboardPage() {
  const { data: summary, isLoading, error } = useDashboardSummary()

  return (
    <div className="flex flex-col gap-6">
      <section className="rounded-xl border border-eco-200 bg-white p-6">
        <p className="text-sm font-medium text-slate-500">Total emissions</p>
        {isLoading && <p className="mt-1 text-3xl font-semibold text-slate-300">...</p>}
        {error && <ErrorBanner error={error} />}
        {summary && (
          <p className="mt-1 text-3xl font-semibold text-eco-700">
            {summary.totalKg.toFixed(2)} <span className="text-lg font-medium text-slate-500">kg CO2e</span>
          </p>
        )}
      </section>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <section className="rounded-xl border border-eco-200 bg-white p-6">
          <h2 className="mb-2 text-sm font-semibold text-slate-700">Trend</h2>
          {summary && <EmissionsTrendChart dailySeries={summary.dailySeries} />}
        </section>
        <section className="rounded-xl border border-eco-200 bg-white p-6">
          <h2 className="mb-2 text-sm font-semibold text-slate-700">By category</h2>
          {summary && <CategoryBreakdownChart byCategory={summary.byCategory} />}
        </section>
      </div>

      <ActivityForm />

      <section className="flex flex-col gap-2">
        <h2 className="text-sm font-semibold text-slate-700">Recent activity</h2>
        <ActivityList />
      </section>
    </div>
  )
}
