// Seeds a demo account against a running backend so a reviewer can log in and
// look around without registering or entering activities by hand. Goes through
// the real HTTP API (not direct SQL) so passwords are hashed and emissions are
// computed exactly the way a real user's would be - see ARCHITECTURE.md section
// 5, decision 1 on why emissions must never be written any other way.
//
// Usage: node scripts/seed-demo-account.mjs
// Requires the backend to be running (docker compose up -d && ./gradlew bootRun).

const BASE_URL = process.env.ECOVISION_API_URL ?? 'http://localhost:8080';
const EMAIL = 'demo@ecovision.app';
const PASSWORD = 'DemoPass123!';

async function api(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error(`${options.method ?? 'GET'} ${path} -> ${res.status}: ${JSON.stringify(body)}`);
  }
  return body;
}

async function main() {
  let token;
  try {
    const registered = await api('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email: EMAIL, password: PASSWORD, displayName: 'Demo User', region: 'NO' }),
    });
    token = registered.token;
    console.log(`Created demo account: ${EMAIL}`);
  } catch {
    const loggedIn = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: EMAIL, password: PASSWORD }),
    });
    token = loggedIn.token;
    console.log(`Demo account already exists, reusing it: ${EMAIL}`);
  }

  const auth = { Authorization: `Bearer ${token}` };

  // A spread of activities across the last couple of weeks so the dashboard's
  // category breakdown and daily trend chart both have something to show.
  const today = new Date();
  const daysAgo = (n) => {
    const d = new Date(today);
    d.setDate(d.getDate() - n);
    return d.toISOString().slice(0, 10);
  };

  const activities = [
    { activityTypeCode: 'car_petrol', quantity: 42, occurredOn: daysAgo(1) },
    { activityTypeCode: 'electricity', quantity: 85, occurredOn: daysAgo(1) },
    { activityTypeCode: 'beef', quantity: 0.5, occurredOn: daysAgo(2) },
    { activityTypeCode: 'train', quantity: 120, occurredOn: daysAgo(4) },
    { activityTypeCode: 'natural_gas', quantity: 60, occurredOn: daysAgo(5) },
    { activityTypeCode: 'chicken', quantity: 1, occurredOn: daysAgo(6) },
    { activityTypeCode: 'bus', quantity: 15, occurredOn: daysAgo(8) },
    { activityTypeCode: 'rice', quantity: 2, occurredOn: daysAgo(9) },
    { activityTypeCode: 'electricity', quantity: 90, occurredOn: daysAgo(11) },
    { activityTypeCode: 'waste_landfill', quantity: 8, occurredOn: daysAgo(12) },
  ];

  for (const activity of activities) {
    await api('/api/activities', { method: 'POST', headers: auth, body: JSON.stringify(activity) });
  }
  console.log(`Logged ${activities.length} demo activities.`);
  console.log(`\nDemo login -> email: ${EMAIL}  password: ${PASSWORD}`);
}

main().catch((err) => {
  console.error(err.message);
  process.exit(1);
});
