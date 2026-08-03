import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up to 50 concurrent virtual users
    { duration: '2m', target: 100 },  // Stay at 100 concurrent users for stable stress testing
    { duration: '15s', target: 0 },   // Ramp down to 0 users
  ],
};

// 34 Product IDs verified to exist in your Supabase 'products' table (from amazon_cleaned.csv)
const productIds = [
  "B07JW9H4J1", "B098NS6PVG", "B096MSW6CT", "B08HDJ86NZ", "B08CF3B7N1", 
  "B08Y1TFSP6", "B08WRWPM22", "B08DDRGWTJ", "B008IFXQFU", "B082LZGK39", 
  "B08CF3D7QR", "B0789LZTCJ", "B07KSMBL2H", "B085DTN6R2", "B09KLVMZ3B", 
  "B083342NKJ", "B0B6F7LX4C", "B082LSVT4B", "B08WRBG3XW", "B08DPLCM6T", 
  "B09C6HXFC1", "B085194JFL", "B09F6S8BT6", "B09NHVCHS9", "B0B1YVCJ2Y", 
  "B01M4GGIVU", "B08B42LWKN", "B094JNXNPV", "B09W5XR9RT", "B077Z65HSD", 
  "B00NH11PEY", "B09CMM3VGK", "B08QSC1XY8", "B008FWZGSG"
];

export default function () {
  // Randomly query one of the active Amazon product IDs
  const id = productIds[Math.floor(Math.random() * productIds.length)];
  const url = `http://localhost:8080/products/${id}`;
  
  const res = http.get(url);
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  // Short delay to simulate real client request pacing
  sleep(0.005); 
}
