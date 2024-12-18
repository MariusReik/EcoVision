<template>
    <div class="layout">
      <Header />
      <main class="content-container">
        <slot></slot>
      </main>
      <footer>
        <p>© 2024 EcoVision. All rights reserved.</p>
      </footer>
    </div>
  </template>
  
  <script>
  import Header from '../components/Header.vue'
  
  export default {
    name: 'Layout',
    components: {
      Header
    }
  }
  </script>
  
  <style scoped>
  footer {
    text-align: center;
    padding: 2rem 0;
    color: #666;
    font-size: 0.875rem;
    border-top: 1px solid var(--border-light);
  }
  </style>
  