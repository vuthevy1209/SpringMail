import React, { useEffect, useRef, useState } from 'react';

/**
 * Isolated component to render email HTML content within an iframe.
 * Prevents style leakage and provides a sandbox for the content.
 */
export default function EmailBody({ content }) {
    const iframeRef = useRef(null);
    const [height, setHeight] = useState('auto');

    useEffect(() => {
        const iframe = iframeRef.current;
        if (!iframe) return;

        const handleResize = () => {
            try {
                if (iframe.contentWindow && iframe.contentDocument && iframe.contentDocument.body) {
                    const doc = iframe.contentDocument;
                    // Reset margins on body to avoid extra spacing
                    doc.body.style.margin = '0';
                    doc.body.style.padding = '0';
                    
                    // Force a layout recalculation
                    const newHeight = doc.documentElement.scrollHeight;
                    setHeight(`${newHeight}px`);
                }
            } catch (err) {
                console.error('Error resizing email iframe:', err);
            }
        };

        iframe.onload = () => {
            handleResize();
            setTimeout(handleResize, 100);
            setTimeout(handleResize, 500);
            setTimeout(handleResize, 2000);
        };

        let observer;
        try {
            if (iframe.contentWindow && iframe.contentDocument && iframe.contentDocument.body) {
                observer = new ResizeObserver(() => handleResize());
                observer.observe(iframe.contentDocument.body);
            }
        } catch (e) {
            // Observer failed
        }

        return () => {
            if (observer) observer.disconnect();
        };
    }, [content]);

    return (
        <iframe
            ref={iframeRef}
            srcDoc={content}
            title="Email Content"
            style={{
                width: '100%',
                border: 'none',
                overflow: 'hidden',
                backgroundColor: 'transparent',
                display: 'block',
                height
            }}
            sandbox="allow-popups allow-popups-to-escape-sandbox allow-same-origin"
            loading="lazy"
        />
    );
}
