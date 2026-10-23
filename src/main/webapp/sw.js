// Extremis Attendance Service Worker for PWA Installation and Notifications
const CACHE_NAME = 'extremis-v2';
const ASSETS_TO_CACHE = [
  '/',
  '/css/style.css',
  '/js/app.js',
  '/js/randomizer-client.js',
  '/manifest.json',
  '/icons/icon-192.svg'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(ASSETS_TO_CACHE).catch(() => {});
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);
  // Cache static assets only; dynamic pages and API calls always go network-first
  if (url.pathname.startsWith('/css/') || url.pathname.startsWith('/js/') || url.pathname.startsWith('/icons/')) {
    event.respondWith(
      caches.match(event.request).then((cached) => {
        return cached || fetch(event.request);
      })
    );
  } else {
    event.respondWith(fetch(event.request).catch(() => caches.match(event.request)));
  }
});

// Web Push Notification Handling
self.addEventListener('push', (event) => {
  let title = 'Random Attendance Check-In Required';
  let body = 'Please verify your attendance now. Deadline approaching!';
  let targetUrl = '/random-checkin';

  if (event.data) {
    try {
      const json = event.data.json();
      if (json.title) title = json.title;
      if (json.body) body = json.body;
      if (json.url) targetUrl = json.url;
    } catch (e) {
      body = event.data.text();
    }
  }

  const options = {
    body: body,
    icon: '/icons/icon-192.svg',
    badge: '/icons/icon-192.svg',
    vibrate: [300, 150, 300, 150, 500],
    tag: 'random-checkin',
    renotify: true,
    requireInteraction: true,
    data: { url: targetUrl }
  };

  event.waitUntil(self.registration.showNotification(title, options));
});

// Tapping Notification opens / focuses the app and navigates to the active challenge
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const targetUrl = (event.notification.data && event.notification.data.url) ? event.notification.data.url : '/random-checkin';

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      for (let i = 0; i < clientList.length; i++) {
        let client = clientList[i];
        if ('focus' in client) {
          if (client.url.includes('/random-checkin')) {
            return client.focus();
          } else {
            client.navigate(targetUrl);
            return client.focus();
          }
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(targetUrl);
      }
    })
  );
});
