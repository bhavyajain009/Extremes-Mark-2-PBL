<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Random Location Verification Control - Extremis Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-portal.css">
</head>
<body class="admin-body">

    <div class="admin-layout">
        <!-- Left Dark Navy Sidebar Navigation -->
        <jsp:include page="/WEB-INF/views/includes/admin-sidebar.jsp">
            <jsp:param name="activeSection" value="randomizer" />
        </jsp:include>

        <!-- Main Content Area -->
        <div class="admin-main-wrapper">
            <!-- Top White Header Bar -->
            <jsp:include page="/WEB-INF/views/includes/admin-header.jsp" />

            <main class="admin-content-area">

        <!-- Flash messages -->
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert-banner alert-banner-success">
                <span><strong>Success:</strong> ${sessionScope.flashSuccess}</span>
            </div>
            <% session.removeAttribute("flashSuccess"); %>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert-banner" style="background: #FEE2E2; border: 1.5px solid #EF4444; color: #991B1B; padding: 0.85rem 1rem; border-radius: 10px; margin-bottom: 1rem; font-size: 0.9rem;">
                <span><strong>Notice:</strong> ${sessionScope.flashError}</span>
            </div>
            <% session.removeAttribute("flashError"); %>
        </c:if>

        <!-- Header Hero Banner -->
        <div class="card mb-20" style="background: linear-gradient(135deg, #090D16 0%, #111827 50%, #1E1B4B 100%); color: #FFFFFF; padding: 1.75rem 2rem; border: 1px solid #1E293B; border-radius: 14px;">
            <div class="flex-between items-center" style="flex-wrap: wrap; gap: 1rem;">
                <div>
                    <div class="flex items-center gap-8 mb-6">
                        <span style="font-size: 1.5rem;">🛰️</span>
                        <span class="status-pill" style="background: rgba(147, 51, 234, 0.25); color: #C084FC; font-weight: 700; border: 1px solid rgba(192, 132, 252, 0.3);">
                            Random Verification Center
                        </span>
                    </div>
                    <h2 class="font-bold text-2xl text-white" style="color: #FFFFFF; margin: 0 0 6px 0;">Random Location Verification Control</h2>
                    <p class="text-xs text-muted" style="color: #94A3B8; margin: 0; max-width: 650px;">
                        Manage server-controlled unpredictable background GPS geofence verifications (4–5 checks/day, 9:05 AM – 3:40 PM, excluding 12:20 PM – 1:10 PM lunch break).
                    </p>
                </div>
                <div class="flex items-center gap-10">
                    <c:choose>
                        <c:when test="${not randomizerSettings.enabled}">
                            <span class="status-pill" style="background: #475569; color: #F1F5F9; font-weight: 800; font-size: 0.85rem; padding: 8px 16px; border: 1px solid #64748B;">
                                ○ SYSTEM DISABLED (OFF)
                            </span>
                        </c:when>
                        <c:when test="${randomizerSettings.paused}">
                            <span class="status-pill" style="background: #D97706; color: #FFFFFF; font-weight: 800; font-size: 0.85rem; padding: 8px 16px;">
                                ⏸ PAUSED
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-pill" style="background: #16A34A; color: #FFFFFF; font-weight: 800; font-size: 0.85rem; padding: 8px 16px; box-shadow: 0 0 12px rgba(22, 163, 74, 0.4);">
                                ● ACTIVE &amp; RUNNING
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Master Power & Action Toolbar -->
        <div class="card mb-20" style="background: #0F172A; border: 1px solid #1E293B; border-radius: 12px; padding: 1.25rem 1.5rem;">
            <div class="flex-between items-center" style="flex-wrap: wrap; gap: 1rem;">
                <div class="flex items-center gap-12" style="flex-wrap: wrap;">
                    <!-- Master Toggle Form -->
                    <form method="post" action="${pageContext.request.contextPath}/admin/randomizer" style="margin: 0;">
                        <input type="hidden" name="action" value="toggle_random_verification">
                        <input type="hidden" name="enable" value="${randomizerSettings.enabled ? '0' : '1'}">
                        <button type="submit" class="btn-sm-action" style="background: ${randomizerSettings.enabled ? '#DC2626' : '#16A34A'}; color: #FFFFFF; border: none; padding: 9px 20px; border-radius: 8px; font-weight: 800; font-size: 0.9rem; cursor: pointer; display: inline-flex; align-items: center; gap: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
                            <span>${randomizerSettings.enabled ? '🛑 Turn OFF Verification System' : '🚀 Turn ON Verification System'}</span>
                        </button>
                    </form>

                    <c:if test="${randomizerSettings.enabled}">
                        <form method="post" action="${pageContext.request.contextPath}/admin/randomizer" style="margin: 0;">
                            <input type="hidden" name="action" value="toggle_pause">
                            <button type="submit" class="btn-sm-action" style="background: #1E293B; color: #E2E8F0; border: 1px solid #475569; padding: 9px 16px; border-radius: 8px; font-weight: 700; font-size: 0.85rem; cursor: pointer;">
                                ${randomizerSettings.paused ? '▶ Resume Automated Checks' : '⏸ Pause Automated Checks'}
                            </button>
                        </form>
                    </c:if>
                </div>

                <!-- Academic Gates Pills -->
                <div style="display: flex; flex-wrap: wrap; gap: 8px; font-size: 0.75rem;">
                    <span style="background: #1E293B; color: #E2E8F0; padding: 6px 12px; border-radius: 6px; border: 1px solid #334155;">
                        Check-in Gate: <strong>8:50&ndash;9:05 AM</strong>
                    </span>
                    <span style="background: #1E293B; color: #F59E0B; padding: 6px 12px; border-radius: 6px; border: 1px solid #78350F;">
                        Lunch Exclusion: <strong>12:20&ndash;1:10 PM</strong>
                    </span>
                    <span style="background: #1E293B; color: #E2E8F0; padding: 6px 12px; border-radius: 6px; border: 1px solid #334155;">
                        Checkout Gate: <strong>3:40 PM</strong>
                    </span>
                    <span style="background: #1E293B; color: #38BDF8; padding: 6px 12px; border-radius: 6px; border: 1px solid #0284C7;">
                        Frequency: <strong>4&ndash;5 Checks/Day</strong>
                    </span>
                </div>
            </div>
        </div>

        <!-- Telemetry Metrics Counter Cards -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
            <div class="card p-16" style="margin: 0; background: #FFFFFF; border: 1.5px solid #E2E8F0; border-radius: 10px;">
                <span class="text-xs text-muted font-bold" style="text-transform: uppercase;">Total Telemetry Checks</span>
                <div style="font-size: 2rem; font-weight: 900; color: #0F172A; margin: 4px 0 2px 0;">${totalLocationChecks}</div>
                <span class="text-xs text-muted">Generated for checked-in students</span>
            </div>
            <div class="card p-16" style="margin: 0; background: #F0FDF4; border: 1.5px solid #86EFAC; border-radius: 10px;">
                <span class="text-xs font-bold" style="color: #166534; text-transform: uppercase;">Verified On Campus</span>
                <div style="font-size: 2rem; font-weight: 900; color: #15803D; margin: 4px 0 2px 0;">${verifiedCampusCount}</div>
                <span class="text-xs text-muted">Within 1,000m campus geofence</span>
            </div>
            <div class="card p-16" style="margin: 0; background: #FEF2F2; border: 1.5px solid #FCA5A5; border-radius: 10px;">
                <span class="text-xs font-bold" style="color: #991B1B; text-transform: uppercase;">Outside Geofence</span>
                <div style="font-size: 2rem; font-weight: 900; color: #DC2626; margin: 4px 0 2px 0;">${outsideGeofenceCount}</div>
                <span class="text-xs text-muted">Flagged for administrative review</span>
            </div>
            <div class="card p-16" style="margin: 0; background: #FFFBEB; border: 1.5px solid #FDE68A; border-radius: 10px;">
                <span class="text-xs font-bold" style="color: #92400E; text-transform: uppercase;">GPS Unavailable / Poor Acc</span>
                <div style="font-size: 2rem; font-weight: 900; color: #D97706; margin: 4px 0 2px 0;">${gpsFailuresCount}</div>
                <span class="text-xs text-muted">Hardware/permission issue (Rule 11)</span>
            </div>
            <div class="card p-16" style="margin: 0; background: #F0F9FF; border: 1.5px solid #BAE6FD; border-radius: 10px;">
                <span class="text-xs font-bold" style="color: #0369A1; text-transform: uppercase;">Pending / Scheduled</span>
                <div style="font-size: 2rem; font-weight: 900; color: #0284C7; margin: 4px 0 2px 0;">${pendingChecksCount}</div>
                <span class="text-xs text-muted">Awaiting random trigger time</span>
            </div>
        </div>

        <!-- Live Telemetry Verification Log Table -->
        <div class="portal-table-container mb-24" style="border: 1.5px solid #CBD5E1; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06); border-radius: 12px; overflow: hidden;">
            <div class="table-header-bar" style="background: #0F172A; color: #FFFFFF; border-bottom: none; padding: 1.25rem 1.5rem;">
                <div>
                    <h3 class="table-title" style="color: #FFFFFF; font-size: 1.15rem; margin: 0;">Live Student Location Telemetry Records</h3>
                    <p class="table-subtitle" style="color: #94A3B8; margin: 2px 0 0 0;">
                        Real-time GPS coordinates, accuracy tolerances, and campus distance calculations
                    </p>
                </div>
                <span class="status-pill font-mono text-xs" style="background: #1E293B; color: #38BDF8; border: 1px solid #334155;">
                    Total Records: ${locationChecks.size()}
                </span>
            </div>

            <div class="portal-table-responsive" style="max-height: 440px; overflow-y: auto;">
                <table class="portal-data-table">
                    <thead>
                        <tr>
                            <th>Student</th>
                            <th>Roll Number</th>
                            <th>Class / Batch</th>
                            <th>Scheduled Slot</th>
                            <th>Server Timestamp</th>
                            <th>GPS Coordinates &amp; Accuracy</th>
                            <th>Distance to Campus</th>
                            <th>Verification Result</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty locationChecks}">
                                <c:forEach var="chk" items="${locationChecks}">
                                    <tr style="${chk.flaggedForReview ? 'background: #FEF2F2;' : ''}">
                                        <td>
                                            <span class="font-bold text-dark">${chk.studentName}</span>
                                        </td>
                                        <td><span class="font-mono text-muted font-semibold text-xs">${chk.studentRollNo}</span></td>
                                        <td>${chk.studentBranch} &ndash; ${chk.studentSection}</td>
                                        <td><span class="font-mono text-xs font-semibold">${chk.formattedScheduledTime}</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${chk.serverTimestamp != null}">
                                                    <span class="font-mono text-xs">${chk.formattedServerTimestamp}</span>
                                                </c:when>
                                                <c:otherwise>&mdash;</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${chk.lat != null}">
                                                    <span class="font-mono text-xs font-semibold text-dark">
                                                        ${chk.lat}, ${chk.lng}
                                                        <span style="color: ${chk.accuracy != null && chk.accuracy > 100 ? '#DC2626' : '#16A34A'};">
                                                            (&plusmn;${Math.round(chk.accuracy)}m)
                                                        </span>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-xs text-muted">&mdash;</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${chk.distanceMeters != null}">
                                                    <span class="font-mono text-xs ${chk.distanceMeters > 1000 ? 'text-red font-bold' : 'text-green'}">
                                                        ${Math.round(chk.distanceMeters)}m
                                                    </span>
                                                </c:when>
                                                <c:otherwise>&mdash;</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${chk.verificationResult == 'VERIFIED_CAMPUS'}">
                                                    <span class="badge-status badge-present">✓ ON CAMPUS</span>
                                                </c:when>
                                                <c:when test="${chk.verificationResult == 'OUTSIDE_GEOFENCE' || chk.flaggedForReview}">
                                                    <span class="badge-status badge-absent" style="background: #DC2626; color: #FFFFFF; font-weight: 800;">
                                                        ⚠️ OUTSIDE CAMPUS (REVIEW)
                                                    </span>
                                                </c:when>
                                                <c:when test="${chk.verificationResult == 'ACCURACY_POOR'}">
                                                    <span class="badge-status" style="background: #FEF3C7; color: #B45309; font-weight: 700;">
                                                        POOR ACCURACY (&gt;100m)
                                                    </span>
                                                </c:when>
                                                <c:when test="${chk.verificationResult == 'GPS_UNAVAILABLE' || chk.verificationResult == 'PERMISSION_DENIED'}">
                                                    <span class="badge-status" style="background: #FFEDD5; color: #C2410C; font-weight: 700;">
                                                        ${chk.verificationResult}
                                                    </span>
                                                </c:when>
                                                <c:when test="${chk.verificationResult == 'EXPIRED_UNREACHABLE' || chk.status == 'EXPIRED'}">
                                                    <span class="badge-status" style="background: #F1F5F9; color: #64748B;">
                                                        OFFLINE / EXPIRED
                                                    </span>
                                                </c:when>
                                                <c:when test="${chk.status == 'CANCELLED'}">
                                                    <span class="badge-status pill-neutral">CANCELLED (OFF)</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-status" style="background: #E0F2FE; color: #0369A1; font-weight: 700;">
                                                        ⏳ SCHEDULED
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="text-center py-20 text-muted text-xs">
                                        No location verification checks recorded yet for today.
                                        Checks are generated when students complete morning attendance while system is active.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Settings & Manual Immediate Trigger 2-Column Grid -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: 1.5rem; margin-bottom: 2rem;">

            <!-- Configuration & Interval Rules Form -->
            <div class="card p-20" style="margin: 0; background: #FFFFFF; border: 1.5px solid #CBD5E1; border-radius: 12px;">
                <div class="flex items-center gap-8 mb-16">
                    <div class="step-icon-circle bg-blue-light text-blue" style="width: 36px; height: 36px; font-size: 1rem;">⚙️</div>
                    <div>
                        <h4 class="font-bold text-dark text-base" style="margin: 0;">Automated Randomizer Configuration</h4>
                        <p class="text-xs text-muted" style="margin: 2px 0 0 0;">Window limits, intervals, and challenge frequency</p>
                    </div>
                </div>

                <form method="post" action="${pageContext.request.contextPath}/admin/randomizer">
                    <input type="hidden" name="action" value="update_randomizer_settings">

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px;">
                        <div>
                            <label class="text-xs text-muted font-bold" for="earliestTime" style="text-transform: uppercase;">Earliest Time</label>
                            <input type="time" id="earliestTime" name="earliestTime" value="${randomizerSettings.earliestTime.toString().substring(0,5)}" min="09:05" max="15:40" required class="form-control" style="font-weight: 700;">
                            <span class="text-xs text-muted" style="display: block; margin-top: 2px;">Min 09:05 AM</span>
                        </div>
                        <div>
                            <label class="text-xs text-muted font-bold" for="latestTime" style="text-transform: uppercase;">Latest Time</label>
                            <input type="time" id="latestTime" name="latestTime" value="${randomizerSettings.latestTime.toString().substring(0,5)}" min="09:05" max="15:40" required class="form-control" style="font-weight: 700;">
                            <span class="text-xs text-muted" style="display: block; margin-top: 2px;">Max 03:40 PM</span>
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px;">
                        <div>
                            <label class="text-xs text-muted font-bold" for="durationMinutes" style="text-transform: uppercase;">Challenge Duration (Min)</label>
                            <input type="number" id="durationMinutes" name="durationMinutes" min="1" max="30" value="${randomizerSettings.durationMinutes}" required class="form-control" style="font-weight: 700;">
                            <span class="text-xs text-muted" style="display: block; margin-top: 2px;">1 &ndash; 30 minutes</span>
                        </div>
                        <div>
                            <label class="text-xs text-muted font-bold" for="challengesPerDay" style="text-transform: uppercase;">Checks Per Day</label>
                            <input type="number" id="challengesPerDay" name="challengesPerDay" min="1" max="10" value="${randomizerSettings.challengesPerDay}" required class="form-control" style="font-weight: 700;">
                            <span class="text-xs text-muted" style="display: block; margin-top: 2px;">Default: 4 &ndash; 5 checks</span>
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px;">
                        <div>
                            <label class="text-xs text-muted font-bold" for="minIntervalMinutes" style="text-transform: uppercase;">Min Interval (Min)</label>
                            <input type="number" id="minIntervalMinutes" name="minIntervalMinutes" min="10" max="180" value="${randomizerSettings.minIntervalMinutes}" required class="form-control" style="font-weight: 700;">
                        </div>
                        <div>
                            <label class="text-xs text-muted font-bold" for="maxIntervalMinutes" style="text-transform: uppercase;">Max Interval (Min)</label>
                            <input type="number" id="maxIntervalMinutes" name="maxIntervalMinutes" min="10" max="240" value="${randomizerSettings.maxIntervalMinutes}" required class="form-control" style="font-weight: 700;">
                        </div>
                    </div>

                    <div class="mb-16">
                        <label class="text-xs text-muted font-bold" for="targetType" style="text-transform: uppercase;">Target Student Group</label>
                        <select id="targetType" name="targetType" class="form-control" style="font-weight: 700;">
                            <option value="ALL" ${randomizerSettings.targetType == 'ALL' ? 'selected' : ''}>All Present / Checked-in Students</option>
                            <option value="BRANCH" ${randomizerSettings.targetType == 'BRANCH' ? 'selected' : ''}>Specific Branch (CSE, IT, ECE)</option>
                            <option value="CLASS" ${randomizerSettings.targetType == 'CLASS' ? 'selected' : ''}>Specific Academic Section</option>
                        </select>
                        <input type="hidden" name="targetValue" value="ALL">
                    </div>

                    <div class="flex-between items-center">
                        <label style="display: flex; align-items: center; gap: 8px; font-size: 0.85rem; font-weight: 700; cursor: pointer; color: #1E293B;">
                            <input type="checkbox" name="enabled" value="true" ${randomizerSettings.enabled ? 'checked' : ''}>
                            Enable Randomizer System
                        </label>

                        <button type="submit" class="btn-sm-action" style="background: #2563EB; color: #FFFFFF; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 800; cursor: pointer; box-shadow: 0 2px 8px rgba(37,99,235,0.3);">
                            💾 Save Settings
                        </button>
                    </div>
                </form>
            </div>

            <!-- Manual Immediate Challenge Trigger Form -->
            <div class="card p-20" style="margin: 0; background: #FFFFFF; border: 1.5px solid #CBD5E1; border-radius: 12px; display: flex; flex-direction: column; justify-content: space-between;">
                <div>
                    <div class="flex items-center gap-8 mb-16">
                        <div class="step-icon-circle bg-blue-light text-blue" style="width: 36px; height: 36px; font-size: 1rem; background: #FEF3C7; color: #B45309;">⚡</div>
                        <div>
                            <h4 class="font-bold text-dark text-base" style="margin: 0;">Instant Manual Check Trigger</h4>
                            <p class="text-xs text-muted" style="margin: 2px 0 0 0;">Broadcast an immediate random check challenge to students</p>
                        </div>
                    </div>

                    <p class="text-xs text-muted mb-16">
                        Triggers a surprise real-time challenge requiring checked-in students to submit their browser GPS location immediately.
                    </p>

                    <form method="post" action="${pageContext.request.contextPath}/admin/randomizer">
                        <input type="hidden" name="action" value="manual_trigger">

                        <div class="mb-12">
                            <label class="text-xs text-muted font-bold" for="manualDur" style="text-transform: uppercase;">Response Window Duration</label>
                            <select id="manualDur" name="durationMinutes" class="form-control" style="font-weight: 700;">
                                <option value="3">3 Minutes (High Urgency)</option>
                                <option value="5" selected>5 Minutes (Standard Campus Window)</option>
                                <option value="10">10 Minutes (Extended Window)</option>
                            </select>
                        </div>

                        <div class="mb-16">
                            <label class="text-xs text-muted font-bold" for="manualTarget" style="text-transform: uppercase;">Recipient Scope</label>
                            <select id="manualTarget" name="targetType" class="form-control" style="font-weight: 700;">
                                <option value="ALL" selected>All Currently Checked-In Students</option>
                            </select>
                            <input type="hidden" name="targetValue" value="ALL">
                        </div>

                        <button type="submit" class="btn-sm-action" style="background: linear-gradient(135deg, #D97706 0%, #B45309 100%); color: #FFFFFF; border: none; padding: 12px 20px; border-radius: 8px; font-weight: 800; cursor: pointer; width: 100%; font-size: 0.95rem; box-shadow: 0 4px 12px rgba(217,119,6,0.3);">
                            ⚡ Fire Immediate Verification Challenge Now
                        </button>
                    </form>
                </div>

                <div class="mt-16 p-12" style="background: #F8FAFC; border: 1px dashed #CBD5E1; border-radius: 8px;">
                    <span class="text-xs text-muted">
                        🔒 <strong>Academic Safety Guarantee:</strong> If lunch break or checkout is active, the system will prevent unfair verification penalties.
                    </span>
                </div>
            </div>

        </div>
            </main>

            <!-- Reusable Modals (Reports, Settings, Notifications) -->
            <jsp:include page="/WEB-INF/views/includes/admin-modals.jsp" />
        </div>
    </div>

    <!-- Admin Portal Script -->
    <script src="${pageContext.request.contextPath}/js/admin-portal.js"></script>
</body>
</html>
