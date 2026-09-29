import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getDatabase } from 'firebase/database';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyBhx_Wosx4TiiZAmTjcaZZNZDKEXAsFbnU", // TODO: Remove fallback after setting .env
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "drift-a0e7e.firebaseapp.com",
  databaseURL: import.meta.env.VITE_FIREBASE_DATABASE_URL || "https://drift-a0e7e-default-rtdb.firebaseio.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "drift-a0e7e",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "drift-a0e7e.firebasestorage.app",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "883931582841",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:883931582841:web:7e86b3013c8b8c57939fa8",
  measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || "G-C5MEZP3TRM"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getDatabase(app);
