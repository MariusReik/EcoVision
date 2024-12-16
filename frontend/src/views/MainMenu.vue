<template>
  <div class="min-h-screen bg-gray-50">
    <header class="bg-white shadow p-4 flex justify-between items-center">
      <h1 class="text-xl font-bold">My Environmental Impact</h1>
      <nav>
        <router-link to="/profile" class="text-blue-600 hover:underline">Profile</router-link>
      </nav>
    </header>
    <main class="p-4">
      <section class="mb-6">
        <h2 class="text-lg font-semibold mb-2">Overall Impact</h2>
        <p>Total Emissions: {{ totalEmissions }} kg CO₂e</p>
      </section>

      <section class="mb-6">
        <h2 class="text-lg font-semibold mb-2">Recent Activities</h2>
        <ul class="list-disc pl-5 space-y-2">
          <li v-for="activity in recentActivities" :key="activity.id">
            {{ activity.type }}: {{ activity.value }} ({{ activity.emission_value }} kg CO₂e)
          </li>
        </ul>
      </section>

      <button @click="goToAddActivity" class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
        Add New Activity
      </button>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const totalEmissions = ref(0)
const recentActivities = ref([])

onMounted(async () => {
  // Fetch dashboard data from API
  // For now, use mock data:
  totalEmissions.value = 123.45
  recentActivities.value = [
    { id: 1, type: 'Car Trip', value: '10 km', emission_value: 2.0 },
    { id: 2, type: 'Meat Meal', value: '1 meal', emission_value: 2.5 },
  ]
})

const goToAddActivity = () => {
  // Implement navigation to an "Add Activity" page or open a modal
}
</script>
