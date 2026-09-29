import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import { StreamProvider } from './hooks/useStreamClient';

// Pages
import Login from './pages/Login';
import SignUp from './pages/SignUp';
import ChatList from './pages/ChatList';
import ChatScreen from './pages/ChatScreen';
import Settings from './pages/Settings';
import Search from './pages/Search';
import CreateGroup from './pages/CreateGroup';
import Terms from './pages/Terms';
import Privacy from './pages/Privacy';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { user, initialized } = useAuthStore();
  
  if (!initialized) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

const App: React.FC = () => {
  const { listenToAuth } = useAuthStore();

  useEffect(() => {
    listenToAuth();
  }, [listenToAuth]);

  return (
    <StreamProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <ChatList />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/chat/:id" 
            element={
              <ProtectedRoute>
                <ChatScreen />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/settings" 
            element={
              <ProtectedRoute>
                <Settings />
              </ProtectedRoute>
            } 
          />
          <Route path="/search" element={<ProtectedRoute><Search /></ProtectedRoute>} />
          <Route path="/create-group" element={<ProtectedRoute><CreateGroup /></ProtectedRoute>} />
          <Route path="/terms" element={<Terms />} />
          <Route path="/privacy" element={<Privacy />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </StreamProvider>
  );
};

export default App;
