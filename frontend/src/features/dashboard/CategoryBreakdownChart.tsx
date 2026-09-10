import { Bar, BarChart, CartesianGrid, Cell, LabelList, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { CategoryEmission } from '../../api/types'
import { CATEGORY_COLORS, CATEGORY_LABELS } from './chartColors'

export function CategoryBreakdownChart({ byCategory }: { byCategory: CategoryEmission[] }) {
  if (byCategory.length === 0) {
    return <p className="text-sm text-slate-500">No activity in this window yet.</p>
  }

  const data = byCategory.map((row) => ({
    category: row.category,
    label: CATEGORY_LABELS[row.category],
    totalKg: row.totalKg,
  }))

  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={data} margin={{ top: 16, right: 16, bottom: 0, left: 0 }}>
        <CartesianGrid stroke="#e1e0d9" vertical={false} />
        <XAxis
          dataKey="label"
          tick={{ fontSize: 12, fill: '#52514e' }}
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
        <Bar dataKey="totalKg" radius={[4, 4, 0, 0]} maxBarSize={64}>
          {data.map((row) => (
            <Cell key={row.category} fill={CATEGORY_COLORS[row.category]} />
          ))}
          <LabelList
            dataKey="totalKg"
            position="top"
            formatter={(value: unknown) => Number(value).toFixed(1)}
            style={{ fontSize: 12, fill: '#0b0b0b' }}
          />
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}
