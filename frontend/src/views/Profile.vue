<template>
  <div class="min-h-screen bg-gray-50">
    <header class="bg-white shadow p-4 flex justify-between items-center">
      <h1 class="text-xl font-bold">Profile & Settings</h1>
      <nav>
        <router-link to="/main-menu" class="text-blue-600 hover:underline">Back to Main Menu</router-link>
      </nav>
    </header>
    <main class="p-4">
      <section class="mb-6">
        <h2 class="text-lg font-semibold mb-2">Account Info</h2>
        <p><strong>Name:</strong> {{ userInfo.name }}</p>
        <p><strong>Email:</strong> {{ userInfo.email }}</p>
      </section>
      
      <section class="mb-6">
        <h2 class="text-lg font-semibold mb-2">Settings</h2>
        <!-- Add toggle or select inputs for units, region, etc. -->
        <label class="block mb-2">
          Region:
          <select v-model="settings.region" class="border-gray-300 rounded px-2 py-1">
            <option>Global</option>
            <option>Europe</option>
            <option>North America</option>
            <!-- ... -->
          </select>
        </label>
      </section>

      <button @click="logout" class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700">
        Logout
      </button>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const userInfo = ref({ name: 'John Doe', email: 'john@example.com' })
const settings = ref({ region: 'Global' })

onMounted(() => {
  // Fetch user info and settings from API if available
})

const logout = () => {
  // Clear token from local storage or state
  // Redirect to login
  router.push('/login')
}
</script>
    