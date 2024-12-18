const express = require('express');
const router = express.Router();
const pool = require('../db');

// Get all user activities
router.get('/activities', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM activities');
    res.json(result.rows);
  } catch (err) {
    console.error('Error fetching activities:', err);
    res.status(500).json({ error: 'Database error' });
  }
});

module.exports = router;