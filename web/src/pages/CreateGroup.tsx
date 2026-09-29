import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useStreamClient } from '../hooks/useStreamClient';
import { db } from '../firebase';
import { ref, get } from 'firebase/database';
import { ArrowLeft, Search as SearchIcon, Users } from 'lucide-react';

interface SearchUser {
  uid: string;
  username: string;
  displayName: string;
}

const CreateGroup: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const client = useStreamClient();
  const [groupName, setGroupName] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState<SearchUser[]>([]);
  const [selectedUsers, setSelectedUsers] = useState<SearchUser[]>([]);

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
      }
    } catch (e) {
      console.error(e);
    }
  };

  const toggleSelectUser = (u: SearchUser) => {
    if (selectedUsers.find(su => su.uid === u.uid)) {
      setSelectedUsers(selectedUsers.filter(su => su.uid !== u.uid));
    } else {
      setSelectedUsers([...selectedUsers, u]);
    }
  };

  const handleCreateGroup = async () => {
    if (!user || !client || !groupName.trim() || selectedUsers.length === 0) return;
    
    try {
      const members = [user.uid, ...selectedUsers.map(su => su.uid)];
      const channel = client.channel('messaging', {
        name: groupName.trim(),
        members: members,
      });
      await channel.create();
      navigate(`/chat/${channel.id}`);
    } catch (err) {
      console.error("Error creating stream group", err);
    }
  };

  return (
    <div className="flex flex-col h-screen bg-white dark:bg-gray-900">
      <header className="flex items-center px-4 py-3 border-b border-gray-200 dark:border-gray-800">
        <button onClick={() => navigate('/')} className="p-2 mr-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-600 dark:text-gray-300">
          <ArrowLeft size={22} />
        </button>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white flex-1">New Group</h1>
        <button 
          onClick={handleCreateGroup}
          disabled={!groupName.trim() || selectedUsers.length === 0}
          className="px-4 py-1.5 bg-blue-500 text-white font-medium rounded-full disabled:opacity-50"
        >
          Create
        </button>
      </header>

      <main className="flex-1 overflow-y-auto">
        <div className="p-4 border-b border-gray-100 dark:border-gray-800">
          <input
            type="text"
            value={groupName}
            onChange={(e) => setGroupName(e.target.value)}
            placeholder="Group Name"
            className="w-full text-lg bg-transparent border-none focus:ring-0 px-2 py-2 text-gray-900 dark:text-white placeholder-gray-500"
          />
        </div>

        {selectedUsers.length > 0 && (
          <div className="p-4 border-b border-gray-100 dark:border-gray-800">
            <h2 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">Members ({selectedUsers.length})</h2>
            <div className="flex flex-wrap gap-2">
              {selectedUsers.map(su => (
                <div key={su.uid} className="flex items-center bg-gray-100 dark:bg-gray-800 rounded-full px-3 py-1">
                  <span className="text-sm text-gray-900 dark:text-white mr-2">{su.displayName}</span>
                  <button onClick={() => toggleSelectUser(su)} className="text-gray-500 hover:text-red-500">×</button>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="p-4">
          <div className="relative mb-4">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search users to add..."
              className="w-full bg-gray-100 dark:bg-gray-800 border-none rounded-full py-2 pl-10 pr-4 focus:ring-2 focus:ring-blue-500 text-gray-900 dark:text-white placeholder-gray-500"
            />
            <SearchIcon size={18} className="absolute left-3 top-2.5 text-gray-500" />
          </div>

          <ul className="divide-y divide-gray-100 dark:divide-gray-800">
            {results.map(r => {
              const isSelected = !!selectedUsers.find(su => su.uid === r.uid);
              return (
                <li 
                  key={r.uid} 
                  onClick={() => toggleSelectUser(r)}
                  className="flex items-center px-2 py-3 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer"
                >
                  <div className="w-10 h-10 rounded-full bg-blue-500 flex items-center justify-center text-white font-semibold flex-shrink-0">
                    {r.displayName.charAt(0).toUpperCase()}
                  </div>
                  <div className="ml-3 flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{r.displayName}</p>
                    <p className="text-xs text-gray-500 truncate">@{r.username}</p>
                  </div>
                  <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${isSelected ? 'bg-blue-500 border-blue-500' : 'border-gray-300 dark:border-gray-600'}`}>
                    {isSelected && <span className="text-white text-sm">✓</span>}
                  </div>
                </li>
              );
            })}
          </ul>
        </div>
      </main>
    </div>
  );
};

export default CreateGroup;
