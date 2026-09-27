import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getDatabase } from 'firebase/database';

const firebaseConfig = {
  apiKey: "AIzaSyBhx_Wosx4TiiZAmTjcaZZNZDKEXAsFbnU",
  authDomain: "drift-a0e7e.firebaseapp.com",
  databaseURL: "https://drift-a0e7e-default-rtdb.firebaseio.com",
  projectId: "drift-a0e7e",
  storageBucket: "drift-a0e7e.firebasestorage.app",
  messagingSenderId: "883931582841",
  appId: "1:883931582841:web:7e86b3013c8b8c57939fa8",
  measurementId: "G-C5MEZP3TRM"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getDatabase(app);
