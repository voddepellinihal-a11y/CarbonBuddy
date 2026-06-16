import { useMemo } from 'react'

const LEVEL_CONFIG = {
  1: { trunkH: 40, crownSize: 80, leaves: 8, leafColor: '#86efac', hasFruit: false, trunkW: 12 },
  2: { trunkH: 55, crownSize: 110, leaves: 14, leafColor: '#4ade80', hasFruit: false, trunkW: 14 },
  3: { trunkH: 70, crownSize: 150, leaves: 22, leafColor: '#22c55e', hasFruit: false, trunkW: 16 },
  4: { trunkH: 85, crownSize: 190, leaves: 32, leafColor: '#16a34a', hasFruit: true, trunkW: 18 },
  5: { trunkH: 100, crownSize: 230, leaves: 45, leafColor: '#15803d', hasFruit: true, trunkW: 20 },
}

export default function TreeAnimation({ level = 1, points = 0, isChampion = false }) {
  const config = LEVEL_CONFIG[Math.min(level, 5)] || LEVEL_CONFIG[1]

  const leaves = useMemo(() => {
    const result = []
    for (let i = 0; i < config.leaves; i++) {
      const angle = (360 / config.leaves) * i + Math.random() * 20
      const radius = config.crownSize * 0.35 * (0.6 + Math.random() * 0.4)
      const x = Math.cos((angle * Math.PI) / 180) * radius
      const y = Math.sin((angle * Math.PI) / 180) * radius * 0.7 - 10
      const size = 18 + Math.random() * 16
      const delay = Math.random() * 3
      result.push({ x, y, size, delay, id: i })
    }
    return result
  }, [config.leaves, config.crownSize])

  const fruits = useMemo(() => {
    if (!config.hasFruit) return []
    const count = level === 5 ? 8 : 4
    const result = []
    for (let i = 0; i < count; i++) {
      const angle = (360 / count) * i + 15
      const radius = config.crownSize * 0.3
      const x = Math.cos((angle * Math.PI) / 180) * radius
      const y = Math.sin((angle * Math.PI) / 180) * radius * 0.6
      const colors = ['#ef4444', '#f97316', '#eab308', '#ec4899']
      result.push({ x, y, color: colors[i % colors.length], delay: i * 0.3, id: i })
    }
    return result
  }, [config.hasFruit, level, config.crownSize])

  const showChampionEffects = isChampion || level >= 5
  const levelLabels = {
    1: 'Level 1 tree',
    2: 'Level 2 tree',
    3: 'Level 3 tree',
    4: 'Level 4 tree with fruits',
    5: 'Level 5 champion tree with fruits',
  }

  return (
    <div
      className="tree-container"
      role="img"
      aria-label={`Level ${level} tree animation. ${levelLabels[Math.min(level, 5)] || levelLabels[1]}`}
    >
      <div className="tree-wrapper" style={{ '--crown-size': `${config.crownSize}px`, '--trunk-h': `${config.trunkH}px`, '--trunk-w': `${config.trunkW}px` }} aria-hidden="true">
        <div className="tree-ground" />
        {showChampionEffects && <div className="crown-glow" />}
        <div className="tree-crown">
          {leaves.map((l) => (
            <div
              key={l.id}
              className="tree-leaf"
              style={{
                transform: `translate(${l.x}px, ${l.y}px)`,
                width: `${l.size}px`,
                height: `${l.size}px`,
                background: config.leafColor,
                animationDelay: `${l.delay}s`,
              }}
            />
          ))}
        </div>
        {fruits.map((f) => (
          <div
            key={`fruit-${f.id}`}
            className="tree-fruit"
            style={{
              transform: `translate(${f.x}px, ${f.y}px)`,
              background: f.color,
              animationDelay: `${f.delay}s`,
            }}
          />
        ))}
        {showChampionEffects && (
          <div className="champion-sparkles">
            {[...Array(6)].map((_, i) => (
              <div
                key={`sparkle-${i}`}
                className="sparkle"
                style={{
                  '--angle': `${i * 60}deg`,
                  animationDelay: `${i * 0.4}s`,
                }}
              />
            ))}
          </div>
        )}
        <div className="tree-trunk">
          <div className="tree-trunk-inner" />
        </div>
      </div>
    </div>
  )
}
