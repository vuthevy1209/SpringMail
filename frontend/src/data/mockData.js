export const folders = [
  { id: 'inbox', name: 'Inbox', icon: 'Inbox', count: 12 },
  { id: 'sent', name: 'Sent', icon: 'Send', count: 0 },
  { id: 'drafts', name: 'Drafts', icon: 'FileText', count: 3 },
  { id: 'trash', name: 'Trash', icon: 'Trash2', count: 0 },
  { id: 'llm', name: 'LLM Assisted', icon: 'Sparkles', count: 2 },
];

export const mockEmails = [
  {
    id: '1',
    sender: 'Elena Rostova',
    senderEmail: 'elena.rostova@synergy.com',
    subject: 'Q3 Partnership Agreement Initial Draft',
    snippet: 'Please find attached the initial draft for our upcoming Q3 partnership. Let me know what your legal team thinks.',
    timestamp: '10:42 AM',
    unread: true,
    folder: 'inbox',
    body: `Hi team,

I hope this email finds you well. 

As discussed in our sync yesterday, I have securely attached the initial draft for the Q3 Partnership Agreement. Our legal team has highlighted sections 4.2 and 5.1 as areas requiring your explicit approval before we move to final signatures.

Please review the document with your stakeholders. We are aiming to finalize everything by next Thursday to ensure a smooth transition into the next quarter.

Let me know if you need any clarification or if an impromptu call would be helpful.

Best regards,

Elena Rostova
Director of Partnerships, Synergy Corp`
  },
  {
    id: '2',
    sender: 'Kaelen Voss',
    senderEmail: 'kvoss@nexential.io',
    subject: 'Follow-up: Architecture Sync',
    snippet: 'Great speaking with you today. I am sending over the technical diagrams we reviewed.',
    timestamp: 'Yesterday',
    unread: false,
    folder: 'inbox',
    body: `Hello,

It was great speaking with you and the engineering leads today. 

As promised, please find the technical diagrams and the proposed system architecture documents attached. We've optimized the load balancer configuration per your team's feedback regarding traffic spikes.

Looking forward to your thoughts.

- Kaelen`
  },
  {
    id: '3',
    sender: 'System Notifications',
    senderEmail: 'no-reply@springmail.local',
    subject: 'Security Alert: New Sign-in',
    snippet: 'A new sign-in was detected on a Mac device in Seattle, WA.',
    timestamp: 'Mar 15',
    unread: true,
    folder: 'inbox',
    body: `Security Alert

We detected a new sign-in to your account from an unrecognized device.

Device: Mac OS
Location: Seattle, WA, USA
Time: March 15, 08:30 PST

If this was you, you can safely ignore this email. If you do not recognize this activity, please secure your account immediately.`
  },
  {
    id: '4',
    sender: 'Aria Chen',
    senderEmail: 'aria.design@studio.co',
    subject: 'Brand Guidelines V2',
    snippet: 'The typography updates are included in this version. The primary accent is set.',
    timestamp: 'Mar 12',
    unread: false,
    folder: 'inbox',
    body: `Hi,

I've pushed the latest updates to the brand guidelines (V2). The typography has been updated to Manrope as requested, and the primary accent color is strictly Emerald Green. 

Take a look and let me know if we are good to hand off to developers.

Thanks,
Aria`
  }
];
