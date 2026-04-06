import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import api from '../service/api';
import Sidebar from '../components/Sidebar';
import InboxList from '../components/InboxList';
import ReaderComposer from '../components/ReaderComposer';
import ComposeModal from '../components/ComposeModal';

export default function InboxPage() {
    const [isAuthenticated, setIsAuthenticated] = useState(null);
    const [emails, setEmails] = useState([]);
    const [isLoadingEmails, setIsLoadingEmails] = useState(false);
    const [selectedEmailId, setSelectedEmailId] = useState(null);
    const [isComposeOpen, setIsComposeOpen] = useState(false);

    const [activeTab, setActiveTab] = useState('primary');

    useEffect(() => {
        api.get('/api/auth/me')
            .then(res => {
                setIsAuthenticated(true);
            })
            .catch(err => {
                setIsAuthenticated(false);
            });
    }, []);

    useEffect(() => {
        if (isAuthenticated) {
            setIsLoadingEmails(true);
            api.get(`/get-emails?category=${activeTab}`)
                .then(emailRes => {
                    setEmails(emailRes.data);
                })
                .finally(() => {
                    setIsLoadingEmails(false);
                });
        }
    }, [isAuthenticated, activeTab]);

    if (isAuthenticated === null) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: 'var(--canvas-gray)', color: 'var(--muted-steel)' }}>
                Loading session details...
            </div>
        );
    }

    if (isAuthenticated === false) {
        return <Navigate to="/login" replace />;
    }

    return (
        <div style={{ display: 'flex', width: '100vw', height: '100vh', overflow: 'hidden' }}>
            <Sidebar onCompose={() => setIsComposeOpen(true)} />
            <InboxList 
                emails={emails} 
                isLoading={isLoadingEmails}
                selectedEmailId={selectedEmailId} 
                onSelectEmail={setSelectedEmailId}
                activeTab={activeTab}
                onTabChange={setActiveTab}
            />
            <ReaderComposer 
                emails={emails}
                selectedEmailId={selectedEmailId} 
            />
            {isComposeOpen && <ComposeModal onClose={() => setIsComposeOpen(false)} />}
        </div>
    );
}
