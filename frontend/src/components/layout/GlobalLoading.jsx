import React from 'react';
import { Mail } from 'lucide-react';

export default function GlobalLoading() {
	return (
		<div className="fixed inset-0 z-[9999] flex flex-col items-center justify-center bg-canvas-gray">
			{/* Center Container with pulse animation for premium feel */}
			<div className="flex flex-col items-center gap-6 animate-pulse">
				<div className="w-20 h-20 bg-emerald-accent rounded-2xl flex items-center justify-center text-white shadow-[0_8px_30px_rgb(16,185,129,0.2)]">
					<Mail size={40} strokeWidth={2.5} />
				</div>
				<div className="flex flex-col items-center text-center">
					<h1 className="text-3xl font-semibold tracking-tight text-charcoal-ink">SpringMail</h1>
				</div>
			</div>
		</div>
	);
}
