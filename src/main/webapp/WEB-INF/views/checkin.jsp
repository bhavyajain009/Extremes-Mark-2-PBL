<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Extremis - ${pageTitle}</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
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
        <div style="width: 40px;"></div> <!-- placeholder to balance flex header -->
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
                        <h3 class="font-bold text-dark text-base">Location Captured</h3>
                        <p class="text-xs text-muted" id="locStatusText">Detecting current GPS coordinates...</p>
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

        <!-- Photo Captured Card — hidden for Check-Out, only Check-In requires photo -->
        <c:choose>
            <c:when test="${mode == 'CHECK_OUT'}">
                <!-- Check-Out: no photo needed, card is hidden -->
            </c:when>
            <c:otherwise>
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
                                <h3 class="font-bold text-dark text-base">Photo Verification</h3>
                                <p class="text-xs text-muted" id="photoStatusText">Face verification required</p>
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
            </c:otherwise>
        </c:choose>

        <!-- Form Submission -->
        <form method="post" action="${pageContext.request.contextPath}/${mode == 'CHECK_IN' ? 'checkin' : 'checkout'}" id="checkForm">
            <input type="hidden" name="latitude" id="inputLat">
            <input type="hidden" name="longitude" id="inputLng">
            <input type="hidden" name="accuracy" id="inputAcc">
            <c:if test="${mode != 'CHECK_OUT'}">
                <input type="hidden" name="photoBase64" id="inputPhoto">
            </c:if>

            <button type="submit" class="btn btn-action-large w-full ${mode == 'CHECK_OUT' ? 'btn-danger' : 'btn-primary'}" id="btnSubmitCheck" disabled>
                ${submitButtonText}
            </button>
            <p class="text-center text-xs text-muted mt-8">
                <c:choose>
                    <c:when test="${mode == 'CHECK_OUT'}">
                        Button activates once your GPS location is verified.
                    </c:when>
                    <c:otherwise>
                        Button activates once both location and photo verification are captured.
                    </c:otherwise>
                </c:choose>
            </p>
        </form>

    </div>

    <!-- Bottom Navigation -->
    <jsp:include page="/WEB-INF/views/includes/nav.jsp" />

</div>

<!-- Camera & Geolocation Script -->
<script src="${pageContext.request.contextPath}/js/app_new.js"></script>
<script>
    // Pass server-side mode to JS so checkout can skip photo requirement
    var PAGE_MODE = '${mode}';
    document.addEventListener('DOMContentLoaded', () => {
        if (PAGE_MODE === 'CHECK_OUT') {
            initCheckOutPage();
        } else {
            initCheckInPage();
        }
    });
</script>
</body>
</html>
