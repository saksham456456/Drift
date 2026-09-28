import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Edit2, ChevronRight, LogOut, Trash2 } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { db } from '../firebase';
import { ref, update, remove, get } from 'firebase/database';
import { getAuth, deleteUser } from 'firebase/auth';

// Simple deterministic color generator from string
const stringToColor = (str: string) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  const c = (hash & 0x00FFFFFF).toString(16).toUpperCase();
  return '#' + '000000'.substring(0, 6 - c.length) + c;
};

const Settings: React.FC = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  
  const [isDarkMode, setIsDarkMode] = useState(() => {
    return document.documentElement.classList.contains('dark');
  });

  const [showEditName, setShowEditName] = useState(false);
  const [newName, setNewName] = useState(user?.displayName || '');
  const [showAbout, setShowAbout] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  useEffect(() => {
    setIsDarkMode(document.documentElement.classList.contains('dark'));
  }, []);

  const toggleDarkMode = () => {
    const html = document.documentElement;
    if (html.classList.contains('dark')) {
      html.classList.remove('dark');
      localStorage.setItem('theme', 'light');
      setIsDarkMode(false);
    } else {
      html.classList.add('dark');
      localStorage.setItem('theme', 'dark');
      setIsDarkMode(true);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleUpdateName = async () => {
    if (!user || !newName.trim()) return;
    try {
      await update(ref(db, `users/${user.uid}`), {
        displayName: newName.trim()
      });
      setShowEditName(false);
      // To strictly follow, authStore might need update if we were refreshing, but firebase realtime sync usually handles it or we reload
      window.location.reload();
    } catch (error) {
      console.error('Error updating name:', error);
    }
  };

  const handleDeleteAccount = async () => {
    if (!user) return;
    try {
      const auth = getAuth();
      const currentUser = auth.currentUser;
      
      if (currentUser) {
        // Remove username mapping
        if (user.username) {
          await remove(ref(db, `usernames/${user.username.replace('@', '')}`));
        }
        // Remove user data
        await remove(ref(db, `users/${user.uid}`));
        // Delete auth user
        await deleteUser(currentUser);
        // authStore will handle logout on state change
        navigate('/login');
      }
    } catch (error) {
      console.error('Error deleting account:', error);
      alert('Failed to delete account. You may need to log in again before doing this.');
    }
  };

  const userColor = user?.displayName ? stringToColor(user.displayName) : '#2196F3';

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col transition-colors">
      {/* Top Bar */}
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center p-4">
        <button onClick={() => navigate(-1)} className="mr-4 text-gray-700 dark:text-gray-200">
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="text-xl font-semibold text-gray-900 dark:text-white">Settings</h1>
      </div>

      <div className="flex-1 overflow-y-auto pb-8">
        {/* Profile Header */}
        <div className="flex flex-col items-center py-8">
          <div 
            className="w-24 h-24 rounded-full flex items-center justify-center text-white text-4xl font-bold shadow-sm"
            style={{ backgroundColor: userColor }}
          >
            {user?.displayName ? user.displayName.charAt(0).toUpperCase() : 'U'}
          </div>
          <h2 className="mt-4 text-2xl font-bold text-gray-900 dark:text-white">
            {user?.displayName || 'User'}
          </h2>
          <p className="text-gray-500 dark:text-gray-400 text-lg">
            {user?.username ? (user.username.startsWith('@') ? user.username : `@${user.username}`) : '@username'}
          </p>
        </div>

        {/* Settings List */}
        <div className="bg-white dark:bg-gray-800 border-y border-gray-200 dark:border-gray-700">
          
          {/* Display Name */}
          <div 
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
            onClick={() => setShowEditName(true)}
          >
            <div>
              <p className="text-gray-900 dark:text-white font-medium">Edit Display Name</p>
              <p className="text-gray-500 dark:text-gray-400 text-sm">{user?.displayName}</p>
            </div>
            <Edit2 className="w-5 h-5 text-gray-400" />
          </div>

          {/* Dark Mode */}
          <div 
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
            onClick={toggleDarkMode}
          >
            <div>
              <p className="text-gray-900 dark:text-white font-medium">Dark Mode</p>
              <p className="text-gray-500 dark:text-gray-400 text-sm">Toggle dark theme</p>
            </div>
            <button className={`w-11 h-6 rounded-full transition-colors relative ${isDarkMode ? 'bg-[#2196F3]' : 'bg-gray-300 dark:bg-gray-600'}`}>
              <div className={`w-4 h-4 bg-white rounded-full absolute top-1 transition-transform ${isDarkMode ? 'translate-x-6' : 'translate-x-1'}`} />
            </button>
          </div>

          {/* About */}
          <div 
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
            onClick={() => setShowAbout(true)}
          >
            <p className="text-gray-900 dark:text-white font-medium">About Drift</p>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </div>

          {/* Terms of Service */}
          <Link 
            to="/terms"
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-gray-900 dark:text-white font-medium">Terms of Service</p>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </Link>

          {/* Privacy Policy */}
          <Link 
            to="/privacy"
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-gray-900 dark:text-white font-medium">Privacy Policy</p>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </Link>

          {/* Delete Account */}
          <div 
            onClick={() => setShowDeleteConfirm(true)}
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-red-500 font-medium">Delete Account</p>
            <Trash2 className="w-5 h-5 text-red-500" />
          </div>

          {/* Log Out */}
          <div 
            onClick={handleLogout}
            className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-red-500 font-medium">Log Out</p>
            <LogOut className="w-5 h-5 text-red-500" />
          </div>

        </div>

        <div className="flex justify-center mt-8">
          <p className="text-gray-400 dark:text-gray-500 text-sm">v2.0</p>
        </div>
      </div>

      {/* Edit Name Modal */}
      {showEditName && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-sm">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">Edit Display Name</h3>
            <input
              type="text"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              className="w-full border border-gray-300 dark:border-gray-600 rounded-lg p-2 mb-4 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Display Name"
            />
            <div className="flex justify-end gap-2">
              <button 
                onClick={() => setShowEditName(false)}
                className="px-4 py-2 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button 
                onClick={handleUpdateName}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}

      {/* About Modal */}
      {showAbout && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-sm">
            <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">About Drift</h3>
            <p className="text-gray-600 dark:text-gray-300 mb-4">
              Drift is a distraction-free messaging app designed to help you connect with others quickly and securely.
            </p>
            <div className="flex justify-end">
              <button 
                onClick={() => setShowAbout(false)}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Account Modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-sm">
            <h3 className="text-lg font-bold text-red-500 mb-2">Delete Account?</h3>
            <p className="text-gray-600 dark:text-gray-300 mb-4">
              Are you sure you want to delete your account? This action cannot be undone and will permanently remove your data.
            </p>
            <div className="flex justify-end gap-2">
              <button 
                onClick={() => setShowDeleteConfirm(false)}
                className="px-4 py-2 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button 
                onClick={handleDeleteAccount}
                className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Settings;
