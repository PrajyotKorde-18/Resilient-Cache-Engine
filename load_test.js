import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '15s', target: 200 },  // Ramp up to 200 concurrent virtual users (VUs) in 15 seconds
    { duration: '45s', target: 500 },  // Ramp up to 500 concurrent virtual users over 45 seconds
    { duration: '30s', target: 1000 }, // Push to extreme load of 1,000 concurrent users for 30 seconds
    { duration: '15s', target: 0 },    // Ramp down to 0 users
  ],
};

export default function () {
  // Randomly query customer indexes between 1 and 175
  const customerId = Math.floor(Math.random() * 175) + 1;
  const url = `http://localhost:8080/customers/${customerId}`;
  
  const res = http.get(url);
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  // Short pause between requests per user to simulate real browser behavior
  sleep(0.01); 
}
