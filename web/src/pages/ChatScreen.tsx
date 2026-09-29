import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { useStreamClient } from '../hooks/useStreamClient';
import { Chat, Channel, MessageList, MessageComposer, Window, ChannelHeader } from 'stream-chat-react';
import 'stream-chat-react/dist/css/index.css';

const ChatScreen: React.FC = () => {
  const { id: chatId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const client = useStreamClient();
  const [channel, setChannel] = useState<any>(null);

  useEffect(() => {
    if (!client || !chatId || !user) return;

    // In a real app, the channel ID is usually generated when users start a chat.
    // For this migration, we'll try to watch a messaging channel with the given ID.
    const setupChannel = async () => {
      try {
        const newChannel = client.channel('messaging', chatId);
        await newChannel.watch();
        setChannel(newChannel);
      } catch (err) {
        console.error("Error watching channel", err);
      }
    };

    setupChannel();
  }, [client, chatId, user]);

  if (!client || !channel) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50 dark:bg-gray-900">
        <p className="text-gray-500">Connecting to secure chat...</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      <header className="flex items-center px-4 py-3 bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <button 
          onClick={() => navigate('/')}
          className="p-2 mr-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300"
        >
          <ArrowLeft size={22} />
        </button>
        <div className="flex-1">
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white">Chat</h1>
        </div>
      </header>

      <main className="flex-1 overflow-hidden">
        <Chat client={client} theme="str-chat__theme-light">
          <Channel channel={channel}>
            <Window>
              <ChannelHeader />
              <MessageList />
              <MessageComposer />
            </Window>
          </Channel>
        </Chat>
      </main>
    </div>
  );
};

export default ChatScreen;
