
# EcoVision

EcoVision is a web application designed to help users monitor and reduce their carbon footprint by tracking daily activities, calculating associated carbon emissions, and providing actionable insights. The platform offers visual dashboards, personalized recommendations, and optional integration with carbon offset programs.

## Features

- **User Activity Tracking**: Log daily activities such as transportation, energy usage, diet, and recycling.
- **Carbon Emission Calculations**: Convert logged activities into carbon emission values using reliable datasets and APIs.
- **Dashboard Visualization**: View charts and insights that illustrate your environmental impact over time.
- **Actionable Insights & Suggestions**: Receive personalized tips to improve and reduce your carbon footprint.
- **Carbon Offset Integration**: Optionally connect with tree-planting or carbon offset programs through external APIs.

## Tech Stack

- **Frontend**: Vue.js 3, Tailwind CSS, Vite
- **State Management**: Pinia
- **Routing**: Vue Router
- **Charting**: Chart.js integrated with Vue
- **Backend**: Express.js
- **Database**: PostgreSQL
- **ORM**: Sequelize

## Getting Started

### Prerequisites

- Node.js (version 14 or above)
- PostgreSQL (version 12 or above)

### Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/MariusReik/EcoVision.git
   cd EcoVision
   ```

2. **Backend Setup**:

   - Navigate to the backend directory:

     ```bash
     cd backend
     ```

   - Install dependencies:

     ```bash
     npm install
     ```

   - Configure the database:

     - Ensure PostgreSQL is running.
     - Create a `.env` file based on the provided `.env.example` and set your database credentials.

   - Run database migrations:

     ```bash
     npx sequelize-cli db:migrate
     ```

   - Start the backend server:

     ```bash
     npm start
     ```

3. **Frontend Setup**:

   - Navigate to the frontend directory:

     ```bash
     cd ../frontend
     ```

   - Install dependencies:

     ```bash
     npm install
     ```

   - Start the frontend development server:

     ```bash
     npm run dev
     ```


## Contributing

Contributions are welcome! Please fork the repository and create a new branch for any feature or bug fix. Submit a pull request with a clear description of your changes.

