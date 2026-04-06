# Design System: SpringMail

## 1. Visual Theme & Atmosphere
A restrained, highly legible interface optimized for reading and managing correspondence. The atmosphere is crisp, organized, and focused—like a well-lit modern office. It uses a density level of 6, ensuring enough information is visible at once without feeling cramped.

## 2. Color Palette & Roles
- **Canvas Gray** (#F9FAFB) — Primary background surface for the app
- **Pure Surface** (#FFFFFF) — Card, email item, and container fill
- **Charcoal Ink** (#18181B) — Primary text, Zinc-950 depth
- **Muted Steel** (#71717A) — Secondary text, dates, senders, descriptions
- **Whisper Border** (rgba(226,232,240,0.5)) — Card borders, 1px structural lines dividing emails
- **Emerald Accent** (#10b981) — Single accent for CTAs, unread indicators, active states, focus rings
(Max 1 accent. Saturation < 80%. No purple/neon.)

## 3. Typography Rules
- **Display:** MANROPE — Clean, professional, weight-driven hierarchy.
- **Body:** MANROPE — Relaxed leading, 65ch max-width, neutral secondary color for email content reading.
- **Mono:** JetBrains Mono — For technical metadata or LLM tags.
- **Banned:** Inter, generic system fonts for premium contexts. Serif fonts banned in this dashboard context.

## 4. Component Stylings
* **Buttons:** Flat, no outer glow. Tactile -1px translate on active. Emerald fill for primary actions.
* **Cards & Lists:**ROUND_EIGHT corners. List items use border-bottom dividers instead of individual cards to manage high density.
* **Inputs/AI Chat:** Label above input, or integrated seamlessly. Focus ring in Emerald Accent. No floating labels.
* **Loaders:** Skeletal shimmer matching exact layout dimensions. No circular spinners.
* **Empty States:** Composed, illustrated compositions.

## 5. Layout Principles
Grid-first responsive architecture. Strict single-column collapse below 768px. Max-width containment for the main reading pane so lines don't get too long. No flexbox percentage math. Generous internal padding.

## 6. Motion & Interaction
Spring physics for all interactive elements. Staggered cascade reveals when opening an email thread. Perpetual micro-loops on active AI components.

## 7. Anti-Patterns (Banned)
- No emojis anywhere
- No Inter font
- No generic serif fonts (Times New Roman, Georgia, Garamond)
- No pure black (#000000)
- No neon/outer glow shadows
- No oversaturated accents
- No excessive gradient text on large headers
- No custom mouse cursors
- No overlapping elements — clean spatial separation always
- No 3-column equal card layouts
- No generic names ("John Doe", "Acme", "Nexus")
- No fake round numbers (99.99%, 50%)
- No fabricated data or statistics
- No fake system/metric sections
- No LABEL // YEAR formatting
- No AI copywriting clichés ("Elevate", "Seamless", "Unleash", "Next-Gen")
- No filler UI text: "Scroll to explore", "Swipe down", scroll arrows, bouncing chevrons
- No broken Unsplash links — use picsum.photos or SVG avatars
- No centered Hero sections
