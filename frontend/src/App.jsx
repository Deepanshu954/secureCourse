import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToggleProvider } from './context/ToggleContext';
import Navbar from './components/Navbar';
import SecurityTogglePanel from './components/SecurityTogglePanel';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import CourseView from './pages/CourseView';

const PrivateRoute = ({ children }) => {
    const { user, loading } = useAuth();
    if (loading) return <div>Loading...</div>;
    return user ? children : <Navigate to="/login" />;
};

const App = () => {
    return (
        <AuthProvider>
            <ToggleProvider>
                <Router>
                    <div className="app-container">
                        <Navbar />
                        <SecurityTogglePanel />
                        <div className="content">
                            <Routes>
                                <Route path="/login" element={<Login />} />
                                <Route path="/signup" element={<Signup />} />
                                <Route path="/dashboard" element={
                                    <PrivateRoute>
                                        <Dashboard />
                                    </PrivateRoute>
                                } />
                                <Route path="/course/:id" element={
                                    <PrivateRoute>
                                        <CourseView />
                                    </PrivateRoute>
                                } />
                                <Route path="/" element={<Navigate to="/dashboard" />} />
                            </Routes>
                        </div>
                    </div>
                </Router>
            </ToggleProvider>
        </AuthProvider>
    );
};

export default App;
