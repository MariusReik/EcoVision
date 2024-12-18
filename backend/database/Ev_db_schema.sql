-- Users table to store user information
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    region VARCHAR(100), --region-specific emission factors
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Activities table to log user activities
CREATE TABLE activities (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL, -- e.g., car_trip, meat_meal
    value DECIMAL(10, 2) NOT NULL, -- User-provided value (e.g., distance in km, grams of meat)
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    emission_value DECIMAL(10, 4) NOT NULL, -- Calculated emission value
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Emission factors table to store emission conversion data
CREATE TABLE emission_factors (
    id SERIAL PRIMARY KEY,
    activity_type VARCHAR(50) NOT NULL, -- e.g., car_trip, meat_meal
    region VARCHAR(100), -- region-specific factors
    emission_factor DECIMAL(10, 6) NOT NULL, -- Emission factor per unit
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Carbon offsets table to track purchases
CREATE TABLE offsets (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    offset_amount DECIMAL(10, 2) NOT NULL, -- Amount of carbon offset in kg/tons
    purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recommendations table for storing improvement suggestions
CREATE TABLE recommendations (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    recommendation_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Achievements table for gamification
CREATE TABLE achievements (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    achievement_name VARCHAR(100) NOT NULL,
    description TEXT,
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Leaderboard data for gamification
CREATE TABLE leaderboard (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    total_emissions DECIMAL(10, 2) NOT NULL, -- Total emissions reduced or offset
    rank INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--  faster lookups
CREATE INDEX idx_activities_user_id ON activities(user_id);
CREATE INDEX idx_emission_factors_activity_type ON emission_factors(activity_type);