import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Edit2, ChevronRight, ExternalLink, LogOut, Moon } from 'lucide-react';
import { useAuthStore } from '../store/authStore';

const Settings: React.FC = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const [isDarkMode, setIsDarkMode] = useState(() => {
    return document.documentElement.classList.contains('dark');
  });

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

  useEffect(() => {
    // Sync state on load
    setIsDarkMode(document.documentElement.classList.contains('dark'));
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

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
          <div className="w-24 h-24 bg-[#2196F3] rounded-full flex items-center justify-center text-white text-4xl font-bold shadow-sm">
            {user?.displayName ? user.displayName.charAt(0).toUpperCase() : 'U'}
          </div>
          <h2 className="mt-4 text-2xl font-bold text-gray-900 dark:text-white">
            {user?.displayName || 'User'}
          </h2>
          <p className="text-gray-500 dark:text-gray-400 text-lg">
            @{user?.username?.replace('@drift.app', '') || 'username'}
          </p>
        </div>

        {/* Settings List */}
        <div className="bg-white dark:bg-gray-800 border-y border-gray-200 dark:border-gray-700">
          
          {/* Display Name */}
          <div className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750">
            <div>
              <p className="text-gray-900 dark:text-white font-medium">Display Name</p>
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
          <div className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750">
            <p className="text-gray-900 dark:text-white font-medium">About Drift</p>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </div>

          {/* Terms of Service */}
          <a 
            href="https://drift.app/terms" 
            target="_blank" 
            rel="noopener noreferrer"
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-gray-900 dark:text-white font-medium">Terms of Service</p>
            <ExternalLink className="w-5 h-5 text-gray-400" />
          </a>

          {/* Privacy Policy */}
          <a 
            href="https://drift.app/privacy" 
            target="_blank" 
            rel="noopener noreferrer"
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-gray-900 dark:text-white font-medium">Privacy Policy</p>
            <ExternalLink className="w-5 h-5 text-gray-400" />
          </a>

          {/* Log Out */}
          <div 
            onClick={handleLogout}
            className="flex items-center p-4 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-red-500 font-medium">Log Out</p>
          </div>

        </div>

        <div className="flex justify-center mt-8">
          <p className="text-gray-400 dark:text-gray-500 text-sm">v2.0</p>
        </div>
      </div>
    </div>
  );
};

export default Settings;
