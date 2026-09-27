import React from 'react';
import { useParams } from 'react-router-dom';

const ChatScreen: React.FC = () => {
  const { id } = useParams();
  return <div className="p-4">Chat Screen {id} (Work in progress)</div>;
};

export default ChatScreen;
