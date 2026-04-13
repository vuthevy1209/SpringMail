import React from 'react';
import SpringIcon from '../../assets/spring-icon.svg';

export default function GlobalLoading() {
    return (
        <div className="fixed inset-0 z-[9999] flex flex-col items-center justify-center bg-canvas-gray">
            {/* Center Container with pulse animation for premium feel */}
            <div className="flex flex-col items-center gap-6 animate-pulse">
                <img src={SpringIcon} alt="Spring Icon" className="w-20 h-20 object-contain" />

                <div className="flex flex-col items-center text-center">
                    <h1 className="text-3xl font-semibold tracking-tight text-charcoal-ink">SpringMail</h1>
                </div>
            </div>
        </div>
    );
}
