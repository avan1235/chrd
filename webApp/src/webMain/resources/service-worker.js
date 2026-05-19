if (typeof window !== "undefined") {
    if (window.crossOriginIsolated || !navigator.serviceWorker) {
        if (window.crossOriginIsolated && navigator.serviceWorker.controller) {
            navigator.serviceWorker.controller.postMessage({type: "coepCredentialless", value: true});
        }
    } else {
        const reloadedBySelf = window.sessionStorage.getItem("coiReloadedBySelf");
        window.sessionStorage.removeItem("coiReloadedBySelf");
        if (!reloadedBySelf && window.isSecureContext) {
            navigator.serviceWorker.register(window.document.currentScript.src).then(reg => {
                const reload = () => {
                    window.sessionStorage.setItem("coiReloadedBySelf", "true");
                    window.location.reload();
                };
                if (reg.active && !navigator.serviceWorker.controller) reload();
                reg.addEventListener("updatefound", reload);
            });
        }
    }
} else {
    const CACHE_VERSION = '{{OVERRIDE THIS IN DEPLOYMENT}}';
    const CACHE_NAME = `chrd-app-cache-${CACHE_VERSION}`;
    const CACHED_EXTENSIONS = ['.wasm', '.png', '.ttf', '.cvr', '.js', '.css'];

    let coepCredentialless = false;

    self.addEventListener("message", (ev) => {
        if (!ev.data) {
            return;
        } else if (ev.data.type === "deregister") {
            self.registration
                .unregister()
                .then(() => {
                    return self.clients.matchAll();
                })
                .then(clients => {
                    clients.forEach((client) => client.navigate(client.url));
                });
        } else if (ev.data.type === "coepCredentialless") {
            coepCredentialless = ev.data.value;
        }
    });

    self.addEventListener('install', event => {
        self.skipWaiting();
    });

    self.addEventListener('activate', event => {
        event.waitUntil(
            caches.keys().then(cacheNames => {
                return Promise.all(
                    cacheNames.map(cacheName => {
                        if (cacheName.startsWith('chrd-app-cache-') && cacheName !== CACHE_NAME) {
                            return caches.delete(cacheName);
                        }
                    })
                );
            })
        );
        self.clients.claim();
    });

    self.addEventListener('fetch', event => {
        const r = event.request;
        if (r.cache === "only-if-cached" && r.mode !== "same-origin") {
            return;
        }

        const url = new URL(r.url);
        const shouldCache = r.method === 'GET' && CACHED_EXTENSIONS.some(ext => url.pathname.endsWith(ext));

        const request = (coepCredentialless && r.mode === "no-cors")
            ? new Request(r, {credentials: "omit"})
            : r;

        event.respondWith(
            (shouldCache ? caches.match(r) : Promise.resolve(null)).then(cachedResponse => {
                if (cachedResponse) {
                    return cachedResponse;
                }

                return fetch(request).then(response => {
                    if (shouldCache && response && response.status === 200 && response.type === 'basic') {
                        const responseToCache = response.clone();
                        caches.open(CACHE_NAME).then(cache => {
                            cache.put(r, responseToCache);
                        });
                    }
                    return response;
                });
            }).then(response => {
                if (!response || response.status === 0) {
                    return response;
                }

                const newHeaders = new Headers(response.headers);
                newHeaders.set("Cross-Origin-Embedder-Policy",
                    coepCredentialless ? "credentialless" : "require-corp"
                );
                if (!coepCredentialless) {
                    newHeaders.set("Cross-Origin-Resource-Policy", "cross-origin");
                }
                newHeaders.set("Cross-Origin-Opener-Policy", "same-origin");

                return new Response(response.body, {
                    status: response.status,
                    statusText: response.statusText,
                    headers: newHeaders,
                });
            }).catch(e => {
                console.error(e);
                return fetch(event.request);
            })
        );
    });
}
