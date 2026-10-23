/**
 * Extremis Random Check-In Client Controller
 * Provides real-time polling, Buzz/Sound alerts, Vibration, and Modal prompts.
 */

(function () {
    let lastAlertedChallengeId = null;
    let countdownInterval = null;
    let pollInterval = null;
    const contextPath = window.EXTREMIS_CONTEXT_PATH || '';

    // Request notification permissions gracefully on first user interaction
    document.addEventListener('DOMContentLoaded', () => {
        if ('Notification' in window && Notification.permission === 'default') {
            // Ask for permission when user clicks anywhere
            const askPerm = () => {
                Notification.requestPermission();
                document.removeEventListener('click', askPerm);
            };
            document.addEventListener('click', askPerm);
        }

        startPolling();
    });

    function startPolling() {
        // Poll immediately then every 4 seconds
        checkActiveChallenge();
        pollInterval = setInterval(checkActiveChallenge, 4000);
    }

    async function checkActiveChallenge() {
        try {
            const resp = await fetch(contextPath + '/api/random-checkin/active', {
                headers: { 'Accept': 'application/json' },
                cache: 'no-store'
            });

            if (!resp.ok) return;
            const data = await resp.json();

            if (!data.authenticated) {
                // User logged out or session ended
                return;
            }

            if (data.hasActiveChallenge) {
                // If on the /random-checkin page, don't show the duplicate modal
                if (window.location.pathname.endsWith('/random-checkin')) {
                    return;
                }

                showChallengeAlert(data);
            } else {
                hideChallengeAlert();
            }
        } catch (e) {
            // Silently ignore transient network glitches during polling
        }
    }

    function showChallengeAlert(data) {
        let modal = document.getElementById('extremisRandomCheckinModal');

        // Check if this is a newly arrived challenge to play buzz and vibrate
        if (lastAlertedChallengeId !== data.challengeId) {
            lastAlertedChallengeId = data.challengeId;
            playBuzzAlarm();
            triggerVibration();
            showSystemNotification();
        }

        if (!modal) {
            modal = createAlertModal();
            document.body.appendChild(modal);
        }

        modal.style.display = 'flex';

        // Update countdown
        let remainingSeconds = data.remainingSeconds;
        const timerEl = document.getElementById('randomModalCountdown');

        if (countdownInterval) clearInterval(countdownInterval);

        function renderTimer() {
            if (remainingSeconds <= 0) {
                if (timerEl) {
                    timerEl.innerText = "00:00 (EXPIRED)";
                    timerEl.style.color = "#EF4444";
                }
                const actionBtn = document.getElementById('randomModalActionBtn');
                if (actionBtn) {
                    actionBtn.innerText = "Deadline Passed";
                    actionBtn.disabled = true;
                    actionBtn.style.background = "#94A3B8";
                }
                clearInterval(countdownInterval);
                return;
            }

            const mins = Math.floor(remainingSeconds / 60);
            const secs = remainingSeconds % 60;
            if (timerEl) {
                timerEl.innerText = String(mins).padStart(2, '0') + ":" + String(secs).padStart(2, '0');
            }
            remainingSeconds--;
        }

        renderTimer();
        countdownInterval = setInterval(renderTimer, 1000);
    }

    function hideChallengeAlert() {
        const modal = document.getElementById('extremisRandomCheckinModal');
        if (modal) {
            modal.style.display = 'none';
        }
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
    }

    function createAlertModal() {
        const modal = document.createElement('div');
        modal.id = 'extremisRandomCheckinModal';
        modal.style.cssText = `
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.85);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            z-index: 999999;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 1rem;
            animation: fadeIn 0.3s ease-out;
        `;

        modal.innerHTML = `
            <div style="
                background: #FFFFFF;
                border-radius: 20px;
                max-width: 380px;
                width: 100%;
                padding: 1.5rem;
                text-align: center;
                box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.45);
                border: 2px solid #EF4444;
                position: relative;
            ">
                <div style="
                    width: 54px; height: 54px;
                    background: #FEE2E2;
                    border-radius: 50%;
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    margin-bottom: 0.85rem;
                    color: #EF4444;
                    box-shadow: 0 0 0 8px rgba(239, 68, 68, 0.15);
                ">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                        <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
                        <path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path>
                    </svg>
                </div>

                <div style="
                    display: inline-block;
                    font-size: 0.75rem;
                    font-weight: 800;
                    color: #EF4444;
                    background: #FEF2F2;
                    padding: 3px 10px;
                    border-radius: 999px;
                    letter-spacing: 0.5px;
                    margin-bottom: 0.5rem;
                ">
                    ATTENDANCE VERIFICATION
                </div>

                <h3 style="font-size: 1.15rem; font-weight: 800; color: #0F172A; margin: 0 0 0.5rem 0;">
                    Random Attendance Check-In Required
                </h3>

                <p style="font-size: 0.8rem; color: #64748B; margin: 0 0 1rem 0; line-height: 1.45;">
                    Please verify your classroom attendance now. Your device location and selfie photo must be submitted before deadline.
                </p>

                <div style="
                    background: #0F172A;
                    border-radius: 12px;
                    padding: 0.75rem;
                    margin-bottom: 1.25rem;
                    color: #F8FAFC;
                ">
                    <div style="font-size: 0.7rem; color: #94A3B8; text-transform: uppercase; font-weight: 700; letter-spacing: 0.5px;">
                        Remaining Response Time
                    </div>
                    <div id="randomModalCountdown" style="
                        font-size: 2rem;
                        font-weight: 900;
                        font-family: ui-monospace, SFMono-Regular, monospace;
                        color: #FEF08A;
                        margin: 2px 0;
                    ">
                        --:--
                    </div>
                    <div style="font-size: 0.7rem; color: #CBD5E1;">
                        Failure to respond marks today as Absent
                    </div>
                </div>

                <a href="${contextPath}/random-checkin" id="randomModalActionBtn" style="
                    display: block;
                    width: 100%;
                    background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
                    color: #FFFFFF;
                    font-weight: 700;
                    font-size: 0.95rem;
                    padding: 0.85rem;
                    border-radius: 12px;
                    text-decoration: none;
                    box-shadow: 0 4px 12px rgba(220, 38, 38, 0.35);
                    transition: transform 0.15s ease;
                ">
                    Verify Attendance Now &rarr;
                </a>
            </div>
        `;
        return modal;
    }

    function playBuzzAlarm() {
        try {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (!AudioContext) return;
            const ctx = new AudioContext();

            function beep(freq, delay, dur) {
                const osc = ctx.createOscillator();
                const gain = ctx.createGain();
                osc.type = 'sawtooth';
                osc.frequency.setValueAtTime(freq, ctx.currentTime + delay);
                gain.gain.setValueAtTime(0.3, ctx.currentTime + delay);
                gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + delay + dur);
                osc.connect(gain);
                gain.connect(ctx.destination);
                osc.start(ctx.currentTime + delay);
                osc.stop(ctx.currentTime + delay + dur);
            }

            // Distinct double attention buzz
            beep(880, 0, 0.22);
            beep(587, 0.25, 0.22);
            beep(880, 0.50, 0.22);
            beep(587, 0.75, 0.22);
        } catch (e) {
            console.warn("Audio buzz alert failed:", e);
        }
    }

    function triggerVibration() {
        if ('vibrate' in navigator) {
            try {
                navigator.vibrate([300, 150, 300, 150, 500]);
            } catch (e) {}
        }
    }

    function showSystemNotification() {
        if ('Notification' in window && Notification.permission === 'granted') {
            try {
                const notif = new Notification('Random Attendance Check-In Required', {
                    body: 'Please verify your attendance now. Response deadline approaching!',
                    icon: contextPath + '/icons/icon-192.svg',
                    badge: contextPath + '/icons/icon-192.svg',
                    vibrate: [300, 150, 300, 150, 500],
                    tag: 'random-checkin',
                    requireInteraction: true
                });
                notif.onclick = function () {
                    window.focus();
                    window.location.href = contextPath + '/random-checkin';
                    this.close();
                };
            } catch (e) {
                if (navigator.serviceWorker && navigator.serviceWorker.ready) {
                    navigator.serviceWorker.ready.then(reg => {
                        reg.showNotification('Random Attendance Check-In Required', {
                            body: 'Please verify your attendance now. Tap to verify presence.',
                            icon: contextPath + '/icons/icon-192.svg',
                            badge: contextPath + '/icons/icon-192.svg',
                            vibrate: [300, 150, 300, 150, 500],
                            tag: 'random-checkin',
                            data: { url: contextPath + '/random-checkin' }
                        });
                    });
                }
            }
        }
    }
})();
