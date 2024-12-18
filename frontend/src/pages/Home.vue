<template>
    <Layout>
      <Frontpage />
    </Layout>
  </template>
  
  <script>
  import Layout from '../layouts/Layout.vue'
  import Frontpage from '../components/Frontpage.vue'
  
  export default {
    name: 'Home',
    components: {
      Layout,
      Frontpage
    }
  }
  </script>
  
  <style scoped>
  </style>
  