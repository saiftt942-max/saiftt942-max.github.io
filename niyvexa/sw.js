const CACHE = "niyvexa-demo-v4";
const CORE = ["./", "./index.html", "./app.html", "./manifest.webmanifest", "./icon.svg", "./icon-192.svg", "./icon-512.svg", "./auth-config.js", "./privacy.html", "./terms.html"];
self.addEventListener("install", event => event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(CORE)).then(() => self.skipWaiting())));
self.addEventListener("activate", event => event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(k => k.startsWith("niyvexa-demo-") && k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim())));
self.addEventListener("fetch", event => {
  const req = event.request, url = new URL(req.url);
  if (req.method !== "GET" || url.origin !== self.location.origin) return;
  event.respondWith(fetch(req).then(res => {
    if (res.ok && res.type === "basic") caches.open(CACHE).then(cache => cache.put(req, res.clone()));
    return res;
  }).catch(async () => {
    const cached = await caches.match(req);
    if (cached) return cached;
    if (req.mode === "navigate") return caches.match("./index.html");
    return new Response("Offline", {status:503});
  }));
});