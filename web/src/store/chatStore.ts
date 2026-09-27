import { create } from 'zustand';
import { db } from '../firebase';
import { ref, onValue, push, set, update, get } from 'firebase/database';

export interface Chat {
  id: string;
  lastMessage?: string;
  lastMessageTime?: number;
  participants: Record<string, number>;
  otherParticipantId?: string;
  otherParticipantName?: string;
}

export interface Message {
  id: string;
  text: string;
  senderId: string;
  timestamp: number;
}

interface ChatStore {
  chats: Chat[];
  activeChatId: string | null;
  messages: Message[];
  setActiveChat: (chatId: string | null) => void;
  listenToChats: (uid: string) => () => void;
  listenToMessages: (chatId: string) => () => void;
  sendMessage: (chatId: string, text: string, senderId: string) => Promise<void>;
  markAsRead: (chatId: string, uid: string) => Promise<void>;
}

export const useChatStore = create<ChatStore>((setZustand) => ({
  chats: [],
  activeChatId: null,
  messages: [],
  setActiveChat: (chatId) => setZustand({ activeChatId: chatId }),

  listenToChats: (uid: string) => {
    const chatsRef = ref(db, 'chats');
    
    const unsubscribe = onValue(chatsRef, async (snapshot) => {
      const data = snapshot.val();
      if (!data) {
        setZustand({ chats: [] });
        return;
      }

      const userChats: Chat[] = [];
      for (const [chatId, chatData] of Object.entries(data as Record<string, any>)) {
        if (chatData.participants && chatData.participants[uid] !== undefined && chatData.participants[uid] >= 0) {
          const chat: Chat = {
            id: chatId,
            lastMessage: chatData.lastMessage,
            lastMessageTime: chatData.lastMessageTime,
            participants: chatData.participants
          };

          const participantIds = Object.keys(chatData.participants);
          if (participantIds.length === 2) {
            const otherId = participantIds.find(id => id !== uid);
            if (otherId) {
              chat.otherParticipantId = otherId;
              try {
                const userRef = ref(db, `users/${otherId}`);
                const userSnap = await get(userRef);
                if (userSnap.exists()) {
                  chat.otherParticipantName = userSnap.val().displayName;
                }
              } catch (e) {
                console.error("Failed to get user name", e);
              }
            }
          }
          userChats.push(chat);
        }
      }
      
      userChats.sort((a, b) => (b.lastMessageTime || 0) - (a.lastMessageTime || 0));
      setZustand({ chats: userChats });
    });

    return unsubscribe;
  },

  listenToMessages: (chatId: string) => {
    const messagesRef = ref(db, `messages/${chatId}`);
    
    const unsubscribe = onValue(messagesRef, (snapshot) => {
      const data = snapshot.val();
      if (!data) {
        setZustand({ messages: [] });
        return;
      }

      const msgs = Object.entries(data as Record<string, any>).map(([id, msgData]) => ({
        id,
        text: msgData.text,
        senderId: msgData.senderId,
        timestamp: msgData.timestamp
      }));
      
      msgs.sort((a, b) => a.timestamp - b.timestamp);
      setZustand({ messages: msgs });
    });

    return unsubscribe;
  },

  sendMessage: async (chatId: string, text: string, senderId: string) => {
    const messagesRef = ref(db, `messages/${chatId}`);
    const newMsgRef = push(messagesRef);
    const timestamp = Date.now();
    
    await set(newMsgRef, {
      text,
      senderId,
      timestamp
    });

    const chatRef = ref(db, `chats/${chatId}`);
    await update(chatRef, {
      lastMessage: text,
      lastMessageTime: timestamp
    });
    
    const chatSnap = await get(chatRef);
    if (chatSnap.exists()) {
       const chatData = chatSnap.val();
       if (chatData.participants) {
          const updates: Record<string, any> = {};
          Object.keys(chatData.participants).forEach(participantId => {
             if (participantId !== senderId) {
                 const currentCount = chatData.participants[participantId] || 0;
                 const baseCount = currentCount < 0 ? 0 : currentCount;
                 updates[`participants/${participantId}`] = baseCount + 1;
             } else {
                 if (chatData.participants[participantId] < 0) {
                     updates[`participants/${participantId}`] = 0;
                 }
             }
          });
          if (Object.keys(updates).length > 0) {
             await update(chatRef, updates);
          }
       }
    }
  },

  markAsRead: async (chatId: string, uid: string) => {
    const chatParticipantRef = ref(db, `chats/${chatId}/participants`);
    const updates: Record<string, any> = {};
    updates[uid] = 0;
    await update(chatParticipantRef, updates);
  }
}));
