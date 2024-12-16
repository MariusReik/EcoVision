<template>
  <div class="flex items-center justify-center h-screen bg-gray-100">
    <div class="w-full max-w-sm p-6 bg-white rounded shadow">
      <h1 class="text-2xl font-bold mb-4">Login</h1>
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label class="block mb-1 font-semibold" for="email">Email</label>
          <input v-model="email" type="email" id="email" required class="w-full border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring focus:ring-blue-200" />
        </div>
        <div class="mb-4">
          <label class="block mb-1 font-semibold" for="password">Password</label>
          <input v-model="password" type="password" id="password" required class="w-full border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring focus:ring-blue-200" />
        </div>
        <button type="submit" class="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">Login</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const email = ref('')
const password = ref('')
const router = useRouter()

const handleLogin = async () => {
  // Placeholder: implement login logic here (fetch/axios call)
  // If success:
  //   - store JWT token in localStorage or a global store
  //   - router.push('/main-menu')
  
  // For now, just navigate to main menu directly:
  router.push('/main-menu')
}
</script>
