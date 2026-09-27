import { create } from 'zustand';
import { onAuthStateChanged, User as FirebaseUser } from 'firebase/auth';
import { ref, get } from 'firebase/database';
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

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  loading: true,
  initialized: false,
  
  listenToAuth: () => {
    onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        const userRef = ref(db, `users/${firebaseUser.uid}`);
        const snapshot = await get(userRef);
        
        if (snapshot.exists()) {
          const userData = snapshot.val();
          set({ 
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
          set({
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
        set({ user: null, loading: false, initialized: true });
      }
    });
  },
  
  setUser: (user) => set({ user }),
  
  logout: async () => {
    await auth.signOut();
    set({ user: null });
  }
}));
