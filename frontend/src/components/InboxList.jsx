import React from 'react';
import { Search, Loader2, Paperclip, Inbox, Users, Tag, Info } from 'lucide-react';

export default function InboxList({ selectedEmailId, onSelectEmail, emails = [], isLoading = false, activeTab = 'primary', onTabChange }) {
  const tabs = [
    { id: 'primary', label: 'Primary', icon: Inbox },
    { id: 'promotions', label: 'Promotions', icon: Tag },
    { id: 'social', label: 'Social', icon: Users },
    { id: 'updates', label: 'Updates', icon: Info }
  ];
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

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '1px solid var(--whisper-border)', backgroundColor: 'var(--canvas-gray)' }}>
        {tabs.map(tab => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange && onTabChange(tab.id)}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                padding: '12px 0',
                border: 'none',
                background: 'none',
                cursor: 'pointer',
                color: isActive ? 'var(--emerald-primary)' : 'var(--muted-steel)',
                borderBottom: isActive ? '2px solid var(--emerald-primary)' : '2px solid transparent',
                transition: 'all 0.2s',
                gap: '4px'
              }}
            >
              <Icon size={18} />
              <span style={{ fontSize: '11px', fontWeight: isActive ? 600 : 500 }}>{tab.label}</span>
            </button>
          )
        })}
      </div>

      {/* Email List */}
      <div style={{ flex: 1, overflowY: 'auto' }}>
        {isLoading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '40px 0', color: 'var(--muted-steel)' }}>
            <Loader2 className="animate-spin" size={24} />
          </div>
        ) : emails.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--muted-steel)', fontSize: '14px' }}>
            No emails found.
          </div>
        ) : (
          emails.map((email) => {
            const isSelected = email.id === selectedEmailId;
            // Xử lý senderName
            const senderName = email.senderName || 'Unknown';
            const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(senderName)}&background=random&color=fff&rounded=true&bold=true`;

            return (
              <div 
                key={email.id}
                onClick={() => onSelectEmail(email.id)}
                style={{
                  padding: '16px 20px',
                  borderBottom: '1px solid var(--whisper-border)',
                  cursor: 'pointer',
                  backgroundColor: isSelected ? 'var(--canvas-gray)' : email.unread ? '#f9fafa' : 'transparent',
                  transition: 'background-color 0.2s',
                  display: 'flex',
                  gap: '12px'
                }}
              >
                {/* Avatar (with Unread Indicator overlay potential) */}
                <div style={{ position: 'relative', width: '40px', height: '40px', flexShrink: 0 }}>
                  <img src={avatarUrl} alt={senderName} style={{ width: '100%', height: '100%' }} />
                  {email.unread && (
                    <div style={{
                      position: 'absolute',
                      top: '-2px',
                      right: '-2px',
                      width: '10px',
                      height: '10px',
                      backgroundColor: 'var(--emerald-accent)',
                      borderRadius: '50%',
                      border: '2px solid #fff'
                    }} />
                  )}
                </div>

                {/* Content */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '4px' }}>
                    <span style={{ 
                      fontWeight: email.unread ? 700 : 500, 
                      color: 'var(--charcoal-ink)',
                      fontSize: '14px',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis'
                    }}>
                      {senderName}
                    </span>
                    <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                      {email.attachments && email.attachments.length > 0 && (
                        <Paperclip size={14} style={{ color: 'var(--muted-steel)' }} />
                      )}
                      <span style={{ 
                        fontSize: '12px', 
                        color: email.unread ? 'var(--charcoal-ink)' : 'var(--muted-steel)', 
                        fontWeight: email.unread ? 600 : 400,
                        whiteSpace: 'nowrap'
                      }}>
                        {email.date ? new Date(email.date).toLocaleDateString() : ''}
                      </span>
                    </div>
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
          })
        )}
      </div>
    </div>
  );
}
