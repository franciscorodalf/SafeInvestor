/*
 * SafeInvestor — Service Worker mínimo.
 *
 * Estrategias:
 *   - Activos estáticos (CDN, /images/*, /css/*, /js/*): cache-first.
 *   - Documentos HTML y /api/*: network-first (siempre fresh, fallback offline).
 *
 * No cacheamos respuestas dinámicas de Thymeleaf para que el contenido
 * (saludo del dashboard, último movimiento, etc.) nunca se quede stale.
 * La PWA es "instalable" pero sigue dependiendo de red para la mayoría de
 * pantallas — la idea es la experiencia de "app", no el offline-first total.
 */

const CACHE = 'safeinvestor-v1';
const APP_SHELL = [
    '/',
    '/manifest.webmanifest',
    '/images/logo.png'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE).then(c => c.addAll(APP_SHELL)).then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then(keys => Promise.all(
            keys.filter(k => k !== CACHE).map(k => caches.delete(k))
        )).then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', (event) => {
    const req = event.request;
    if (req.method !== 'GET') return;

    const url = new URL(req.url);

    // Network-first para HTML y /api/* (contenido dinámico)
    const isDoc = req.mode === 'navigate' || (req.headers.get('accept') || '').includes('text/html');
    const isApi = url.pathname.startsWith('/api/');
    if (isDoc || isApi) {
        event.respondWith(
            fetch(req)
                .then(res => {
                    // Cachea solo el doc raíz para tener algo si el usuario está offline
                    if (req.url === self.location.origin + '/') {
                        const copy = res.clone();
                        caches.open(CACHE).then(c => c.put('/', copy));
                    }
                    return res;
                })
                .catch(() => caches.match(req).then(r => r || caches.match('/')))
        );
        return;
    }

    // Cache-first para estáticos (imágenes, css, js, fuentes, etc.)
    event.respondWith(
        caches.match(req).then(cached => {
            if (cached) return cached;
            return fetch(req).then(res => {
                // Cachea respuestas exitosas same-origin para futuras visitas
                if (res.ok && url.origin === self.location.origin) {
                    const copy = res.clone();
                    caches.open(CACHE).then(c => c.put(req, copy));
                }
                return res;
            });
        })
    );
});
