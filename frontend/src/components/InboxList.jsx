import React from 'react';
import { mockEmails } from '../data/mockData';
import { Search } from 'lucide-react';

export default function InboxList({ selectedEmailId, onSelectEmail }) {
  return (
    <div style={{
      width: '380px',
      backgroundColor: 'var(--pure-surface)',
      borderRight: '1px solid var(--whisper-border)',
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
    }}>
      {/* Header & Search */}
      <div style={{
        padding: '24px 20px 16px',
        borderBottom: '1px solid var(--whisper-border)'
      }}>
        <h2 style={{ fontSize: '20px', marginBottom: '16px', color: 'var(--charcoal-ink)' }}>Inbox</h2>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          backgroundColor: 'var(--canvas-gray)',
          padding: '8px 12px',
          borderRadius: 'var(--round-8)',
          gap: '8px'
        }}>
          <Search size={16} style={{ color: 'var(--muted-steel)' }} />
          <input 
            type="text" 
            placeholder="Search mail or ask AI..." 
            style={{
              border: 'none',
              background: 'none',
              outline: 'none',
              width: '100%',
              fontSize: '14px',
              fontFamily: 'var(--font-body)',
              color: 'var(--charcoal-ink)'
            }}
          />
        </div>
      </div>

      {/* Email List */}
      <div style={{ flex: 1, overflowY: 'auto' }}>
        {mockEmails.map((email) => {
          const isSelected = email.id === selectedEmailId;
          
          return (
            <div 
              key={email.id}
              onClick={() => onSelectEmail(email.id)}
              style={{
                padding: '16px 20px',
                borderBottom: '1px solid var(--whisper-border)',
                cursor: 'pointer',
                backgroundColor: isSelected ? 'var(--canvas-gray)' : 'transparent',
                transition: 'background-color 0.2s',
                display: 'flex',
                gap: '12px'
              }}
            >
              {/* Unread Indicator */}
              <div style={{ width: '8px', paddingTop: '6px' }}>
                {email.unread && (
                  <div style={{
                    width: '8px',
                    height: '8px',
                    backgroundColor: 'var(--emerald-accent)',
                    borderRadius: '50%'
                  }} />
                )}
              </div>

              {/* Content */}
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '4px' }}>
                  <span style={{ 
                    fontWeight: email.unread ? 700 : 600, 
                    color: 'var(--charcoal-ink)',
                    fontSize: '14px',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis'
                  }}>
                    {email.sender}
                  </span>
                  <span style={{ fontSize: '12px', color: 'var(--muted-steel)', whiteSpace: 'nowrap', marginLeft: '8px' }}>
                    {email.timestamp}
                  </span>
                </div>
                <div style={{ 
                  fontWeight: email.unread ? 600 : 500,
                  fontSize: '14px',
                  color: 'var(--charcoal-ink)',
                  marginBottom: '4px',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis'
                }}>
                  {email.subject}
                </div>
                <div style={{
                  fontSize: '13px',
                  color: 'var(--muted-steel)',
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden',
                  lineHeight: '1.4'
                }}>
                  {email.snippet}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
