import React, { useMemo } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useStreamClient } from '../hooks/useStreamClient';
import { Settings, MessageSquarePlus, Users } from 'lucide-react';
import { Chat, ChannelList } from 'stream-chat-react';
import 'stream-chat-react/dist/css/index.css';

const ChatList: React.FC = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const client = useStreamClient();

  const filters = useMemo(() => {
    if (!user) return {};
    return { members: { $in: [user.uid] } };
  }, [user]);

  const sort = useMemo(() => ({ last_message_at: -1 } as const), []);

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      <header className="flex justify-between items-center px-4 py-3 bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <h1 className="text-xl font-bold text-gray-900 dark:text-white">Drift</h1>
        <div className="flex space-x-3 text-gray-500 dark:text-gray-400">
          <Link to="/create-group" className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700"><Users size={22} /></Link>
          <Link to="/search" className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700">
            <MessageSquarePlus size={22} />
          </Link>
          <Link to="/settings" className="p-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700">
            <Settings size={22} />
          </Link>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto">
        {!client ? (
           <div className="flex items-center justify-center h-full text-gray-500">Loading chats...</div>
        ) : (
           <Chat client={client} theme="str-chat__theme-light">
             <ChannelList 
               filters={filters} 
               sort={sort}
               onChannelSelect={(channel) => {
                  // channel.id might be empty or generated, navigate to the channel's ID
                  navigate(`/chat/${channel.id}`);
               }}
             />
           </Chat>
        )}
      </main>
    </div>
  );
};

export default ChatList;
