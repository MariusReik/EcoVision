import type { ActivityCategory } from '../../api/types'

// Fixed categorical assignment, never cycled - see the dataviz skill's color
// formula. Four categories, so the "adjacent pairlist" gate (not all-pairs) applies
// and all four slots clear it.
export const CATEGORY_COLORS: Record<ActivityCategory, string> = {
  TRANSPORT: '#2a78d6', // slot 1, blue
  ENERGY: '#eb6834', // slot 2, orange
  FOOD: '#1baf7a', // slot 3, aqua
  WASTE: '#eda100', // slot 4, yellow
}

export const CATEGORY_LABELS: Record<ActivityCategory, string> = {
  TRANSPORT: 'Transport',
  ENERGY: 'Energy',
  FOOD: 'Food',
  WASTE: 'Waste',
}

export const TREND_LINE_COLOR = '#2a78d6' // sequential default hue, single series
