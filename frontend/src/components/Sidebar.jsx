import React from 'react';
import { folders } from '../data/mockData';
import { Inbox, Send, FileText, Trash2, Sparkles, Settings, Mail } from 'lucide-react';

const iconMap = {
  Inbox: Inbox,
  Send: Send,
  FileText: FileText,
  Trash2: Trash2,
  Sparkles: Sparkles,
};

export default function Sidebar({ onCompose }) {
  return (
    <div style={{
      width: '240px',
      backgroundColor: 'var(--canvas-gray)',
      borderRight: '1px solid var(--whisper-border)',
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      padding: '24px 16px'
    }}>
      {/* Brand */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px', padding: '0 8px' }}>
        <div style={{ 
          backgroundColor: 'var(--emerald-accent)', 
          color: 'white', 
          padding: '6px', 
          borderRadius: '8px'
        }}>
          <Mail size={18} />
        </div>
        <span style={{ fontWeight: 700, letterSpacing: '-0.02em', fontSize: '18px' }}>SpringMail</span>
      </div>

      {/* Compose Button */}
      <button 
        onClick={onCompose}
        className="btn-primary"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '12px',
          borderRadius: 'var(--round-8)',
          marginBottom: '24px',
          boxShadow: '0 4px 12px rgba(16, 185, 129, 0.2)'
        }}
      >
        <Sparkles size={18} />
        Compose
      </button>

      {/* Folders */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 }}>
        {folders.map(folder => {
          const Icon = iconMap[folder.icon];
          const isActive = folder.id === 'inbox'; // Simple mock active state
          
          return (
            <button key={folder.id} style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '10px 12px',
              borderRadius: 'var(--round-8)',
              backgroundColor: isActive ? 'var(--pure-surface)' : 'transparent',
              color: isActive ? 'var(--charcoal-ink)' : 'var(--muted-steel)',
              fontWeight: isActive ? 600 : 500,
              boxShadow: isActive ? '0 1px 2px rgba(0,0,0,0.02)' : 'none',
              border: isActive ? '1px solid var(--whisper-border)' : '1px solid transparent',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <Icon size={18} style={{ color: isActive ? 'var(--emerald-accent)' : 'inherit' }} />
                <span style={{ fontSize: '14px' }}>{folder.name}</span>
              </div>
              {folder.count > 0 && (
                <span style={{ 
                  fontSize: '12px', 
                  backgroundColor: isActive ? 'var(--emerald-accent)' : 'transparent',
                  color: isActive ? 'white' : 'inherit',
                  padding: '2px 8px',
                  borderRadius: '12px',
                  fontWeight: 600
                }}>
                  {folder.count}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* Settings */}
      <div style={{ marginTop: 'auto' }}>
        <button style={{
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '10px 12px',
          width: '100%',
          color: 'var(--muted-steel)',
          fontWeight: 500,
          borderRadius: 'var(--round-8)'
        }}>
          <Settings size={18} />
          <span style={{ fontSize: '14px' }}>Settings</span>
        </button>
      </div>
    </div>
  );
}
