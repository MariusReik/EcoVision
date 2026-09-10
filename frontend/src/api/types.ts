// Mirrors the DTOs in backend/src/main/java/no/ecovision - see ARCHITECTURE.md section 7.

export type AccountingBasis = 'LOCATION' | 'MARKET'

export type ActivityCategory = 'TRANSPORT' | 'ENERGY' | 'FOOD' | 'WASTE'

export interface User {
  id: string
  email: string
  displayName: string
  region: string
  accountingBasis: AccountingBasis
  createdAt: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface RegisterRequest {
  email: string
  password: string
  displayName: string
  region: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface PatchMeRequest {
  displayName?: string
  region?: string
  accountingBasis?: AccountingBasis
}

export interface ActivityType {
  code: string
  category: ActivityCategory
  unit: string
  displayName: string
}

export interface CreateActivityRequest {
  activityTypeCode: string
  // Sent as the raw decimal text the user typed (e.g. "12.5"), never a parsed float -
  // Jackson deserializes a JSON string into BigDecimal just as happily as a JSON
  // number, and this way no IEEE-754 rounding touches the value on its way to the
  // server that stores it as BigDecimal (CLAUDE.md: never double/float).
  quantity: string
  occurredOn: string
  note?: string | null
}

export interface Activity {
  id: string
  activityTypeCode: string
  quantity: number
  occurredOn: string
  emissionsKg: number
  note: string | null
}

export interface ActivityPage {
  items: Activity[]
  nextCursor: string | null
}

export interface CategoryEmission {
  category: ActivityCategory
  totalKg: number
}

export interface DailyEmission {
  date: string
  totalKg: number
}

export interface DashboardSummary {
  totalKg: number
  byCategory: CategoryEmission[]
  dailySeries: DailyEmission[]
}

/** RFC 9457 application/problem+json, per CLAUDE.md. */
export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
}
