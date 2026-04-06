import React, { useState } from 'react';
import { X, Maximize2, Minus, Paperclip, Link, Smile, Image as ImageIcon, Sparkles, Send } from 'lucide-react';

export default function ComposeModal({ onClose }) {
  const [recipient, setRecipient] = useState('');
  const [subject, setSubject] = useState('');
  const [body, setBody] = useState('');

  return (
    <div style={{
      position: 'fixed',
      bottom: '24px',
      right: '80px',
      width: '500px',
      height: '500px',
      backgroundColor: 'var(--pure-surface)',
      border: '1px solid var(--whisper-border)',
      borderRadius: '12px',
      boxShadow: '0 8px 30px rgba(0, 0, 0, 0.12)',
      display: 'flex',
      flexDirection: 'column',
      zIndex: 1000,
      overflow: 'hidden'
    }}>
      {/* Header */}
      <div style={{
        backgroundColor: 'var(--canvas-gray)',
        padding: '12px 16px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderBottom: '1px solid var(--whisper-border)'
      }}>
        <div style={{ fontWeight: 600, fontSize: '14px', color: 'var(--charcoal-ink)' }}>New Message</div>
        <div style={{ display: 'flex', gap: '12px', color: 'var(--muted-steel)' }}>
          <button style={{ color: 'inherit' }}><Minus size={16} /></button>
          <button style={{ color: 'inherit' }}><Maximize2 size={16} /></button>
          <button style={{ color: 'inherit' }} onClick={onClose}><X size={16} /></button>
        </div>
      </div>

      {/* Form Fields */}
      <div style={{ padding: '0 16px' }}>
        <input 
          type="text" 
          placeholder="To" 
          value={recipient}
          onChange={(e) => setRecipient(e.target.value)}
          style={{
            width: '100%',
            padding: '12px 0',
            border: 'none',
            borderBottom: '1px solid var(--whisper-border)',
            outline: 'none',
            fontSize: '14px',
            fontFamily: 'var(--font-body)',
            color: 'var(--charcoal-ink)'
          }}
        />
        <input 
          type="text" 
          placeholder="Subject" 
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          style={{
            width: '100%',
            padding: '12px 0',
            border: 'none',
            borderBottom: '1px solid var(--whisper-border)',
            outline: 'none',
            fontSize: '14px',
            fontFamily: 'var(--font-body)',
            color: 'var(--charcoal-ink)',
            fontWeight: 600
          }}
        />
      </div>

      {/* Body Area */}
      <div style={{ flex: 1, padding: '16px', position: 'relative' }}>
        <textarea
          value={body}
          onChange={(e) => setBody(e.target.value)}
          style={{
            width: '100%',
            height: '100%',
            border: 'none',
            outline: 'none',
            resize: 'none',
            fontSize: '15px',
            lineHeight: 1.6,
            fontFamily: 'var(--font-body)',
            color: 'var(--charcoal-ink)'
          }}
        />
      </div>

      {/* Footer / Toolbar */}
      <div style={{
        padding: '12px 16px',
        borderTop: '1px solid var(--whisper-border)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        backgroundColor: 'var(--pure-surface)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ display: 'flex' }}>
            <button 
              className="btn-primary" 
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                borderRadius: 'var(--round-full)',
                padding: '8px 20px'
              }}
            >
              <Send size={14} style={{ marginRight: '4px' }} />
              Send
            </button>
          </div>
          
          <div style={{ display: 'flex', gap: '12px', color: 'var(--muted-steel)' }}>
            <button style={{ color: 'inherit' }}><Paperclip size={18} /></button>
            <button style={{ color: 'inherit' }}><Link size={18} /></button>
            <button style={{ color: 'inherit' }}><Smile size={18} /></button>
            <button style={{ color: 'inherit' }}><ImageIcon size={18} /></button>
          </div>
        </div>

        <div>
          <button style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            color: 'var(--emerald-accent)',
            fontWeight: 600,
            fontSize: '13px',
            padding: '6px 12px',
            backgroundColor: 'var(--canvas-gray)',
            borderRadius: 'var(--round-full)'
          }}>
            <Sparkles size={14} />
            Write with AI
          </button>
        </div>
      </div>
    </div>
  );
}
