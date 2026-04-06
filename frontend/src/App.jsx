import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import InboxPage from './pages/InboxPage';
import MainLayout from './components/layout/MainLayout';

export default function App() {
    return (
        <Routes>
            {/* Root redirect */}
            <Route path="/" element={<Navigate to="/inbox" replace />} />
            
            {/* Login Route (no layout) */}
            <Route path="/login" element={<LoginPage />} />
            
            {/* App Routes (with layout) */}
            <Route element={<MainLayout />}>
                <Route path="/inbox" element={<InboxPage />} />
            </Route>
        </Routes>
    );
}
