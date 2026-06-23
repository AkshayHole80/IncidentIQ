import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import ProtectedRoute from './routes/ProtectedRoute';
import MainLayout from './layouts/MainLayout';
import { ConfigProvider, theme } from 'antd';

// Import Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import CreateIncident from './pages/CreateIncident';
import MyIncidents from './pages/MyIncidents';
import AssignedIncidents from './pages/AssignedIncidents';
import AllIncidents from './pages/AllIncidents';
import IncidentDetails from './pages/IncidentDetails';

function AppContent() {
  const { darkMode } = useAuth();

  return (
    <ConfigProvider
      theme={{
        algorithm: darkMode ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: {
          colorPrimary: '#1890ff',
          borderRadius: 8,
        },
      }}
    >
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected Routes with MainLayout */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Dashboard />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/incidents/create"
          element={
            <ProtectedRoute allowedRoles={['USER', 'ADMIN']}>
              <MainLayout>
                <CreateIncident />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/incidents/my"
          element={
            <ProtectedRoute allowedRoles={['USER', 'ADMIN']}>
              <MainLayout>
                <MyIncidents />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/incidents/assigned"
          element={
            <ProtectedRoute allowedRoles={['SUPPORT_ENGINEER']}>
              <MainLayout>
                <AssignedIncidents />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/incidents/all"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <MainLayout>
                <AllIncidents />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/incidents/:id"
          element={
            <ProtectedRoute>
              <MainLayout>
                <IncidentDetails />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Catch-all redirect to Dashboard */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </ConfigProvider>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <NotificationProvider>
          <AppContent />
        </NotificationProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
