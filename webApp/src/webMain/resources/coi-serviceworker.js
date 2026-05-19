(() => {
    const reloadedBySelf = window.sessionStorage.getItem("coiReloadedBySelf");
    window.sessionStorage.removeItem("coiReloadedBySelf");
    const coepDegrading = (reloadedBySelf == "coepdegrade");

    // You can customize the behavior of this script through a global `coi` variable.
    const coi = {
        shouldRegister: () => !reloadedBySelf,
        shouldDeregister: () => false,
        coepCredentialless: () => true,
        coepDegrade: () => true,
        doReload: () => window.location.reload(),
        quiet: false,
        ...window.coi
    };

    const n = navigator;
    const controlling = n.serviceWorker && n.serviceWorker.controller;

    // Record the failure if the page is served by serviceWorker.
    if (controlling && !window.crossOriginIsolated) {
        window.sessionStorage.setItem("coiCoepHasFailed", "true");
    }
    const coepHasFailed = window.sessionStorage.getItem("coiCoepHasFailed");

    if (controlling) {
        // Reload only on the first failure.
        const reloadToDegrade = coi.coepDegrade() && !(
            coepDegrading || window.crossOriginIsolated
        );
        n.serviceWorker.controller.postMessage({
            type: "coepCredentialless",
            value: (reloadToDegrade || coepHasFailed && coi.coepDegrade())
                ? false
                : coi.coepCredentialless(),
        });
        if (reloadToDegrade) {
            !coi.quiet && console.log("Reloading page to degrade COEP.");
            window.sessionStorage.setItem("coiReloadedBySelf", "coepdegrade");
            coi.doReload("coepdegrade");
        }

        if (coi.shouldDeregister()) {
            n.serviceWorker.controller.postMessage({ type: "deregister" });
        }
    }

    // If we're already coi: do nothing. Perhaps it's due to this script doing its job, or COOP/COEP are
    // already set from the origin server. Also if the browser has no notion of crossOriginIsolated, just give up here.
    if (window.crossOriginIsolated !== false || !coi.shouldRegister()) return;

    if (!window.isSecureContext) {
        !coi.quiet && console.log("COOP/COEP Service Worker not registered, a secure context is required.");
        return;
    }

    // In some environments (e.g. Firefox private mode) this won't be available
    if (!n.serviceWorker) {
        !coi.quiet && console.error("COOP/COEP Service Worker not registered, perhaps due to private mode.");
        return;
    }

    n.serviceWorker.register('./service-worker.js').then(
        (registration) => {
            !coi.quiet && console.log("COOP/COEP Service Worker registered", registration.scope);

            registration.addEventListener("updatefound", () => {
                !coi.quiet && console.log("Reloading page to make use of updated COOP/COEP Service Worker.");
                window.sessionStorage.setItem("coiReloadedBySelf", "updatefound");
                coi.doReload();
            });

            // If the registration is active, but it's not controlling the page
            if (registration.active && !n.serviceWorker.controller) {
                !coi.quiet && console.log("Reloading page to make use of COOP/COEP Service Worker.");
                window.sessionStorage.setItem("coiReloadedBySelf", "notcontrolling");
                coi.doReload();
            }
        },
        (err) => {
            !coi.quiet && console.error("COOP/COEP Service Worker failed to register:", err);
        }
    );
})();
