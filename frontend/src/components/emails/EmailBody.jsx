import React, { useEffect, useRef, useState, useMemo } from 'react';

/**
 * Isolated component to render email HTML content within an iframe.
 * Prevents style leakage and provides a sandbox for the content.
 */
export default function EmailBody({ content, messageId, attachments = [] }) {
    const iframeRef = useRef(null);
    const [height, setHeight] = useState('60px'); // Set a small initial height

    // Process the content to resolve cid: images
    const processedContent = useMemo(() => {
        if (!content) return '';
        
        let newContent = content;
        const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

        // Replace cid: links with backend attachment URLs or Data URIs
        if (attachments && attachments.length > 0) {
            const cidRegex = /src=(['"]?)cid:([^'"]+)\1/gi;
            newContent = newContent.replace(cidRegex, (match, quote, cid) => {
                const normalizedCid = cid.replace(/[<>]/g, '').trim();
                
                const attachment = attachments.find(att => {
                    if (!att.contentId) return false;
                    const normalizedAttCid = att.contentId.replace(/[<>]/g, '').trim();
                    return normalizedAttCid === normalizedCid;
                });

                if (attachment) {
                    if (attachment.data) {
                        const base64Data = attachment.data.replace(/-/g, '+').replace(/_/g, '/');
                        return `src="data:${attachment.mimeType};base64,${base64Data}"`;
                    }
                    if (attachment.id) {
                        return `src="${baseUrl}/mail/attachments?messageId=${messageId}&attachmentId=${attachment.id}"`;
                    }
                }
                return match;
            });
        }

        // Inject CSS để tránh ảnh quá to làm vỡ layout
        const styleTag = `
            <style>
                img {
                    max-width: 100% !important;
                    height: auto !important;
                    display: block;
                }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    line-height: 1.5;
                    color: #333;
                    margin: 0;
                    padding: 0;
                    overflow: hidden; /* Prevent iframe scrollbars */
                    word-wrap: break-word;
                }
                .email-container {
                    padding: 1px 0; /* Avoid margin collapsing */
                }
            </style>
        `;

        // Wrap in a container for better height calculation
        return `
            <html>
                <head>${styleTag}</head>
                <body>
                    <div class="email-container">
                        ${newContent}
                    </div>
                </body>
            </html>
        `;
    }, [content, messageId, attachments]);

    useEffect(() => {
        const iframe = iframeRef.current;
        if (!iframe) return;

        const handleResize = () => {
            try {
                const doc = iframe.contentDocument;
                if (doc && doc.body) {
                    // Use offsetHeight of the body or scrollHeight of the documentElement
                    // documentElement.scrollHeight might be too large if there's padding/margin
                    const body = doc.body;
                    const html = doc.documentElement;
                    
                    const newHeight = Math.max(
                        body.scrollHeight, 
                        body.offsetHeight, 
                        html.offsetHeight, 
                        html.scrollHeight
                    );
                    
                    if (newHeight > 0) {
                        setHeight(`${newHeight}px`);
                    }
                }
            } catch (err) {
                // Ignore cross-origin errors (though srcDoc should be same-origin)
            }
        };

        // Attach resize event to the window inside iframe if possible
        if (iframe.contentWindow) {
            iframe.contentWindow.addEventListener('resize', handleResize);
        }

        iframe.onload = () => {
            handleResize();
            // Multiple attempts as images or fonts might load later
            setTimeout(handleResize, 50);
            setTimeout(handleResize, 200);
            setTimeout(handleResize, 1000);
        };

        let observer;
        try {
            if (iframe.contentDocument && iframe.contentDocument.body) {
                observer = new ResizeObserver(() => handleResize());
                observer.observe(iframe.contentDocument.body);
            }
        } catch (e) { }

        return () => {
            if (observer) observer.disconnect();
            if (iframe.contentWindow) {
                iframe.contentWindow.removeEventListener('resize', handleResize);
            }
        };
    }, [processedContent]);

    return (
        <iframe
            ref={iframeRef}
            srcDoc={processedContent}
            title="Email Content"
            style={{
                width: '100%',
                border: 'none',
                overflow: 'hidden',
                backgroundColor: 'transparent',
                display: 'block',
                height: height
            }}
            sandbox="allow-popups allow-popups-to-escape-sandbox allow-same-origin"
            loading="lazy"
        />
    );
}
