import { defineStore } from 'pinia'

export const useEcoDataStore = defineStore('ecoData', {
  state: () => ({
    data: null,
    loading: false,
    error: null
  }),
  actions: {
    async fetchEcoData() {
      this.loading = true
      try {
        const res = await fetch('/api/eco-data')
        if (!res.ok) throw new Error('Failed to fetch')
        this.data = await res.json()
      } catch (err) {
        this.error = err
      } finally {
        this.loading = false
      }
    }
  }
})
