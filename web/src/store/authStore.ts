import { create } from 'zustand';
import { onAuthStateChanged, User as FirebaseUser } from 'firebase/auth';
import { ref, get, onValue, onDisconnect, serverTimestamp, set as firebaseSet } from 'firebase/database';
import { auth, db } from '../firebase';

export interface UserProfile {
  uid: string;
  email: string;
  displayName: string;
  username: string;
  bio?: string;
  profilePictureUrl?: string;
}

interface AuthState {
  user: UserProfile | null;
  loading: boolean;
  initialized: boolean;
  listenToAuth: () => void;
  setUser: (user: UserProfile | null) => void;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((setZustand) => ({
  user: null,
  loading: true,
  initialized: false,
  
  listenToAuth: () => {
    onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        const connectedRef = ref(db, '.info/connected');
        const lastSeenRef = ref(db, `users/${firebaseUser.uid}/lastSeen`);
        onValue(connectedRef, (snap) => {
          if (snap.val() === true) {
            firebaseSet(lastSeenRef, -1);
            onDisconnect(lastSeenRef).set(serverTimestamp());
          }
        });
        const userRef = ref(db, `users/${firebaseUser.uid}`);
        const snapshot = await get(userRef);
        
        if (snapshot.exists()) {
          const userData = snapshot.val();
          setZustand({ 
            user: {
              uid: firebaseUser.uid,
              email: firebaseUser.email || '',
              displayName: userData.displayName || '',
              username: userData.username || '',
              bio: userData.bio || '',
              profilePictureUrl: userData.profilePictureUrl || ''
            },
            loading: false,
            initialized: true
          });
        } else {
          // Fallback if doc doesn't exist yet but user is logged in
          setZustand({
            user: {
              uid: firebaseUser.uid,
              email: firebaseUser.email || '',
              displayName: firebaseUser.displayName || '',
              username: '',
            },
            loading: false,
            initialized: true
          });
        }
      } else {
        setZustand({ user: null, loading: false, initialized: true });
      }
    });
  },
  
  setUser: (user) => setZustand({ user }),
  
  logout: async () => {
    await auth.signOut();
    setZustand({ user: null });
  }
}));
