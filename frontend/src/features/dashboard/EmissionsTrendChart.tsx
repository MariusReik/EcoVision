import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { DailyEmission } from '../../api/types'
import { TREND_LINE_COLOR } from './chartColors'

export function EmissionsTrendChart({ dailySeries }: { dailySeries: DailyEmission[] }) {
  if (dailySeries.length === 0) {
    return <p className="text-sm text-slate-500">No activity in this window yet.</p>
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={dailySeries} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
        <CartesianGrid stroke="#e1e0d9" vertical={false} />
        <XAxis
          dataKey="date"
          tick={{ fontSize: 12, fill: '#898781' }}
          axisLine={{ stroke: '#c3c2b7' }}
          tickLine={false}
        />
        <YAxis
          tick={{ fontSize: 12, fill: '#898781' }}
          axisLine={false}
          tickLine={false}
          width={48}
          label={{ value: 'kg CO2e', angle: -90, position: 'insideLeft', fill: '#898781', fontSize: 12 }}
        />
        <Tooltip
          formatter={(value: unknown) => [`${Number(value).toFixed(2)} kg CO2e`, 'Total']}
          contentStyle={{ fontSize: 12, borderRadius: 8, borderColor: '#e1e0d9' }}
        />
        <Line
          type="monotone"
          dataKey="totalKg"
          stroke={TREND_LINE_COLOR}
          strokeWidth={2}
          dot={{ r: 4, fill: TREND_LINE_COLOR }}
          activeDot={{ r: 6 }}
        />
      </LineChart>
    </ResponsiveContainer>
  )
}
