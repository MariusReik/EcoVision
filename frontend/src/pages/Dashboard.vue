<template>
    <Layout>
      <div>
        <h2>Environmental Stats</h2>
        <div v-if="loading">Loading...</div>
        <div v-else-if="error">Failed to load data. Please try again later.</div>
        <div v-else>
          <!-- Render data here -->
        </div>
      </div>
    </Layout>
  </template>
  
  <script setup>
  import { useEcoDataStore } from '../store/useEcoData.js'
  const ecoDataStore = useEcoDataStore()
  
  onMounted(() => {
    ecoDataStore.fetchEcoData()
  })
  
  const { data, loading, error } = storeToRefs(ecoDataStore)
  </script>
  