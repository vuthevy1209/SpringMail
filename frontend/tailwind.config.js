/** @type {import('tailwindcss').Config} */
export default {
	content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
	theme: {
		extend: {
			colors: {
				'canvas-gray':   '#F9FAFB',
				'pure-surface':  '#FFFFFF',
				'charcoal-ink':  '#18181B',
				'muted-steel':   '#71717A',
				'whisper':       '#E2E8F0', // use as border-whisper/50
				'emerald-accent': '#10B981',
				'emerald-hover':  '#0ca16d',
			},
			fontFamily: {
				sans: ['Manrope', 'sans-serif'],
				mono: ['"JetBrains Mono"', 'monospace'],
			},
		},
	},
	plugins: [],
};
