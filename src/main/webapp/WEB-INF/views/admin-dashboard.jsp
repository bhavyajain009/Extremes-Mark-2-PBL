<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis — Admin Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-portal.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo.png">
</head>
<body class="admin-body">

    <div class="admin-layout">
        <!-- 1. Left Dark Navy Sidebar Navigation -->
        <jsp:include page="/WEB-INF/views/includes/admin-sidebar.jsp">
            <jsp:param name="activeSection" value="dashboard" />
        </jsp:include>

        <!-- 2. Main Content Area to the Right of Sidebar -->
        <div class="admin-main-wrapper">
            <!-- Top White Header Bar -->
            <jsp:include page="/WEB-INF/views/includes/admin-header.jsp" />

            <!-- Dashboard Content Container -->
            <main class="admin-content-area">

                <!-- Flash Notifications -->
                <c:if test="${not empty sessionScope.flashSuccess}">
                    <div class="alert-banner alert-banner-success" style="margin-bottom: 1.25rem;">
                        <span><strong>Success:</strong> ${sessionScope.flashSuccess}</span>
                    </div>
                    <% session.removeAttribute("flashSuccess"); %>
                </c:if>
                <c:if test="${not empty sessionScope.flashError}">
                    <div class="alert-banner" style="background: #FEE2E2; border: 1.5px solid #EF4444; color: #991B1B; padding: 0.85rem 1rem; border-radius: 10px; margin-bottom: 1.25rem; font-size: 0.9rem;">
                        <span><strong>Notice:</strong> ${sessionScope.flashError}</span>
                    </div>
                    <% session.removeAttribute("flashError"); %>
                </c:if>

                <!-- Hero Welcome Banner -->
                <div class="admin-hero-banner">
                    <div class="admin-hero-left">
                        <span class="admin-badge-purple">Master Administration</span>
                        <h1 class="admin-hero-title">
                            Welcome, <c:choose><c:when test="${not empty admin.name}">${admin.name}</c:when><c:otherwise>Chief Administrator</c:otherwise></c:choose>
                        </h1>
                        <p class="admin-hero-subtitle">Extremis Student Attendance Management System Control Console</p>
                    </div>

                    <div class="admin-hero-right">
                        <svg class="admin-hero-calendar-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line>
                            <line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                        <div class="admin-hero-date-text">
                            <span class="admin-hero-date-val" id="adminCurrentDate">Wednesday, 15 Oct 2026</span>
                            <span class="admin-hero-time-val" id="adminCurrentTime">01:35 PM | Asia/Kolkata</span>
                        </div>
                    </div>
                </div>

                <!-- Two Live Status Overview Cards (Side by Side) -->
                <div class="admin-status-grid-2">

                    <!-- Card 1: Attendance Timers Live Status -->
                    <div class="admin-card">
                        <div>
                            <div class="admin-card-header">
                                <div class="admin-card-title-group">
                                    <div class="admin-card-icon-circle">
                                        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <circle cx="12" cy="12" r="10"></circle>
                                            <polyline points="12 6 12 12 16 14"></polyline>
                                        </svg>
                                    </div>
                                    <h3 class="admin-card-title">Attendance Timers Live Status</h3>
                                </div>
                                <span class="admin-status-pill admin-pill-blue-outline font-mono">Asia/Kolkata</span>
                            </div>

                            <p class="admin-card-subtitle">Active time restrictions and slot windows for morning arrivals and departures.</p>

                            <div class="admin-timing-boxes-grid">
                                <!-- Check-in Timer Box -->
                                <div class="admin-timing-box">
                                    <span class="admin-timing-label">Check-In Timer</span>
                                    <div>
                                        <c:choose>
                                            <c:when test="${timerSettings.checkinTimerEnabled}">
                                                <span class="admin-status-pill admin-pill-green">● ENFORCED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="admin-status-pill admin-pill-gray">○ ANYTIME</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="admin-timing-value">
                                        ${timerSettings.formattedCheckinWindow} (${timerSettings.checkinDurationMinutes}m)
                                    </div>
                                </div>

                                <!-- Check-out Timer Box -->
                                <div class="admin-timing-box">
                                    <span class="admin-timing-label">Check-Out Timer</span>
                                    <div>
                                        <c:choose>
                                            <c:when test="${timerSettings.checkoutTimerEnabled}">
                                                <span class="admin-status-pill admin-pill-green">● ENFORCED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="admin-status-pill admin-pill-gray">○ ANYTIME</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="admin-timing-value">
                                        ${timerSettings.formattedCheckoutWindow} (${timerSettings.checkoutDurationMinutes}m)
                                    </div>
                                </div>
                            </div>
                        </div>

                        <a href="${pageContext.request.contextPath}/admin/timers" class="admin-btn-primary">
                            <span>Manage Attendance Timers</span>
                            <span>&rarr;</span>
                        </a>
                    </div>

                    <!-- Card 2: Random Location Verification Status -->
                    <div class="admin-card">
                        <div>
                            <div class="admin-card-header">
                                <div class="admin-card-title-group">
                                    <div class="admin-card-icon-circle" style="background: #EFF6FF; color: #1D64F2;">
                                        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                                            <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                                        </svg>
                                    </div>
                                    <h3 class="admin-card-title">Random Location Verification Status</h3>
                                </div>

                                <c:choose>
                                    <c:when test="${not randomizerSettings.enabled}">
                                        <span class="admin-status-pill admin-pill-gray">○ OFF</span>
                                    </c:when>
                                    <c:when test="${randomizerSettings.paused}">
                                        <span class="admin-status-pill admin-pill-amber">⏸ PAUSED</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="admin-status-pill admin-pill-green">● ACTIVE</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <p class="admin-card-subtitle">Unpredictable background GPS checks (4–5/day, 9:05 AM – 3:40 PM, excluding lunch).</p>

                            <!-- 4 Telemetry Metrics Tiles -->
                            <div class="admin-telemetry-grid">
                                <div class="admin-telemetry-tile tile-blue">
                                    <span class="admin-telemetry-num">${totalLocationChecks}</span>
                                    <span class="admin-telemetry-label">Total Checks</span>
                                </div>
                                <div class="admin-telemetry-tile tile-green">
                                    <span class="admin-telemetry-num">${verifiedCampusCount}</span>
                                    <span class="admin-telemetry-label">Verified</span>
                                </div>
                                <div class="admin-telemetry-tile tile-red">
                                    <span class="admin-telemetry-num">${outsideGeofenceCount}</span>
                                    <span class="admin-telemetry-label">Outside</span>
                                </div>
                                <div class="admin-telemetry-tile tile-amber">
                                    <span class="admin-telemetry-num">${gpsFailuresCount}</span>
                                    <span class="admin-telemetry-label">GPS Issues</span>
                                </div>
                            </div>
                        </div>

                        <a href="${pageContext.request.contextPath}/admin/randomizer" class="admin-btn-dark">
                            <span>Open Verification Control Center</span>
                            <span>&rarr;</span>
                        </a>
                    </div>

                </div>

                <!-- Statistics Row (8 Cards Matching Reference Image) -->
                <div class="admin-stats-row">
                    <!-- 1. Students -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-blue">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                            </svg>
                            <span class="admin-stat-val">${totalStudents}</span>
                        </div>
                        <span class="admin-stat-lbl">Students</span>
                    </div>

                    <!-- 2. Teachers -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-green">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                            <span class="admin-stat-val">${totalFaculties}</span>
                        </div>
                        <span class="admin-stat-lbl">Teachers</span>
                    </div>

                    <!-- 3. Classes -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-blue">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                            </svg>
                            <span class="admin-stat-val">${totalClasses}</span>
                        </div>
                        <span class="admin-stat-lbl">Classes</span>
                    </div>

                    <!-- 4. Batches -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-teal">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                                <polyline points="2 17 12 22 22 17"></polyline>
                                <polyline points="2 12 12 17 22 12"></polyline>
                            </svg>
                            <span class="admin-stat-val">${totalBatches}</span>
                        </div>
                        <span class="admin-stat-lbl">Batches</span>
                    </div>

                    <!-- 5. Assignments -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-purple">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"></path>
                                <rect x="8" y="2" width="8" height="4" rx="1" ry="1"></rect>
                            </svg>
                            <span class="admin-stat-val">${totalAssignments}</span>
                        </div>
                        <span class="admin-stat-lbl">Assignments</span>
                    </div>

                    <!-- 6. Overrides -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-red">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                                <line x1="12" y1="9" x2="12" y2="13"></line>
                                <line x1="12" y1="17" x2="12.01" y2="17"></line>
                            </svg>
                            <span class="admin-stat-val">${totalOverrides}</span>
                        </div>
                        <span class="admin-stat-lbl">Overrides</span>
                    </div>

                    <!-- 7. Today Check-Ins -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-teal">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                                <line x1="16" y1="2" x2="16" y2="6"></line>
                                <line x1="8" y1="2" x2="8" y2="6"></line>
                                <polyline points="9 11 12 14 22 4"></polyline>
                            </svg>
                            <span class="admin-stat-val">${todayCheckIns}</span>
                        </div>
                        <span class="admin-stat-lbl">Today Check-Ins</span>
                    </div>

                    <!-- 8. Locked Devices -->
                    <div class="admin-stat-card">
                        <div class="admin-stat-top stat-color-orange">
                            <svg class="admin-stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                            </svg>
                            <span class="admin-stat-val">${activeSessions}</span>
                        </div>
                        <span class="admin-stat-lbl">Locked Devices</span>
                    </div>
                </div>

                <!-- Quick Access Navigation Cards (3 Cards Matching Reference Image) -->
                <div class="admin-quick-grid-3">
                    <!-- Quick Card 1: Attendance Timers -->
                    <a href="${pageContext.request.contextPath}/admin/timers" class="admin-quick-card">
                        <div class="admin-quick-top">
                            <div class="admin-quick-icon-box" style="background: #1D64F2;">
                                <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <polyline points="12 6 12 12 16 14"></polyline>
                                </svg>
                            </div>
                            <div class="admin-quick-info">
                                <h4>Attendance Timers</h4>
                                <p>Check-in / Check-out slots &amp; duration</p>
                            </div>
                        </div>
                        <div class="admin-quick-bottom">
                            <span class="admin-quick-action-link">Configure Timers &rarr;</span>
                            <div class="admin-quick-arrow-circle">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                    <line x1="5" y1="12" x2="19" y2="12"></line>
                                    <polyline points="12 5 19 12 12 19"></polyline>
                                </svg>
                            </div>
                        </div>
                    </a>

                    <!-- Quick Card 2: Random Verification -->
                    <a href="${pageContext.request.contextPath}/admin/randomizer" class="admin-quick-card">
                        <div class="admin-quick-top">
                            <div class="admin-quick-icon-box" style="background: #059669;">
                                <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
                                    <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                                </svg>
                            </div>
                            <div class="admin-quick-info">
                                <h4>Random Verification</h4>
                                <p>GPS geofence controls &amp; live logs</p>
                            </div>
                        </div>
                        <div class="admin-quick-bottom">
                            <span class="admin-quick-action-link">Control Center &rarr;</span>
                            <div class="admin-quick-arrow-circle">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                    <line x1="5" y1="12" x2="19" y2="12"></line>
                                    <polyline points="12 5 19 12 12 19"></polyline>
                                </svg>
                            </div>
                        </div>
                    </a>

                    <!-- Quick Card 3: Student Directory -->
                    <a href="${pageContext.request.contextPath}/admin/students" class="admin-quick-card">
                        <div class="admin-quick-top">
                            <div class="admin-quick-icon-box" style="background: #1E3A8A;">
                                <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                    <circle cx="9" cy="7" r="4"></circle>
                                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                </svg>
                            </div>
                            <div class="admin-quick-info">
                                <h4>Student Directory</h4>
                                <p>Class-wise view &amp; Excel mass import</p>
                            </div>
                        </div>
                        <div class="admin-quick-bottom">
                            <span class="admin-quick-action-link">Manage Students &rarr;</span>
                            <div class="admin-quick-arrow-circle">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                    <line x1="5" y1="12" x2="19" y2="12"></line>
                                    <polyline points="12 5 19 12 12 19"></polyline>
                                </svg>
                            </div>
                        </div>
                    </a>
                </div>

                <!-- Student Device Session Manager Card -->
                <div class="admin-table-container">
                    <div class="admin-table-header">
                        <div>
                            <h3 class="admin-table-title">Student Device Security &amp; Single-Device Locks</h3>
                            <p class="admin-table-sub">Enforce single-device attendance policy and unlock student devices when needed</p>
                        </div>
                        <span class="admin-status-pill admin-pill-gray font-mono">Total Students: ${students.size()}</span>
                    </div>

                    <div style="overflow-x: auto;">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Student Name</th>
                                    <th>Roll Number</th>
                                    <th>Branch &amp; Section</th>
                                    <th>Device Session Status</th>
                                    <th style="text-align: right;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="s" items="${students}">
                                    <tr>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: 10px;">
                                                <div style="width: 32px; height: 32px; border-radius: 50%; background: #EFF6FF; color: #1D64F2; font-weight: 700; font-size: 0.85rem; display: flex; align-items: center; justify-content: center;">
                                                    ${s.name.substring(0, 1).toUpperCase()}
                                                </div>
                                                <span style="font-weight: 700; color: #0F172A;">${s.name}</span>
                                            </div>
                                        </td>
                                        <td><span class="font-mono" style="color: #64748B; font-weight: 600;">${s.rollNo}</span></td>
                                        <td>${s.branch} &ndash; ${s.section}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty s.sessionToken}">
                                                    <span class="admin-status-pill" style="background: #FEF2F2; color: #DC2626; border: 1px solid #FECACA;">
                                                        🔒 Device Locked
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="admin-status-pill" style="background: #F0FDF4; color: #16A34A; border: 1px solid #BBF7D0;">
                                                        ✓ Unlocked / Available
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <c:if test="${not empty s.sessionToken}">
                                                <form method="post" action="${pageContext.request.contextPath}/admin/dashboard" style="display:inline; margin: 0;">
                                                    <input type="hidden" name="action" value="reset_session">
                                                    <input type="hidden" name="studentId" value="${s.id}">
                                                    <button type="submit" class="btn-sm-action btn-secondary-action" style="color: #DC2626; font-size: 0.8rem; font-weight: 700; border-radius: 6px; padding: 5px 12px; border: 1px solid #FECACA; background: #FEF2F2; cursor: pointer;">
                                                        Reset Device Lock
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
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
