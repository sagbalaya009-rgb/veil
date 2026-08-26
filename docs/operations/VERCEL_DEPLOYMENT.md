# Vercel deployment

Project Veil is a static Vite web app. The production build command is `pnpm build`, and Vercel should serve `dist/public`.

The repository root includes `vercel.json` with those settings and a client-side routing fallback. The website intentionally does not request browser permissions, does not call external APIs, and does not claim to monitor activity outside the browser.

## Deployment settings

| Setting | Value |
| --- | --- |
| Framework preset | Vite |
| Install command | `pnpm install --frozen-lockfile` |
| Build command | `pnpm build` |
| Output directory | `dist/public` |
| Node.js | 22.x or later |

## Deployment path

Connect the intended Git repository in Vercel, confirm the settings above, and then deploy from the Vercel project dashboard. If a custom domain is added later, configure it in the same Vercel project after the first successful deployment.

> The browser MVP is limited to commitment planning and reflection. It does not block content outside this site, inspect other tabs, capture screens, or communicate with a server.
