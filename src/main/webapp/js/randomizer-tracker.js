/**
 * Extremis Server-Controlled Random Location Verification Client
 *
 * Implements automated background GPS location verification for students during
 * active authenticated PWA sessions.
 *
 * Requirements:
 * 1. Checks location 4-5 times per academic day at unpredictable times.
 * 2. Does NOT require repeated manual student check-ins or button presses.
 * 3. Does NOT create additional check-in records.
 * 4. Silently requests fresh GPS location via browser Geolocation API when due.
 * 5. Handles GPS errors, permission denials, and geofence results gracefully.
 */

(function () {
    const contextPath = window.EXTREMIS_CONTEXT_PATH || '';
    let isVerifying = false;
    let processedCheckIds = new Set();
    let pollInterval = null;

    document.addEventListener('DOMContentLoaded', () => {
        initLocationTracker();
    });

    function initLocationTracker() {
        // Run immediately when page loads
        checkPendingVerification();

        // Periodic polling every 20 seconds
        pollInterval = setInterval(checkPendingVerification, 20000);

        // Resume checks when user focuses or returns to the active PWA tab
        window.addEventListener('focus', () => {
            checkPendingVerification();
        });

        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') {
                checkPendingVerification();
            }
        });
    }

    async function checkPendingVerification() {
        if (isVerifying) return;

        try {
            const resp = await fetch(contextPath + '/api/random-checkin/poll', {
                headers: { 'Accept': 'application/json' },
                cache: 'no-store'
            });

            if (!resp.ok) return;
            const data = await resp.json();

            if (!data.authenticated || !data.enabled) {
                return;
            }

            if (data.checkDue && data.checkId) {
                if (processedCheckIds.has(data.checkId)) {
                    return; // Already submitted
                }
                executeLocationVerification(data.checkId);
            }
        } catch (e) {
            // Silently ignore transient network disconnects
        }
    }

    function executeLocationVerification(checkId) {
        if (!navigator.geolocation) {
            submitVerificationResult(checkId, null, null, null, 'GPS_UNAVAILABLE');
            return;
        }

        isVerifying = true;

        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const lat = pos.coords.latitude;
                const lng = pos.coords.longitude;
                const acc = pos.coords.accuracy;
                submitVerificationResult(checkId, lat, lng, acc, null);
            },
            (err) => {
                let errType = 'GPS_UNAVAILABLE';
                if (err.code === 1) {
                    errType = 'PERMISSION_DENIED';
                } else if (err.code === 3) {
                    errType = 'TIMEOUT';
                }
                submitVerificationResult(checkId, null, null, null, errType);
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }
        );
    }

    async function submitVerificationResult(checkId, lat, lng, accuracy, error) {
        processedCheckIds.add(checkId);

        try {
            const payload = {
                checkId: checkId,
                latitude: lat,
                longitude: lng,
                accuracy: accuracy,
                error: error,
                clientTimestamp: new Date().toISOString()
            };

            const resp = await fetch(contextPath + '/api/random-checkin/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (resp.ok) {
                const resultData = await resp.json();
                showVerificationNotification(lat != null, error);
            }
        } catch (e) {
            console.warn('Extremis: Verification submission deferred due to network error', e);
        } finally {
            isVerifying = false;
        }
    }

    function showVerificationNotification(success, error) {
        // Non-intrusive toast informing student that background presence check completed
        let toast = document.getElementById('extremisVerificationToast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'extremisVerificationToast';
            toast.style.cssText = `
                position: fixed;
                bottom: 80px;
                left: 50%;
                transform: translateX(-50%);
                background: #0F172A;
                color: #FFFFFF;
                padding: 10px 18px;
                border-radius: 999px;
                font-size: 0.82rem;
                font-weight: 600;
                display: flex;
                align-items: center;
                gap: 8px;
                box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.35);
                z-index: 99999;
                transition: opacity 0.3s ease;
                pointer-events: none;
            `;
            document.body.appendChild(toast);
        }

        if (success) {
            toast.style.background = '#0F172A';
            toast.style.border = '1.5px solid #22C55E';
            toast.innerHTML = `
                <span style="color: #22C55E; font-size: 1rem;">✓</span>
                <span>Campus Location Verified &bull; Academic Presence Confirmed</span>
            `;
        } else {
            toast.style.background = '#1E293B';
            toast.style.border = '1.5px solid #F59E0B';
            toast.innerHTML = `
                <span style="color: #F59E0B; font-size: 1rem;">ℹ</span>
                <span>Random Verification Logged (${error || 'GPS Inactive'})</span>
            `;
        }

        toast.style.opacity = '1';
        toast.style.display = 'flex';

        setTimeout(() => {
            if (toast) {
                toast.style.opacity = '0';
                setTimeout(() => { if (toast) toast.style.display = 'none'; }, 300);
            }
        }, 4000);
    }
})();
