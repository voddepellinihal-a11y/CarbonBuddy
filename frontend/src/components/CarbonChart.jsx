import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts'

const COLORS = ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899']

export default function CarbonChart({ data }) {
  if (!data || data.length === 0) return null

  const totalCarbon = data.reduce((sum, d) => sum + (d.carbonKg || 0), 0)
  const description = data.map(d => `${d.category}: ${d.carbonKg.toFixed(2)} kg, ${d.percentage.toFixed(1)} percent`).join('. ')

  return (
    <div
      role="img"
      aria-label={`Carbon emissions pie chart. Total: ${totalCarbon.toFixed(2)} kg CO₂. Breakdown: ${description}`}
    >
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie data={data} dataKey="carbonKg" nameKey="category" cx="50%" cy="50%" outerRadius={100} label={({ category, percentage }) => `${category} ${percentage.toFixed(1)}%`}>
            {data.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
          </Pie>
          <Tooltip formatter={(value) => `${Number(value).toFixed(2)} kg`} />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}
