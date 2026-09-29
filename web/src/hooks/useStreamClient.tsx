import React, { createContext, useContext, useState, useEffect } from 'react';
import { StreamChat } from 'stream-chat';
import { auth } from '../firebase';

const STREAM_API_KEY = import.meta.env.VITE_STREAM_API_KEY || 'dummy_api_key';

interface StreamContextType {
  client: StreamChat | null;
}

const StreamContext = createContext<StreamContextType>({ client: null });

export const StreamProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [client, setClient] = useState<StreamChat | null>(null);

  useEffect(() => {
    let active = true;

    const initStream = async () => {
      // Firebase auth state listener to get the user when they log in
      const unsubscribe = auth.onAuthStateChanged(async (user) => {
        if (!user) {
          if (client) {
            client.disconnectUser();
            setClient(null);
          }
          return;
        }

        try {
          const idToken = await user.getIdToken();
          const res = await fetch('/api/stream-token', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${idToken}`,
            },
          });

          if (!res.ok) throw new Error('Failed to get stream token');

          const { token, uid } = await res.json();
          
          const streamClient = StreamChat.getInstance(STREAM_API_KEY);
          await streamClient.connectUser(
            {
              id: uid,
              name: user.displayName || 'Unknown',
            },
            token
          );

          if (active) setClient(streamClient);
        } catch (err) {
          console.error('Error connecting to Stream', err);
        }
      });

      return () => {
        unsubscribe();
      };
    };

    initStream();

    return () => {
      active = false;
      if (client) {
        client.disconnectUser();
      }
    };
  }, []);

  return (
    <StreamContext.Provider value={{ client }}>
      {children}
    </StreamContext.Provider>
  );
};

export const useStreamClient = () => {
  const context = useContext(StreamContext);
  return context.client;
};
