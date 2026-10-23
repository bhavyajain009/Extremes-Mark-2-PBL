<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis - Student Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo.png">
    <link rel="manifest" href="${pageContext.request.contextPath}/manifest.json">
    <meta name="theme-color" content="#dc2626">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
</head>
<body>
<div class="app-container">

    <div class="content-wrap">
        <!-- App Header on Home -->
        <header class="app-header">
            <div class="brand-wrap">
                <h1>Extremis</h1>
                <p>Student Dashboard</p>
            </div>

            <!-- Desktop Navigation Links (Visible on Tablet & Desktop) -->
            <nav class="desktop-nav" aria-label="Main Navigation">
                <a href="${pageContext.request.contextPath}/dashboard" class="desktop-nav-link ${activeTab == 'home' ? 'active' : ''}">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                        <polyline points="9 22 9 12 15 12 15 22"></polyline>
                    </svg>
                    <span>Home</span>
                </a>
                <a href="${pageContext.request.contextPath}/report" class="desktop-nav-link ${activeTab == 'report' ? 'active' : ''}">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                    </svg>
                    <span>Report</span>
                </a>
                <a href="${pageContext.request.contextPath}/profile" class="desktop-nav-link ${activeTab == 'profile' ? 'active' : ''}">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span>Profile</span>
                </a>
            </nav>

            <a href="${pageContext.request.contextPath}/notifications" class="header-action-btn" title="Notifications" aria-label="Notifications">
                <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                </svg>
                <c:if test="${hasUnreadNotices}">
                    <span class="badge-dot"></span>
                </c:if>
            </a>
        </header>

        <!-- Flash messages -->
        <c:if test="${not empty flashSuccess}">
            <div class="alert alert-success">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                <span>${flashSuccess}</span>
            </div>
            <c:remove var="flashSuccess" scope="session"/>
        </c:if>
        <c:if test="${not empty flashError}">
            <div class="alert alert-error">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <span>${flashError}</span>
            </div>
            <c:remove var="flashError" scope="session"/>
        </c:if>

        <!-- Responsive Dashboard Grid -->
        <div class="dashboard-grid">

            <!-- Primary Column: Student Info, Notices, and Statistics -->
            <div class="dashboard-main-col">

                <!-- 1. Important Notice Banner (Megaphone icon + Chevron) -->
                <c:if test="${not empty latestNotice}">
                    <a href="${pageContext.request.contextPath}/notifications" class="notice-card-teaser">
                        <div class="notice-teaser-left">
                            <div class="notice-teaser-icon">
                                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                    <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                                    <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
                                    <path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path>
                                </svg>
                            </div>
                            <div>
                                <div class="notice-teaser-title">${latestNotice.title}</div>
                                <div class="notice-teaser-sub">Midterm exam form submission</div>
                                <div class="notice-teaser-date">last date: 15 Oct 2026</div>
                            </div>
                        </div>
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="9 18 15 12 9 6"></polyline>
                        </svg>
                    </a>
                </c:if>

                <!-- 2. Profile Card (Avatar + Name + Roll + Branch/Section) -->
                <div class="card profile-card">
                    <div class="profile-avatar-wrap">
                        <svg viewBox="0 0 24 24" width="30" height="30" fill="currentColor">
                            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"></path>
                        </svg>
                    </div>
                    <div class="profile-info">
                        <h2>${currentStudent.name}</h2>
                        <div class="profile-roll">${currentStudent.rollNo}</div>
                        <div class="profile-branch-row">
                            <svg viewBox="0 0 24 24" width="15" height="15" fill="#64748B">
                                <path d="M12 3L1 9l11 6 9-4.91V17h2V9L12 3z M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82z"/>
                            </svg>
                            <span>${currentStudent.branch} &ndash; ${currentStudent.section}</span>
                        </div>
                    </div>
                </div>

                <!-- 3. Dual Stats Cards (Today's Attendance + Overall Attendance) -->
                <div class="dual-cards-row">
                    <!-- Card A: Today's Attendance -->
                    <div class="card mini-stat-card">
                        <div class="mini-stat-header">
                            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#1D4ED8" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                                <line x1="16" y1="2" x2="16" y2="6"></line>
                                <line x1="8" y1="2" x2="8" y2="6"></line>
                                <line x1="3" y1="10" x2="21" y2="10"></line>
                            </svg>
                            <div>Today's<br>Attendance</div>
                        </div>

                        <div>
                            <c:choose>
                                <c:when test="${not empty todayAttendance && not empty todayAttendance.checkInTime}">
                                    <span class="chip-status chip-present">
                                        <c:choose>
                                            <c:when test="${not empty todayAttendance.checkOutTime}">Checked Out</c:when>
                                            <c:otherwise>Checked In</c:otherwise>
                                        </c:choose>
                                    </span>
                                    <div class="mini-stat-time">
                                        <c:choose>
                                            <c:when test="${not empty todayAttendance.formattedCheckInTime}">
                                                ${todayAttendance.formattedCheckInTime}
                                            </c:when>
                                            <c:otherwise>&mdash;</c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <span class="chip-status chip-not-marked">Not Checked In</span>
                                    <div class="mini-stat-time">&mdash;</div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="mini-stat-footer">College Attendance (Daily)</div>
                    </div>

                    <!-- Card B: Overall Attendance (CSS Conic-Gradient Ring) -->
                    <div class="card mini-stat-card" style="text-align: center;">
                        <div class="mini-stat-header" style="justify-content: flex-start;">
                            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#1D4ED8" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="18" y1="20" x2="18" y2="10"></line>
                                <line x1="12" y1="20" x2="12" y2="4"></line>
                                <line x1="6" y1="20" x2="6" y2="14"></line>
                            </svg>
                            <div style="text-align: left;">Overall<br>Attendance</div>
                        </div>

                        <!-- Conic Gradient Progress Ring -->
                        <div class="circular-progress-wrap" style="background: conic-gradient(#1D4ED8 ${overallPercentage * 3.6}deg, #E2E8F0 0deg);">
                            <div class="circular-progress-center">
                                ${overallPercentage}%
                            </div>
                        </div>

                        <div class="mini-stat-footer">78 / 100 classes</div>
                    </div>
                </div>

                <!-- 4. Attendance Report Teaser Card -->
                <a href="${pageContext.request.contextPath}/report" class="card report-teaser-link">
                    <div class="report-teaser-left">
                        <div class="report-icon-box">
                            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                <polyline points="14 2 14 8 20 8"></polyline>
                                <line x1="16" y1="13" x2="8" y2="13"></line>
                                <line x1="16" y1="17" x2="8" y2="17"></line>
                                <polyline points="10 9 9 9 8 9"></polyline>
                            </svg>
                        </div>
                        <div>
                            <div style="font-size: 0.85rem; font-weight: 800; color: var(--text-main);">Attendance Report</div>
                            <div style="font-size: 0.76rem; color: var(--text-muted); margin-top: 1px;">View monthly attendance breakdown</div>
                        </div>
                    </div>
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="9 18 15 12 9 6"></polyline>
                    </svg>
                </a>

            </div>

            <!-- Side Column: Action Panel / Verification Button -->
            <div class="dashboard-side-col">
                <div class="card action-panel-card">
                    <div class="action-panel-header">
                        <span class="action-panel-title">Campus Verification</span>
                        <span class="status-pill ${buttonColor == 'blue' ? 'pill-present' : (buttonColor == 'red' ? 'pill-absent' : 'pill-neutral')}">
                            ${buttonLabel}
                        </span>
                    </div>

                    <!-- Big Circular Action Button -->
                    <div class="big-circle-container">
                        <c:choose>
                            <c:when test="${buttonEnabled}">
                                <a href="${actionUrl}" class="big-circle-btn ${buttonColor == 'red' ? 'btn-red' : 'btn-blue'}">
                                    <c:choose>
                                        <c:when test="${buttonColor == 'red'}">
                                            <!-- Check Out arrow icon -->
                                            <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                                                <polyline points="16 17 21 12 16 7"></polyline>
                                                <line x1="21" y1="12" x2="9" y2="12"></line>
                                            </svg>
                                        </c:when>
                                        <c:otherwise>
                                            <!-- Check In concentric rings icon -->
                                            <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                <circle cx="12" cy="12" r="9.5"></circle>
                                                <circle cx="12" cy="12" r="6"></circle>
                                                <circle cx="12" cy="12" r="2.5" fill="currentColor"></circle>
                                            </svg>
                                        </c:otherwise>
                                    </c:choose>
                                    <span class="btn-label">${buttonLabel}</span>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <div class="big-circle-btn btn-gray">
                                    <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="9"></circle>
                                        <circle cx="12" cy="12" r="3" fill="currentColor"></circle>
                                    </svg>
                                    <span class="btn-label">${buttonLabel}</span>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="big-circle-subtext">${buttonSubtext}</div>

                        <div class="info-note-pill">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#1D4ED8" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink: 0;">
                                <circle cx="12" cy="12" r="10"></circle>
                                <line x1="12" y1="16" x2="12" y2="12"></line>
                                <line x1="12" y1="8" x2="12.01" y2="8"></line>
                            </svg>
                            <span>After 3:40 PM, this option will change to <strong>Check Out</strong> instead of Check In.</span>
                        </div>
                    </div>
                </div>
            </div>

        </div>

    </div>

    <!-- Shared Bottom Navigation Include (Visible only on mobile devices) -->
    <jsp:include page="/WEB-INF/views/includes/nav.jsp"/>

</div>
<script>
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('${pageContext.request.contextPath}/sw.js')
                .then(reg => console.log('Extremis ServiceWorker registered:', reg.scope))
                .catch(err => console.warn('Extremis ServiceWorker registration failed:', err));
        });
    }
</script>
</body>
</html>
