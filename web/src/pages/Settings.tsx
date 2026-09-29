import React, { useState, useEffect } from 'react';
import { ArrowLeft, Edit2, LogOut, ChevronRight, Trash2 } from 'lucide-react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

const Settings = () => {
  const navigate = useNavigate();
  const { user, logout, updateDisplayName, deleteAccount } = useAuthStore();
  const [showEditName, setShowEditName] = useState(false);
  const [showAbout, setShowAbout] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [newName, setNewName] = useState(user?.displayName || '');
  const [isDarkMode, setIsDarkMode] = useState(() => {
    return localStorage.getItem('darkMode') === 'true' || 
           (!('darkMode' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches);
  });
  
  const userColor = React.useMemo(() => {
    const colors = ['#2196F3', '#E91E63', '#4CAF50', '#FF9800', '#9C27B0'];
    const charCode = user?.displayName ? user.displayName.charCodeAt(0) : 0;
    return colors[charCode % colors.length];
  }, [user?.displayName]);

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('darkMode', 'true');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('darkMode', 'false');
    }
  }, [isDarkMode]);

  const toggleDarkMode = () => setIsDarkMode(!isDarkMode);

  const handleUpdateName = async () => {
    if (newName.trim() && newName.trim() !== user?.displayName) {
      await updateDisplayName(newName.trim());
    }
    setShowEditName(false);
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleDeleteAccount = async () => {
    await deleteAccount();
    navigate('/login');
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-200">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 flex items-center p-4 border-b border-gray-200 dark:border-gray-700 shadow-sm z-10">
        <button 
          onClick={() => navigate(-1)} 
          className="p-2 -ml-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors text-gray-900 dark:text-white"
        >
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="ml-2 text-xl font-semibold text-gray-900 dark:text-white">Settings</h1>
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

          <div 
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
            onClick={() => setShowAbout(true)}
          >
            <p className="text-gray-900 dark:text-white font-medium">About Drift</p>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </div>

          <Link 
            to="/terms"
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-gray-900 dark:text-white font-medium">Terms of Service</p>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </Link>

          <Link 
            to="/privacy"
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-gray-900 dark:text-white font-medium">Privacy Policy</p>
            <ChevronRight className="w-5 h-5 text-gray-400" />
          </Link>

          <div 
            onClick={() => setShowDeleteConfirm(true)}
            className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-750"
          >
            <p className="text-red-500 font-medium">Delete Account</p>
            <Trash2 className="w-5 h-5 text-red-500" />
          </div>

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

      <Dialog open={showEditName} onOpenChange={setShowEditName}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Edit Display Name</DialogTitle>
            <DialogDescription>
              Make changes to your display name here. Click save when you're done.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                className="col-span-3"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowEditName(false)}>Cancel</Button>
            <Button onClick={handleUpdateName}>Save changes</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showAbout} onOpenChange={setShowAbout}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>About Drift</DialogTitle>
            <DialogDescription>
              Drift is a distraction-free messaging app designed to help you connect with others quickly and securely.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => setShowAbout(false)}>Close</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={showDeleteConfirm} onOpenChange={setShowDeleteConfirm}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle className="text-destructive">Delete Account?</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete your account? This action cannot be undone and will permanently remove your data.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDeleteConfirm(false)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDeleteAccount}>Delete Account</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Settings;
