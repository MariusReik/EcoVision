<template>
    <section class="hero">
      <div class="hero-content">
        <h1>Welcome to EcoVision</h1>
        <p>Analyze environmental impact through smart image recognition and data visualization.</p>
        <router-link to="/analysis" class="btn btn-primary">Get Started</router-link>
      </div>
    </section>
  </template>
  
  <style scoped>
  .hero {
    background: var(--bg-white);
    padding: 4rem 2rem;
    text-align: center;
  }
  
  .hero-content {
    max-width: 600px;
    margin: 0 auto;
  }
  
  .hero h1 {
    font-size: 2.5rem;
    font-weight: 600;
    margin-bottom: 1rem;
    color: var(--text-dark);
  }
  
  .hero p {
    font-size: 1.125rem;
    color: #555;
    margin-bottom: 2rem;
  }
  
  .btn {
    display: inline-block;
    padding: 0.75rem 1.25rem;
    border-radius: 4px;
    border: 2px solid var(--primary-green);
    background: var(--bg-white);
    color: var(--text-dark);
    font-weight: 500;
    text-decoration: none;
    transition: background-color 0.2s, color 0.2s;
  }
  
  .btn:hover {
    background: var(--primary-green);
    color: #fff;
  }
  
  .btn-primary {
    background: var(--primary-green);
    color: #fff;
    border: none;
  }
  
  .btn-primary:hover {
    background: #7BB03F;
  }
  </style>
  