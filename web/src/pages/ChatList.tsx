import React, { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useChatStore } from '../store/chatStore';
import { Settings, MessageSquarePlus } from 'lucide-react';

const ChatList: React.FC = () => {
  const { user } = useAuthStore();
  const { chats, listenToChats } = useChatStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      const unsubscribe = listenToChats(user.uid);
      return () => unsubscribe();
    }
  }, [user, listenToChats]);

  const getInitial = (name?: string) => {
    if (!name) return '?';
    return name.charAt(0).toUpperCase();
  };

  const formatTime = (ts?: number) => {
    if (!ts) return '';
    const date = new Date(ts);
    const now = new Date();
    if (date.toDateString() === now.toDateString()) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      <header className="flex justify-between items-center px-4 py-3 bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <h1 className="text-xl font-bold text-gray-900 dark:text-white">Drift</h1>
        <div className="flex space-x-3 text-gray-500 dark:text-gray-400">
          <Link to="/search" className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700">
            <MessageSquarePlus size={22} />
          </Link>
          <Link to="/settings" className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700">
            <Settings size={22} />
          </Link>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto">
        {chats.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-500 dark:text-gray-400">
            <MessageSquarePlus size={48} className="mb-4 opacity-50" />
            <p className="text-lg font-medium">No conversations yet</p>
            <p className="text-sm">Start a chat to see it here.</p>
          </div>
        ) : (
          <ul className="divide-y divide-gray-200 dark:divide-gray-800">
            {chats.map(chat => {
              const unreadCount = (user && chat.participants[user.uid]) || 0;
              return (
                <li 
                  key={chat.id} 
                  onClick={() => navigate(`/chat/${chat.id}`)}
                  className="flex items-center px-4 py-4 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer"
                >
                  <div className="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center text-white text-lg font-semibold flex-shrink-0">
                    {getInitial(chat.otherParticipantName)}
                  </div>
                  <div className="ml-4 flex-1 min-w-0">
                    <div className="flex justify-between items-baseline mb-1">
                      <h2 className="text-base font-semibold text-gray-900 dark:text-white truncate">
                        {chat.otherParticipantName || 'Unknown User'}
                      </h2>
                      <span className="text-xs text-gray-500 dark:text-gray-400 ml-2 whitespace-nowrap">
                        {formatTime(chat.lastMessageTime)}
                      </span>
                    </div>
                    <p className={`text-sm truncate ${unreadCount > 0 ? 'text-gray-900 dark:text-white font-medium' : 'text-gray-500 dark:text-gray-400'}`}>
                      {chat.lastMessage || 'No messages yet'}
                    </p>
                  </div>
                  {unreadCount > 0 && (
                    <div className="ml-3 w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center text-[10px] font-bold text-white">
                      {unreadCount}
                    </div>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </main>
    </div>
  );
};

export default ChatList;
