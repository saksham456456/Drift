import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useChatStore } from '../store/chatStore';
import { ArrowLeft, Send } from 'lucide-react';

const ChatScreen: React.FC = () => {
  const { id: chatId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { chats, messages, listenToMessages, sendMessage, markAsRead, setActiveChat } = useChatStore();
  
  const [inputText, setInputText] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const chat = chats.find(c => c.id === chatId);

  useEffect(() => {
    if (chatId) {
      setActiveChat(chatId);
      const unsubscribe = listenToMessages(chatId);
      return () => {
        unsubscribe();
        setActiveChat(null);
      };
    }
  }, [chatId, listenToMessages, setActiveChat]);

  useEffect(() => {
    if (chatId && user) {
      markAsRead(chatId, user.uid);
    }
  }, [chatId, user, markAsRead, messages]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || !chatId || !user) return;
    
    const textToSend = inputText.trim();
    setInputText('');
    await sendMessage(chatId, textToSend, user.uid);
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      <header className="flex items-center px-4 py-3 bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <button 
          onClick={() => navigate('/')}
          className="p-2 mr-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300"
        >
          <ArrowLeft size={22} />
        </button>
        <div className="flex items-center flex-1">
          <div className="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold">
            {chat?.otherParticipantName ? chat.otherParticipantName.charAt(0).toUpperCase() : '?'}
          </div>
          <h1 className="ml-3 text-lg font-semibold text-gray-900 dark:text-white truncate">
            {chat?.otherParticipantName || 'Chat'}
          </h1>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((msg, index) => {
          const isOwn = msg.senderId === user?.uid;
          const showTime = index === messages.length - 1 || messages[index + 1].senderId !== msg.senderId;
          const date = new Date(msg.timestamp);
          
          return (
            <div key={msg.id} className={`flex flex-col ${isOwn ? 'items-end' : 'items-start'}`}>
              <div 
                className={`max-w-[75%] rounded-2xl px-4 py-2 ${
                  isOwn 
                    ? 'bg-blue-500 text-white rounded-br-sm' 
                    : 'bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white rounded-bl-sm'
                }`}
              >
                <p className="text-[15px] leading-relaxed break-words whitespace-pre-wrap">{msg.text}</p>
              </div>
              {showTime && (
                <span className="text-[11px] text-gray-500 dark:text-gray-400 mt-1 mx-1">
                  {date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              )}
            </div>
          );
        })}
        <div ref={messagesEndRef} />
      </main>

      <div className="bg-white dark:bg-gray-800 p-3 shadow-[0_-1px_3px_rgba(0,0,0,0.05)] border-t border-gray-200 dark:border-gray-700">
        <form onSubmit={handleSend} className="flex items-end space-x-2 max-w-4xl mx-auto">
          <div className="flex-1 bg-gray-100 dark:bg-gray-700 rounded-2xl border border-transparent focus-within:border-blue-500 transition-colors overflow-hidden flex items-center min-h-[44px]">
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Message..."
              className="w-full bg-transparent border-none focus:ring-0 px-4 py-2 text-gray-900 dark:text-white"
            />
          </div>
          <button 
            type="submit" 
            disabled={!inputText.trim()}
            className={`p-3 rounded-full flex-shrink-0 transition-colors ${
              inputText.trim() 
                ? 'bg-blue-500 text-white hover:bg-blue-600' 
                : 'bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500'
            }`}
          >
            <Send size={20} className={inputText.trim() ? 'ml-0.5' : ''} />
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChatScreen;
