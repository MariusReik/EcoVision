// ISO 3166-1 alpha-2 codes the register/settings forms offer, plus the GLOBAL
// fallback region. This is just a curated pick list for the <select> - the backend
// accepts any two-letter code (see RegisterRequest.java) and falls back to whatever
// GLOBAL emission factor exists if the exact region has none (ARCHITECTURE.md section 5).
export const REGION_OPTIONS: { code: string; label: string }[] = [
  { code: 'GLOBAL', label: 'Global average' },
  { code: 'NO', label: 'Norway' },
  { code: 'SE', label: 'Sweden' },
  { code: 'DK', label: 'Denmark' },
  { code: 'GB', label: 'United Kingdom' },
  { code: 'DE', label: 'Germany' },
  { code: 'US', label: 'United States' },
]
