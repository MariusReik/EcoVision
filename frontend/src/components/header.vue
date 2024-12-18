<template>
    <header class="app-header">
      <div class="logo">EcoVision</div>
      <nav>
        <router-link to="/dashboard">Dashboard</router-link>
        <router-link to="/analysis">Analysis</router-link>
        <router-link to="/profile">Profile</router-link>
      </nav>
    </header>
  </template>
  
  <style scoped>
  .app-header {
    background: var(--bg-white);
    border-bottom: 1px solid var(--border-light);
    padding: 1rem 2rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  .logo {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--text-dark);
  }
  
  nav a {
    margin-left: 1rem;
    color: #444;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.2s;
  }
  
  nav a:hover {
    color: var(--primary-green);
  }
  </style>
  