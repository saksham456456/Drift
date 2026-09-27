import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { db } from '../firebase';
import { ref, get, query, orderByChild, startAt, endAt } from 'firebase/database';
import { ArrowLeft, Search as SearchIcon } from 'lucide-react';
import { useChatStore } from '../store/chatStore';

interface SearchUser {
  uid: string;
  username: string;
  displayName: string;
}

const Search: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState<SearchUser[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchQuery.trim().length > 0) {
        performSearch();
      } else {
        setResults([]);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const performSearch = async () => {
    if (!user) return;
    setLoading(true);
    try {
      const q = searchQuery.toLowerCase();
      const usernamesRef = ref(db, 'usernames');
      
      const snapshot = await get(usernamesRef);
      if (snapshot.exists()) {
        const matches: SearchUser[] = [];
        const data = snapshot.val();
        
        for (const [username, uid] of Object.entries(data as Record<string, string>)) {
          if (username.startsWith(q) && uid !== user.uid) {
             const userRef = ref(db, `users/${uid}`);
             const userSnap = await get(userRef);
             if (userSnap.exists()) {
               matches.push({
                 uid,
                 username,
                 displayName: userSnap.val().displayName || username
               });
             }
          }
        }
        setResults(matches);
      } else {
        setResults([]);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const startChat = async (otherUid: string) => {
    if (!user) return;
    
    // Check if chat exists
    const chatsRef = ref(db, 'chats');
    const snapshot = await get(chatsRef);
    let existingChatId = null;
    
    if (snapshot.exists()) {
      const allChats = snapshot.val();
      for (const [chatId, chatData] of Object.entries(allChats as Record<string, any>)) {
        if (chatData.participants && chatData.participants[user.uid] !== undefined && chatData.participants[otherUid] !== undefined) {
           // Both are participants. This is their direct chat.
           const participantKeys = Object.keys(chatData.participants);
           if (participantKeys.length === 2 && chatData.type !== 'group') {
              existingChatId = chatId;
              break;
           }
        }
      }
    }

    if (existingChatId) {
       navigate(`/chat/${existingChatId}`);
    } else {
       // Create new chat
       const { push, set } = await import('firebase/database');
       const newChatRef = push(chatsRef);
       await set(newChatRef, {
          type: 'direct',
          participants: {
             [user.uid]: 0,
             [otherUid]: 0
          },
          lastMessage: '',
          lastMessageTime: Date.now(),
          createdBy: user.uid
       });
       navigate(`/chat/${newChatRef.key}`);
    }
  };

  return (
    <div className="flex flex-col h-screen bg-white dark:bg-gray-900">
      <header className="flex items-center px-4 py-3 border-b border-gray-200 dark:border-gray-800">
        <button onClick={() => navigate('/')} className="p-2 mr-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-600 dark:text-gray-300">
          <ArrowLeft size={22} />
        </button>
        <div className="flex-1 relative">
          <input
            type="text"
            autoFocus
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by username..."
            className="w-full bg-gray-100 dark:bg-gray-800 border-none rounded-full py-2 pl-10 pr-4 focus:ring-2 focus:ring-blue-500 text-gray-900 dark:text-white placeholder-gray-500"
          />
          <SearchIcon size={18} className="absolute left-3 top-2.5 text-gray-500" />
        </div>
      </header>

      <main className="flex-1 overflow-y-auto">
        {loading ? (
          <div className="p-4 text-center text-gray-500">Searching...</div>
        ) : results.length > 0 ? (
          <ul className="divide-y divide-gray-100 dark:divide-gray-800">
            {results.map(r => (
              <li 
                key={r.uid} 
                onClick={() => startChat(r.uid)}
                className="flex items-center px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer"
              >
                <div className="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold flex-shrink-0">
                  {r.displayName.charAt(0).toUpperCase()}
                </div>
                <div className="ml-3 flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{r.displayName}</p>
                  <p className="text-xs text-gray-500 truncate">@{r.username}</p>
                </div>
              </li>
            ))}
          </ul>
        ) : searchQuery.trim().length > 0 ? (
          <div className="p-8 text-center text-gray-500">
            <p>No users found matching "{searchQuery}"</p>
          </div>
        ) : (
          <div className="p-8 text-center text-gray-500">
            <SearchIcon size={48} className="mx-auto mb-4 opacity-20" />
            <p>Type a username to find people</p>
          </div>
        )}
      </main>
    </div>
  );
};

export default Search;
