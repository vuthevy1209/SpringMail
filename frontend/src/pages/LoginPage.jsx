import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail } from 'lucide-react';

export default function LoginPage() {
    const navigate = useNavigate();

    const handleLogin = () => {
        // Redirect thẳng trình duyệt sang cổng 8080 của Backend để bắt đầu luồng OAuth2 Google
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>

                {/* Branding */}
                <div style={styles.brandingContainer}>
                    <div style={styles.iconBox}>
                        <Mail size={24} />
                    </div>
                    <div>
                        <h1 style={styles.title}>SpringMail</h1>
                        <p style={styles.subtitle}>
                            The modern, AI-integrated inbox.
                        </p>
                    </div>
                </div>

                {/* Login Action */}
                <button
                    onClick={handleLogin}
                    className="btn-outline"
                    style={styles.loginButton}
                >
                    <GoogleIcon style={styles.logoSvg} />
                    Sign in with Google
                </button>

            </div>
        </div>
    );
}

const GoogleIcon = ({ style }) => (
    <svg style={style} viewBox="0 0 24 24">
        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
);

const styles = {
    container: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        backgroundColor: 'var(--canvas-gray)'
    },
    card: {
        backgroundColor: 'var(--pure-surface)',
        padding: '64px',
        borderRadius: 'var(--round-8)',
        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.05)',
        textAlign: 'center',
        maxWidth: '400px',
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '32px'
    },
    brandingContainer: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '16px'
    },
    iconBox: {
        width: '48px',
        height: '48px',
        backgroundColor: 'var(--emerald-accent)',
        borderRadius: '12px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: 'white'
    },
    title: {
        fontSize: '24px',
        letterSpacing: '-0.02em',
        color: 'var(--charcoal-ink)'
    },
    subtitle: {
        color: 'var(--muted-steel)',
        fontSize: '14px',
        marginTop: '4px'
    },
    loginButton: {
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        width: '100%',
        justifyContent: 'center',
        padding: '12px 16px',
        fontSize: '15px'
    },
    logoSvg: {
        width: '18px',
        height: '18px'
    }
};
