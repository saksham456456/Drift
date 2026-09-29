import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getDatabase } from 'firebase/database';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || import.meta.env.apiKey || "AIzaSyBhx_Wosx4TiiZAmTjcaZZNZDKEXAsFbnU",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || import.meta.env.authDomain || "drift-a0e7e.firebaseapp.com",
  databaseURL: import.meta.env.VITE_FIREBASE_DATABASE_URL || import.meta.env.databaseURL || "https://drift-a0e7e-default-rtdb.firebaseio.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || import.meta.env.projectId || "drift-a0e7e",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || import.meta.env.storageBucket || "drift-a0e7e.firebasestorage.app",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || import.meta.env.messagingSenderId || "883931582841",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || import.meta.env.appId || "1:883931582841:web:7e86b3013c8b8c57939fa8",
  measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || import.meta.env.measurementId || "G-C5MEZP3TRM"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getDatabase(app);
