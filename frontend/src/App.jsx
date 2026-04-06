import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import InboxPage from './pages/InboxPage';

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/inbox" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/inbox" element={<InboxPage />} />
        </Routes>
    );
}
