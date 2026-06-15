import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts'

const COLORS = ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899']

export default function CarbonChart({ data }) {
  if (!data || data.length === 0) return null

  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie data={data} dataKey="carbonKg" nameKey="category" cx="50%" cy="50%" outerRadius={100} label={({ category, percentage }) => `${category} ${percentage.toFixed(1)}%`}>
          {data.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
        </Pie>
        <Tooltip formatter={(value) => `${Number(value).toFixed(2)} kg`} />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  )
}
