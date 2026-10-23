<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Extremis - Random Attendance Check-In</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="manifest" href="${pageContext.request.contextPath}/manifest.json">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        .countdown-container {
            background: linear-gradient(135deg, #1E1B4B 0%, #312E81 100%);
            border-radius: 16px;
            color: #FFFFFF;
            padding: 18px 16px;
            text-align: center;
            box-shadow: 0 4px 12px rgba(49, 46, 129, 0.25);
            margin-bottom: 16px;
            position: relative;
            overflow: hidden;
        }
        .countdown-container::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0; height: 4px;
            background: linear-gradient(90deg, #EF4444, #F59E0B, #10B981);
        }
        .countdown-timer-val {
            font-size: 2.4rem;
            font-weight: 900;
            font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
            letter-spacing: 2px;
            color: #FEF08A;
            text-shadow: 0 2px 8px rgba(0,0,0,0.4);
            margin: 6px 0;
        }
        .urgent-pulse-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: rgba(239, 68, 68, 0.25);
            border: 1px solid rgba(239, 68, 68, 0.5);
            color: #FCA5A5;
            padding: 4px 10px;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .pulse-dot {
            width: 8px;
            height: 8px;
            background-color: #EF4444;
            border-radius: 50%;
            animation: pulse-ring 1.5s cubic-bezier(0.215, 0.61, 0.355, 1) infinite;
        }
        @keyframes pulse-ring {
            0% { transform: scale(0.9); opacity: 1; }
            50% { transform: scale(1.4); opacity: 0.6; }
            100% { transform: scale(0.9); opacity: 1; }
        }
    </style>
</head>
<body>
<div class="mobile-container">

    <!-- Top Header -->
    <header class="page-header">
        <a href="${pageContext.request.contextPath}/dashboard" class="btn-icon" aria-label="Back">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
        </a>
        <h1 class="page-title">${pageTitle}</h1>
        <div style="width: 40px;"></div>
    </header>

    <div class="content-scrollable">

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger mb-16">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                <span>${errorMessage}</span>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${hasActiveChallenge}">
                <!-- Active Challenge Card with Live Countdown -->
                <div class="countdown-container">
                    <div class="urgent-pulse-badge">
                        <span class="pulse-dot"></span>
                        ATTENDANCE RANDOMIZER ACTIVE
                    </div>
                    <div style="font-size: 0.85rem; color: #E0E7FF; margin-top: 6px;">
                        Please verify your classroom presence immediately:
                    </div>
                    <div class="countdown-timer-val" id="countdownClock">--:--</div>
                    <div style="font-size: 0.72rem; color: #CBD5E1;">
                        Deadline: <strong>${challenge.formattedDeadlineTime}</strong> &bull; Challenge #${challenge.challengeCode}
                    </div>
                </div>

                <!-- Location Captured Card -->
                <div class="card check-step-card mb-16" id="locationCard">
                    <div class="flex-between mb-8">
                        <div class="flex items-center gap-8">
                            <div class="step-icon-circle bg-blue-light text-blue">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                    <circle cx="12" cy="10" r="3"/>
                                </svg>
                            </div>
                            <div>
                                <h3 class="font-bold text-dark text-base">Campus GPS Geofence</h3>
                                <p class="text-xs text-muted" id="locStatusText">Validating classroom GPS coordinates...</p>
                            </div>
                        </div>
                        <div id="locBadge" class="status-badge badge-pending">
                            <span id="locBadgeText">Acquiring...</span>
                        </div>
                    </div>

                    <div class="location-details-box mt-12" id="locCoordsBox" style="display: none;">
                        <div class="loc-data-row">
                            <span class="text-xs text-muted">Latitude:</span>
                            <span class="text-xs font-semibold text-dark font-mono" id="dispLat">--</span>
                        </div>
                        <div class="loc-data-row">
                            <span class="text-xs text-muted">Longitude:</span>
                            <span class="text-xs font-semibold text-dark font-mono" id="dispLng">--</span>
                        </div>
                        <div class="loc-data-row">
                            <span class="text-xs text-muted">Accuracy:</span>
                            <span class="text-xs font-semibold text-green font-mono" id="dispAcc">--</span>
                        </div>
                    </div>

                    <button type="button" class="btn btn-secondary btn-sm mt-12 w-full" id="btnRetryLoc" style="display: none;">
                        Retry Location Access
                    </button>
                </div>

                <!-- Photo Captured Card -->
                <div class="card check-step-card mb-20" id="photoCard">
                    <div class="flex-between mb-12">
                        <div class="flex items-center gap-8">
                            <div class="step-icon-circle bg-blue-light text-blue">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                                    <circle cx="12" cy="13" r="4"/>
                                </svg>
                            </div>
                            <div>
                                <h3 class="font-bold text-dark text-base">Live Selfie Camera</h3>
                                <p class="text-xs text-muted" id="photoStatusText">Live face verification required</p>
                            </div>
                        </div>
                        <div id="photoBadge" class="status-badge badge-pending">
                            <span id="photoBadgeText">Pending</span>
                        </div>
                    </div>

                    <!-- Camera View Container -->
                    <div class="camera-container" id="cameraBox">
                        <video id="cameraVideo" autoplay playsinline muted></video>
                        <canvas id="photoCanvas" style="display: none;"></canvas>
                        <img id="photoPreview" alt="Captured Preview" style="display: none;" />
                        <div id="cameraPlaceholder" class="camera-placeholder">
                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#94A3B8" stroke-width="2">
                                <circle cx="12" cy="12" r="10"/>
                                <path d="M8 12h8M12 8v8"/>
                            </svg>
                            <span class="text-xs text-muted mt-8">Initializing Camera...</span>
                        </div>
                    </div>

                    <!-- Action buttons for Photo -->
                    <div class="photo-controls mt-12 flex gap-8">
                        <button type="button" class="btn btn-primary flex-1" id="btnCapturePhoto">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="9"/>
                                <circle cx="12" cy="12" r="3"/>
                            </svg>
                            <span>Capture Photo</span>
                        </button>
                        <button type="button" class="btn btn-secondary flex-1" id="btnRetakePhoto" style="display: none;">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M1 4v6h6M23 20v-6h-6"/>
                                <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"/>
                            </svg>
                            <span>Retake</span>
                        </button>
                    </div>
                </div>

                <!-- Form Submission -->
                <form method="post" action="${pageContext.request.contextPath}/random-checkin" id="checkForm">
                    <input type="hidden" name="challengeId" value="${challenge.id}">
                    <input type="hidden" name="latitude" id="inputLat">
                    <input type="hidden" name="longitude" id="inputLng">
                    <input type="hidden" name="accuracy" id="inputAcc">
                    <input type="hidden" name="photoBase64" id="inputPhoto">

                    <button type="submit" class="btn btn-action-large w-full btn-primary" id="btnSubmitCheck" disabled style="background: linear-gradient(135deg, #16A34A 0%, #15803D 100%);">
                        Verify Attendance Now
                    </button>
                    <p class="text-center text-xs text-muted mt-8">
                        Verifies GPS inside campus and captures selfie for server timestamping.
                    </p>
                </form>
            </c:when>

            <c:otherwise>
                <!-- No Active Challenge / Completed State -->
                <div class="card text-center p-24" style="margin-top: 2rem;">
                    <div class="step-icon-circle mx-auto mb-16" style="width: 56px; height: 56px; font-size: 1.8rem; background: #ECFDF5; color: #059669;">
                        ✓
                    </div>
                    <h3 class="font-bold text-dark text-lg mb-8">No Active Random Check-In</h3>
                    <p class="text-xs text-muted mb-20" style="line-height: 1.5;">
                        <c:choose>
                            <c:when test="${pastChallengeStatus == 'RESPONDED'}">
                                Your response for this random attendance challenge has been recorded successfully!
                            </c:when>
                            <c:when test="${pastChallengeStatus == 'MISSED'}">
                                The deadline for this random attendance check-in has expired.
                            </c:when>
                            <c:otherwise>
                                There is currently no active random attendance check-in pending for your account. You will receive an alert as soon as the next challenge becomes active.
                            </c:otherwise>
                        </c:choose>
                    </p>
                    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary w-full py-12">
                        Back to Student Dashboard
                    </a>
                </div>
            </c:otherwise>
        </c:choose>

    </div>

    <!-- Bottom Navigation -->
    <jsp:include page="/WEB-INF/views/includes/nav.jsp" />

</div>

<!-- Camera & Geolocation Script -->
<script src="${pageContext.request.contextPath}/js/app.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        <c:if test="${hasActiveChallenge}">
            initCheckInPage();

            // Countdown timer logic
            let remainingSeconds = ${remainingSeconds};
            const clockEl = document.getElementById('countdownClock');
            const submitBtn = document.getElementById('btnSubmitCheck');

            function updateClock() {
                if (remainingSeconds <= 0) {
                    clockEl.innerText = "00:00 (EXPIRED)";
                    clockEl.style.color = "#EF4444";
                    if (submitBtn) {
                        submitBtn.disabled = true;
                        submitBtn.innerText = "Challenge Expired";
                    }
                    return;
                }

                const mins = Math.floor(remainingSeconds / 60);
                const secs = remainingSeconds % 60;
                clockEl.innerText = String(mins).padStart(2, '0') + ":" + String(secs).padStart(2, '0');
                remainingSeconds--;
                setTimeout(updateClock, 1000);
            }
            updateClock();
        </c:if>
    });
</script>
</body>
</html>
