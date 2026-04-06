import React, { useState } from 'react';
import { Sparkles, Reply, ListChecks, Send, X, Paperclip, FileText } from 'lucide-react';

export default function ReaderComposer({ selectedEmailId, emails = [] }) {
  const [composerOpen, setComposerOpen] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [draftContent, setDraftContent] = useState('');

  const email = emails.find(e => e.id === selectedEmailId);

  if (!email) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'var(--canvas-gray)' }}>
        <div style={{ textAlign: 'center', color: 'var(--muted-steel)' }}>
          <Sparkles size={32} style={{ margin: '0 auto 16px', opacity: 0.5 }} />
          <p>Select an email to read</p>
        </div>
      </div>
    );
  }

  const handleDraftReply = () => {
    setComposerOpen(true);
    setIsGenerating(true);
    // Simulating LLM generation time
    setTimeout(() => {
      setIsGenerating(false);
      setDraftContent("Thank you for sending this over.\n\nI will review the attached documents with the team and get back to you by early next week. Let's touch base again on Wednesday.\n\nBest,\nSpringMail User");
    }, 2000);
  };

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100vh', backgroundColor: 'var(--canvas-gray)', overflow: 'hidden' }}>
      
      {/* LLM Actions Toolbar */}
      <div style={{ 
        padding: '16px 24px', 
        borderBottom: '1px solid var(--whisper-border)',
        display: 'flex',
        gap: '12px',
        backgroundColor: 'var(--pure-surface)'
      }}>
        <button className="btn-outline" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Sparkles size={16} style={{ color: 'var(--emerald-accent)' }} />
          Summarize
        </button>
        <button className="btn-outline" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ListChecks size={16} />
          Extract Action Items
        </button>
        <div style={{ flex: 1 }} />
        <button 
          className="btn-primary" 
          onClick={handleDraftReply}
          style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
        >
          <Reply size={16} />
          Draft Reply
        </button>
      </div>

      {/* Reading Pane */}
      <div style={{ flex: 1, padding: '32px 48px', overflowY: 'auto' }}>
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
          <h1 style={{ fontSize: '24px', color: 'var(--charcoal-ink)', marginBottom: '24px' }}>
            {email.subject}
          </h1>

          {/* Sender Info */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ 
                width: '40px', 
                height: '40px', 
                flexShrink: 0
              }}>
                <img 
                  src={`https://ui-avatars.com/api/?name=${encodeURIComponent(email.senderName || 'Unknown')}&background=random&color=fff&rounded=true&bold=true`} 
                  alt={email.senderName} 
                  style={{ width: '100%', height: '100%' }} 
                />
              </div>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--charcoal-ink)' }}>
                  {email.senderName || (email.from && email.from.includes('<') ? email.from.split('<')[0].trim() : email.from)}
                </div>
                <div style={{ fontSize: '13px', color: 'var(--muted-steel)' }}>
                  {email.from && email.from.includes('<') ? '<' + email.from.split('<')[1] : ''}
                </div>
              </div>
            </div>
            <div style={{ color: 'var(--muted-steel)', fontSize: '14px' }}>
              {email.date ? new Date(email.date).toLocaleString() : ''}
            </div>
          </div>

          {/* Email Body */}
          <div 
            style={{ 
              color: 'var(--charcoal-ink)', 
              fontSize: '15px', 
              lineHeight: 1.6,
              wordWrap: 'break-word',
              overflowWrap: 'anywhere'
            }}
            dangerouslySetInnerHTML={{ __html: email.content }}
          />

          {/* Attachments */}
          {email.attachments && email.attachments.length > 0 && (
            <div style={{ marginTop: '32px', paddingTop: '24px', borderTop: '1px solid var(--whisper-border)' }}>
              <div style={{ fontWeight: 600, color: 'var(--charcoal-ink)', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Paperclip size={16} />
                {email.attachments.length} Attachments
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px' }}>
                {email.attachments.map((att, idx) => (
                  <div key={idx} style={{ 
                    display: 'flex', alignItems: 'center', gap: '8px', 
                    padding: '8px 12px', border: '1px solid var(--whisper-border)', 
                    borderRadius: '8px', backgroundColor: 'var(--pure-surface)',
                    fontSize: '13px', color: 'var(--charcoal-ink)',
                    boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
                  }}>
                    <FileText size={16} style={{ color: 'var(--emerald-accent)' }} />
                    <span style={{ maxWidth: '200px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={att}>{att}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* LLM Composer Pane */}
      {composerOpen && (
        <div style={{ 
          height: '40vh', 
          borderTop: '1px solid var(--whisper-border)',
          backgroundColor: 'var(--pure-surface)',
          padding: '24px 32px',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: '0 -4px 20px rgba(0,0,0,0.03)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--emerald-accent)', fontWeight: 600, fontSize: '14px' }}>
              <Sparkles size={16} />
              LLM Draft
            </div>
            <button onClick={() => setComposerOpen(false)} style={{ color: 'var(--muted-steel)' }}>
              <X size={20} />
            </button>
          </div>

          {isGenerating ? (
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ height: '12px', width: '90%', backgroundColor: 'var(--canvas-gray)', borderRadius: '4px', animation: 'pulse 1.5s infinite' }} />
              <div style={{ height: '12px', width: '100%', backgroundColor: 'var(--canvas-gray)', borderRadius: '4px', animation: 'pulse 1.5s infinite 0.2s' }} />
              <div style={{ height: '12px', width: '80%', backgroundColor: 'var(--canvas-gray)', borderRadius: '4px', animation: 'pulse 1.5s infinite 0.4s' }} />
            </div>
          ) : (
            <>
              <textarea 
                value={draftContent}
                onChange={(e) => setDraftContent(e.target.value)}
                style={{
                  flex: 1,
                  border: 'none',
                  outline: 'none',
                  resize: 'none',
                  fontFamily: 'var(--font-body)',
                  fontSize: '15px',
                  lineHeight: 1.6,
                  color: 'var(--charcoal-ink)',
                  marginBottom: '16px'
                }}
              />
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '12px', borderTop: '1px solid var(--whisper-border)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <button className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', borderRadius: 'var(--round-full)', padding: '8px 20px' }}>
                    <Send size={14} style={{ marginRight: '4px' }} />
                    Send
                  </button>
                  <div style={{ display: 'flex', gap: '12px', color: 'var(--muted-steel)' }}>
                    <button style={{ color: 'inherit' }}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m21.44 11.05-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></button>
                    <button style={{ color: 'inherit' }}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></button>
                    <button style={{ color: 'inherit' }}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" x2="9.01" y1="9" y2="9"/><line x1="15" x2="15.01" y1="9" y2="9"/></svg></button>
                    <button style={{ color: 'inherit' }}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg></button>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '12px' }}>
                  <button className="btn-outline" onClick={() => setComposerOpen(false)} style={{ border: 'none', background: 'transparent' }}>Discard</button>
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
                    Refine with AI
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      <style>{`
        @keyframes pulse {
          0%, 100% { opacity: 0.5; }
          50% { opacity: 1; }
        }
      `}</style>
    </div>
  );
}
