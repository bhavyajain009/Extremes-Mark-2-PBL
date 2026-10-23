<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis - Faculty Attendance Analytics & Management Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
</head>
<body class="bg-light">

    <!-- Top Navigation Bar -->
    <nav class="portal-navbar">
        <div class="portal-navbar-inner">
            <div class="portal-brand">
                <div class="portal-brand-logo">E</div>
                <div>
                    <div class="portal-brand-title">Extremis</div>
                    <div class="portal-brand-sub">Faculty Attendance Portal</div>
                </div>
            </div>

            <ul class="portal-nav-links">
                <li><a href="javascript:void(0)" onclick="switchTab('overview')" class="portal-nav-link ${activeTab == 'overview' ? 'active' : ''}" id="nav-overview">Student Overview</a></li>
                <li><a href="javascript:void(0)" onclick="switchTab('analytics')" class="portal-nav-link ${activeTab == 'analytics' ? 'active' : ''}" id="nav-analytics">Analytics & Trends</a></li>
                <li><a href="javascript:void(0)" onclick="switchTab('daily')" class="portal-nav-link ${activeTab == 'daily' ? 'active' : ''}" id="nav-daily">Daily Roster</a></li>
                <li><a href="javascript:void(0)" onclick="switchTab('reports')" class="portal-nav-link ${activeTab == 'reports' ? 'active' : ''}" id="nav-reports">Reports & Export</a></li>
            </ul>

            <div class="portal-nav-user">
                <div class="portal-user-badge">
                    <span class="portal-user-name">${faculty.name}</span>
                    <span class="portal-user-role">ID: ${faculty.facultyId} &bull; ${faculty.branch}</span>
                </div>
                <form action="${pageContext.request.contextPath}/logout" method="post" style="margin: 0;">
                    <button type="submit" class="btn-secondary-action btn-sm-action" title="Logout" style="color: var(--danger-red-start);">
                        Logout
                    </button>
                </form>
            </div>
        </div>
    </nav>

    <div class="app-container" style="max-width: 1400px; padding: 1.5rem 1.5rem 4rem;">

        <!-- Alert Notifications -->
        <c:if test="${not empty param.success}">
            <div class="alert-banner alert-banner-success" id="successBanner">
                <span><strong>Success:</strong> ${param.success}</span>
                <button type="button" onclick="document.getElementById('successBanner').style.display='none'" style="background:none;border:none;cursor:pointer;color:inherit;font-weight:700;">&times;</button>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert-banner alert-banner-error" id="errorBanner">
                <span><strong>Error:</strong> ${param.error}</span>
                <button type="button" onclick="document.getElementById('errorBanner').style.display='none'" style="background:none;border:none;cursor:pointer;color:inherit;font-weight:700;">&times;</button>
            </div>
        </c:if>

        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert-banner alert-banner-success mb-16" id="sessionFlashSuccess">
                <span><strong>Success:</strong> ${sessionScope.flashSuccess}</span>
                <button type="button" onclick="document.getElementById('sessionFlashSuccess').style.display='none'" style="background:none;border:none;cursor:pointer;color:inherit;font-weight:700;">&times;</button>
            </div>
            <% session.removeAttribute("flashSuccess"); %>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert-banner alert-banner-error mb-16" id="sessionFlashError">
                <span><strong>Error:</strong> ${sessionScope.flashError}</span>
                <button type="button" onclick="document.getElementById('sessionFlashError').style.display='none'" style="background:none;border:none;cursor:pointer;color:inherit;font-weight:700;">&times;</button>
            </div>
            <% session.removeAttribute("flashError"); %>
        </c:if>

        <c:if test="${locOutsideGeofence > 0}">
            <div class="card mb-16" style="background: #FEF2F2; border: 2px solid #F87171; padding: 1rem 1.25rem; border-radius: 12px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                <div class="flex items-center gap-12">
                    <span style="font-size: 1.8rem;">🚨</span>
                    <div>
                        <h4 style="color: #991B1B; font-weight: 800; font-size: 1rem; margin: 0;">Attendance Verification Alert: ${locOutsideGeofence} Student(s) Detected Outside Campus</h4>
                        <p style="color: #7F1D1D; font-size: 0.8rem; margin: 2px 0 0 0;">
                            Random location check detected student(s) outside the 1000m college geofence. Please verify if they had an authorized reason (e.g. medical, approved college duty) or confirm their absence.
                        </p>
                    </div>
                </div>
                <button type="button" class="btn-primary-action" onclick="switchTab('location')" style="background: #DC2626; font-size: 0.82rem; font-weight: 800; padding: 8px 16px; border: none; border-radius: 8px; cursor: pointer; color: #FFFFFF;">
                    Review Location Records Now &rarr;
                </button>
            </div>
        </c:if>

        <!-- 1. Faculty Profile & Cohort Assignment Header Banner -->
        <div class="card mb-16" style="background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%); color: #FFFFFF; padding: 1.25rem 1.5rem; border-radius: 14px;">
            <div class="flex-between" style="flex-wrap: wrap; gap: 1rem;">
                <div>
                    <div class="flex items-center gap-8" style="margin-bottom: 4px;">
                        <span class="status-pill" style="background: rgba(37, 99, 235, 0.35); color: #93C5FD;">
                            Authorized Faculty &bull; ${faculty.department != null ? faculty.department : faculty.branch}
                        </span>
                        <span class="status-pill" style="background: rgba(16, 185, 129, 0.25); color: #6EE7B7;">
                            Active Session 2026-27
                        </span>
                    </div>
                    <h2 class="font-bold text-xl text-white" style="margin: 4px 0 2px;">${faculty.name}</h2>
                    <p class="text-xs text-muted" style="color: #94A3B8;">
                        Faculty Code: <strong>${faculty.facultyId}</strong> &bull; Email: ${faculty.email} &bull; Dept: ${faculty.branch} - ${faculty.section}
                    </p>
                </div>

                <div style="text-align: right;">
                    <div class="text-xs text-muted" style="color: #94A3B8; margin-bottom: 6px;">Assigned Subjects & Classes:</div>
                    <div class="flex gap-6" style="flex-wrap: wrap; justify-content: flex-end;">
                        <c:choose>
                            <c:when test="${not empty assignments}">
                                <c:forEach var="a" items="${assignments}">
                                    <span class="status-pill" style="background: rgba(56, 189, 248, 0.2); color: #38BDF8; font-weight: 700;">
                                        ${a.classCode} (${a.batchCode != null ? a.batchCode : 'All'}) &bull; ${a.subject}
                                    </span>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="status-pill" style="background: rgba(255, 255, 255, 0.15); color: #FFFFFF;">
                                    ${faculty.branch} - ${faculty.section}
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <!-- Filter & Date Range Selector Toolbar -->
        <div class="filter-card mb-16 no-print">
            <form action="${pageContext.request.contextPath}/faculty/dashboard" method="get" id="dashboardFilterForm" class="filter-grid" style="grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));">
                <input type="hidden" name="tab" id="activeTabInput" value="${activeTab}">

                <div class="filter-field">
                    <label for="filterClass">Assigned Class</label>
                    <select name="classId" id="filterClass" class="filter-control" onchange="document.getElementById('dashboardFilterForm').submit()">
                        <c:forEach var="c" items="${assignedClasses}">
                            <option value="${c.id}" ${c.id == selectedClassId ? 'selected' : ''}>${c.code} - ${c.name}</option>
                        </c:forEach>
                        <c:if test="${empty assignedClasses}">
                            <option value="">${faculty.branch} - ${faculty.section}</option>
                        </c:if>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="filterBatch">Cohort Batch</label>
                    <select name="batchId" id="filterBatch" class="filter-control" onchange="document.getElementById('dashboardFilterForm').submit()">
                        <option value="">All Batches</option>
                        <c:forEach var="b" items="${classBatches}">
                            <option value="${b.id}" ${b.id == selectedBatchId ? 'selected' : ''}>Batch ${b.code} (${b.name})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="rangePreset">Reporting Period</label>
                    <select name="rangePreset" id="rangePreset" class="filter-control" onchange="onRangePresetChange(this.value)">
                        <option value="this_month" ${rangePreset == 'this_month' ? 'selected' : ''}>This Month (Oct 2026)</option>
                        <option value="last_7_days" ${rangePreset == 'last_7_days' ? 'selected' : ''}>Last 7 Days</option>
                        <option value="today" ${rangePreset == 'today' ? 'selected' : ''}>Today Only</option>
                        <option value="all_time" ${rangePreset == 'all_time' ? 'selected' : ''}>Full Academic Semester</option>
                        <option value="custom" ${rangePreset == 'custom' ? 'selected' : ''}>Custom Date Range</option>
                    </select>
                </div>

                <div class="filter-field" id="startDateGroup">
                    <label for="filterStartDate">From Date</label>
                    <input type="date" name="startDate" id="filterStartDate" class="filter-control" value="${startDate}">
                </div>

                <div class="filter-field" id="endDateGroup">
                    <label for="filterEndDate">To Date</label>
                    <input type="date" name="endDate" id="filterEndDate" class="filter-control" value="${endDate}">
                </div>

                <div class="filter-field" style="display: flex; gap: 0.5rem; flex-direction: row; align-items: flex-end;">
                    <button type="submit" class="btn-primary-action" style="flex: 1;">Apply Filters</button>
                    <a href="${pageContext.request.contextPath}/faculty/dashboard" class="btn-secondary-action" title="Reset Filters">Reset</a>
                </div>
            </form>
        </div>

        <!-- 2. 8-Dynamic KPI Summary Cards -->
        <div class="kpi-grid-8">
            <!-- 1. Enrolled Students -->
            <div class="kpi-card kpi-blue">
                <div>
                    <div class="kpi-lbl">Total Enrolled</div>
                    <div class="kpi-val">${analytics.totalEnrolled}</div>
                </div>
                <div class="kpi-sub">Students in Cohort</div>
            </div>

            <!-- 2. Conducted Sessions -->
            <div class="kpi-card kpi-indigo">
                <div>
                    <div class="kpi-lbl">Sessions Conducted</div>
                    <div class="kpi-val">${analytics.conductedSessions}</div>
                </div>
                <div class="kpi-sub">${startDate} &rarr; ${endDate}</div>
            </div>

            <!-- 3. Overall Attendance % -->
            <div class="kpi-card ${analytics.overallAttendancePercentage >= 75.0 ? 'kpi-green' : (analytics.overallAttendancePercentage >= 60.0 ? 'kpi-amber' : 'kpi-red')}">
                <div>
                    <div class="kpi-lbl">Attendance Rate</div>
                    <div class="kpi-val">${analytics.overallAttendancePercentage}%</div>
                </div>
                <div class="kpi-sub">
                    <c:choose>
                        <c:when test="${analytics.overallAttendancePercentage >= 75.0}">Institutional Safe</c:when>
                        <c:when test="${analytics.overallAttendancePercentage >= 60.0}">Needs Monitoring</c:when>
                        <c:otherwise>Critical Shortage</c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- 4. Total Present -->
            <div class="kpi-card kpi-green">
                <div>
                    <div class="kpi-lbl">Total Present</div>
                    <div class="kpi-val">${analytics.totalPresent}</div>
                </div>
                <div class="kpi-sub">Marked on time</div>
            </div>

            <!-- 5. Total Late -->
            <div class="kpi-card kpi-amber">
                <div>
                    <div class="kpi-lbl">Late Arrivals</div>
                    <div class="kpi-val">${analytics.totalLate}</div>
                </div>
                <div class="kpi-sub">Grace check-ins</div>
            </div>

            <!-- 6. Total Absent -->
            <div class="kpi-card kpi-red">
                <div>
                    <div class="kpi-lbl">Total Absent</div>
                    <div class="kpi-val">${analytics.totalAbsent}</div>
                </div>
                <div class="kpi-sub">Unattended sessions</div>
            </div>

            <!-- 7. Excused Sessions -->
            <div class="kpi-card kpi-cyan">
                <div>
                    <div class="kpi-lbl">Excused / OD</div>
                    <div class="kpi-val">${analytics.totalExcused}</div>
                </div>
                <div class="kpi-sub">Approved leaves</div>
            </div>

            <!-- 8. Shortage / At-Risk Count (<75%) -->
            <div class="kpi-card ${analytics.atRiskCount > 0 ? 'kpi-red' : 'kpi-green'}">
                <div>
                    <div class="kpi-lbl">Low Attendance</div>
                    <div class="kpi-val">${analytics.atRiskCount}</div>
                </div>
                <div class="kpi-sub">Below 75% threshold</div>
            </div>
        </div>

        <!-- Tab Navigation Buttons -->
        <div class="tab-nav no-print">
            <button type="button" class="tab-btn ${activeTab == 'overview' ? 'active' : ''}" onclick="switchTab('overview')">
                &#x1F4CA; Complete Student Overview (${totalEnrolled})
            </button>
            <button type="button" class="tab-btn ${activeTab == 'analytics' ? 'active' : ''}" onclick="switchTab('analytics')">
                &#x1F4C8; Class Visual Analytics & Trends
            </button>
            <button type="button" class="tab-btn ${activeTab == 'daily' ? 'active' : ''}" onclick="switchTab('daily')">
                &#x1F4C5; Attendance Calendar & Daily Records
            </button>
            <button type="button" class="tab-btn ${activeTab == 'reports' ? 'active' : ''}" onclick="switchTab('reports')">
                &#x1F4D1; Attendance Reports & Overrides Log
            </button>
            <button type="button" class="tab-btn ${activeTab == 'location' ? 'active' : ''}" onclick="switchTab('location')" id="nav-location" style="${locOutsideGeofence > 0 ? 'border: 1.5px solid #EF4444; color: #DC2626; font-weight: 800;' : ''}">
                🛰️ Random Location Checks (${locTotalChecks})
                <c:if test="${locOutsideGeofence > 0}">
                    <span class="status-pill" style="background: #DC2626; color: #FFFFFF; font-size: 0.68rem; padding: 2px 6px; font-weight: 800; margin-left: 4px;">${locOutsideGeofence} PENDING REVIEW</span>
                </c:if>
            </button>
        </div>

        <!-- =================================================================== -->
        <!-- TAB 1: COMPLETE STUDENT ATTENDANCE OVERVIEW TABLE (MOST IMPORTANT) -->
        <!-- =================================================================== -->
        <div id="tabContent-overview" style="display: ${activeTab == 'overview' ? 'block' : 'none'};">
            <div class="portal-table-container">
                <div class="table-header-bar flex-between" style="flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <h3 class="table-title">Complete Student Attendance Performance Overview</h3>
                        <p class="table-subtitle">
                            Displaying cumulative historical attendance for <strong>${currentClass != null ? currentClass.name : faculty.branch}</strong>
                            across period <strong>${startDate}</strong> to <strong>${endDate}</strong> (${analytics.conductedSessions} conducted sessions).
                        </p>
                    </div>

                    <!-- Table Search & Tier Filters -->
                    <div class="flex gap-8 items-center" style="flex-wrap: wrap;">
                        <input type="text" id="studentTableSearch" placeholder="Search student name or roll..."
                               class="filter-control" style="width: 220px; font-size: 0.82rem;" onkeyup="filterStudentTable()">

                        <div class="flex gap-4">
                            <a href="${pageContext.request.contextPath}/faculty/dashboard?classId=${selectedClassId}&batchId=${selectedBatchId}&startDate=${startDate}&endDate=${endDate}&tab=overview&tierFilter=ALL"
                               class="status-pill ${tierFilter == 'ALL' ? 'pill-neutral font-bold' : ''}" style="cursor: pointer;">All (${analytics.totalEnrolled})</a>
                            <a href="${pageContext.request.contextPath}/faculty/dashboard?classId=${selectedClassId}&batchId=${selectedBatchId}&startDate=${startDate}&endDate=${endDate}&tab=overview&tierFilter=SAFE"
                               class="tier-badge tier-badge-safe" style="cursor: pointer;">Safe (${analytics.safeCount})</a>
                            <a href="${pageContext.request.contextPath}/faculty/dashboard?classId=${selectedClassId}&batchId=${selectedBatchId}&startDate=${startDate}&endDate=${endDate}&tab=overview&tierFilter=WARNING"
                               class="tier-badge tier-badge-warning" style="cursor: pointer;">Warning (${analytics.warningCount})</a>
                            <a href="${pageContext.request.contextPath}/faculty/dashboard?classId=${selectedClassId}&batchId=${selectedBatchId}&startDate=${startDate}&endDate=${endDate}&tab=overview&tierFilter=CRITICAL"
                               class="tier-badge tier-badge-critical" style="cursor: pointer;">Critical (${analytics.criticalCount})</a>
                        </div>

                        <a href="${pageContext.request.contextPath}/faculty/export?classId=${selectedClassId}&batchId=${selectedBatchId}&startDate=${startDate}&endDate=${endDate}&type=summary"
                           class="btn-secondary-action btn-sm-action" title="Export this summary table to CSV">
                            &#x2193; Export CSV
                        </a>
                    </div>
                </div>

                <div class="portal-table-responsive">
                    <table class="portal-data-table" id="studentOverviewTable">
                        <thead>
                            <tr>
                                <th class="sortable-th ${sortCol == 'name' ? 'active-sort' : ''}" onclick="sortTable('name')">
                                    Student Name <span class="sort-icon">${sortCol == 'name' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th class="sortable-th ${sortCol == 'roll' ? 'active-sort' : ''}" onclick="sortTable('roll')">
                                    Roll Number <span class="sort-icon">${sortCol == 'roll' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th>Class & Batch</th>
                                <th class="sortable-th ${sortCol == 'sessions' ? 'active-sort' : ''}" onclick="sortTable('sessions')" style="text-align: center;">
                                    Sessions <span class="sort-icon">${sortCol == 'sessions' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th class="sortable-th ${sortCol == 'present' ? 'active-sort' : ''}" onclick="sortTable('present')" style="text-align: center; color: #059669;">
                                    Present <span class="sort-icon">${sortCol == 'present' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th class="sortable-th ${sortCol == 'late' ? 'active-sort' : ''}" onclick="sortTable('late')" style="text-align: center; color: #D97706;">
                                    Late <span class="sort-icon">${sortCol == 'late' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th class="sortable-th ${sortCol == 'absent' ? 'active-sort' : ''}" onclick="sortTable('absent')" style="text-align: center; color: #DC2626;">
                                    Absent <span class="sort-icon">${sortCol == 'absent' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th style="text-align: center; color: #2563EB;">Excused</th>
                                <th class="sortable-th ${sortCol == 'percentage' ? 'active-sort' : ''}" onclick="sortTable('percentage')" style="text-align: center;">
                                    Overall % <span class="sort-icon">${sortCol == 'percentage' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th class="sortable-th ${sortCol == 'lastdate' ? 'active-sort' : ''}" onclick="sortTable('lastdate')">
                                    Last Record <span class="sort-icon">${sortCol == 'lastdate' && sortDir == 'asc' ? '&#x25B2;' : '&#x25BC;'}</span>
                                </th>
                                <th>Status Tier</th>
                                <th style="text-align: right;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty studentSummaries}">
                                    <tr>
                                        <td colspan="12" style="text-align: center; padding: 3rem; color: var(--text-muted);">
                                            No enrolled students found matching the selected class, batch, or filter criteria.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="st" items="${studentSummaries}">
                                        <tr class="student-row">
                                            <td>
                                                <div class="flex items-center gap-8">
                                                    <div class="step-icon-circle bg-blue-light text-blue" style="width: 32px; height: 32px; font-weight: 700; font-size: 0.8rem; cursor: pointer;"
                                                         onclick="openStudentDrawer(${st.studentId})">
                                                        ${st.name != null ? st.name.substring(0, 1) : 'S'}
                                                    </div>
                                                    <div>
                                                        <a href="javascript:void(0)" onclick="openStudentDrawer(${st.studentId})" class="font-bold text-dark hover-blue" style="text-decoration: none;">
                                                            ${st.name}
                                                        </a>
                                                        <c:if test="${st.overriddenCount > 0}">
                                                            <span class="badge-override-pill" title="${st.overriddenCount} manual override(s) recorded">&#x270E; ${st.overriddenCount} Override</span>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <span class="font-mono text-muted font-semibold student-roll-val">${st.rollNo}</span>
                                            </td>
                                            <td>
                                                <span class="status-pill pill-neutral text-xs">${st.classCode} &bull; ${st.batchCode != null ? st.batchCode : 'General'}</span>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="font-mono font-bold">${st.totalSessions}</span>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="font-mono font-bold text-green">${st.presentCount}</span>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="font-mono font-bold" style="color: #D97706;">${st.lateCount}</span>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="font-mono font-bold text-red">${st.absentCount}</span>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="font-mono text-muted">${st.excusedCount}</span>
                                            </td>
                                            <td style="text-align: center;">
                                                <div style="display: flex; flex-direction: column; align-items: center;">
                                                    <span class="font-mono font-bold text-dark" style="font-size: 0.95rem;">
                                                        ${st.formattedPercentage}
                                                    </span>
                                                    <div class="attendance-progress-wrap">
                                                        <div class="attendance-progress-bar ${st.performanceTier == 'SAFE' ? 'progress-safe' : (st.performanceTier == 'WARNING' ? 'progress-warning' : 'progress-critical')}"
                                                             style="width: ${st.attendancePercentage}%;"></div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <div style="font-size: 0.78rem;">
                                                    <div class="font-mono text-dark">${st.lastDate}</div>
                                                    <div>
                                                        <c:choose>
                                                            <c:when test="${st.lastStatus == 'PRESENT'}"><span class="badge-status badge-present" style="font-size:0.65rem;padding:0.1rem 0.4rem;">Present</span></c:when>
                                                            <c:when test="${st.lastStatus == 'LATE'}"><span class="badge-status badge-late" style="font-size:0.65rem;padding:0.1rem 0.4rem;">Late</span></c:when>
                                                            <c:when test="${st.lastStatus == 'EXCUSED' || st.lastStatus == 'OD'}"><span class="badge-status badge-excused" style="font-size:0.65rem;padding:0.1rem 0.4rem;">Excused</span></c:when>
                                                            <c:when test="${st.lastStatus == 'NOT_RECORDED'}"><span class="text-xs text-muted">Not Marked</span></c:when>
                                                            <c:otherwise><span class="badge-status badge-absent" style="font-size:0.65rem;padding:0.1rem 0.4rem;">Absent</span></c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${st.performanceTier == 'SAFE'}">
                                                        <span class="tier-badge tier-badge-safe">&#x2713; Safe</span>
                                                    </c:when>
                                                    <c:when test="${st.performanceTier == 'WARNING'}">
                                                        <span class="tier-badge tier-badge-warning">&#x26A0; Warning</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="tier-badge tier-badge-critical">&#x2717; Critical</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="text-align: right;">
                                                <div class="flex gap-4" style="justify-content: flex-end;">
                                                    <button type="button" class="btn-secondary-action btn-sm-action"
                                                            onclick="openStudentDrawer(${st.studentId})" title="View complete daily session history">
                                                        View Details
                                                    </button>
                                                    <button type="button" class="btn-primary-action btn-sm-action"
                                                            onclick="openOverrideModal('${st.studentId}', '${st.name} (${st.rollNo})', '${endDate}', '${st.lastStatus}')" title="Manual authorized override">
                                                        Override
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- =================================================================== -->
        <!-- TAB 2: CLASS VISUAL ANALYTICS & TRENDS -->
        <!-- =================================================================== -->
        <div id="tabContent-analytics" style="display: ${activeTab == 'analytics' ? 'block' : 'none'};">
            <div class="analytics-grid-2">
                <!-- Daily Attendance Rate Trend Chart (SVG Line & Area) -->
                <div class="chart-card">
                    <div class="flex-between" style="margin-bottom: 0.5rem;">
                        <div>
                            <h3 class="chart-title">Daily Attendance Rate Trend Over Time</h3>
                            <p class="chart-subtitle">Attendance percentage across all conducted dates in reporting period (${startDate} to ${endDate})</p>
                        </div>
                        <span class="status-pill pill-neutral font-bold">${analytics.dailyTrendsCount} Conducted Dates</span>
                    </div>

                    <!-- Pure HTML5 SVG Responsive Trend Chart -->
                    <div class="trend-chart-container" style="background: #F8FAFC; border-radius: 8px; border: 1px solid #E2E8F0; padding: 10px;">
                        <c:choose>
                            <c:when test="${not empty analytics.dailyTrends}">
                                <svg viewBox="0 0 700 200" preserveAspectRatio="none" class="svg-trend-chart">
                                    <!-- Grid lines at 100%, 75% threshold, 50%, 25% -->
                                    <line x1="40" y1="20" x2="680" y2="20" stroke="#E2E8F0" stroke-width="1" stroke-dasharray="4" />
                                    <text x="5" y="24" font-size="10" fill="#94A3B8">100%</text>

                                    <!-- 75% Threshold Line -->
                                    <line x1="40" y1="57.5" x2="680" y2="57.5" stroke="#F59E0B" stroke-width="1.5" stroke-dasharray="3" />
                                    <text x="5" y="61.5" font-size="10" fill="#D97706" font-weight="700">75% (Min)</text>

                                    <line x1="40" y1="95" x2="680" y2="95" stroke="#E2E8F0" stroke-width="1" stroke-dasharray="4" />
                                    <text x="5" y="99" font-size="10" fill="#94A3B8">50%</text>

                                    <line x1="40" y1="132.5" x2="680" y2="132.5" stroke="#E2E8F0" stroke-width="1" stroke-dasharray="4" />
                                    <text x="5" y="136.5" font-size="10" fill="#94A3B8">25%</text>

                                    <!-- Baseline -->
                                    <line x1="40" y1="170" x2="680" y2="170" stroke="#CBD5E1" stroke-width="1.5" />
                                    <text x="5" y="174" font-size="10" fill="#94A3B8">0%</text>

                                    <!-- Render Trend Points & Bars -->
                                    <c:forEach var="d" items="${analytics.dailyTrends}" varStatus="loop">
                                        <c:set var="xPos" value="${50 + (loop.index * 60)}" />
                                        <c:set var="barHeight" value="${d.attendancePercentage * 1.4}" />
                                        <c:set var="yPos" value="${170.0 - barHeight}" />

                                        <!-- Vertical bar representing daily percentage -->
                                        <rect x="${xPos - 12}" y="${yPos}" width="24" height="${barHeight}"
                                              rx="4" fill="${d.attendancePercentage >= 75.0 ? '#10B981' : (d.attendancePercentage >= 60.0 ? '#F59E0B' : '#EF4444')}"
                                              opacity="0.85">
                                            <title>${d.date}: ${d.attendancePercentage}% (Present: ${d.presentCount}, Late: ${d.lateCount}, Absent: ${d.absentCount})</title>
                                        </rect>

                                        <!-- Value label on top of bar -->
                                        <text x="${xPos}" y="${yPos - 5}" font-size="10" font-weight="700"
                                              fill="#1E293B" text-anchor="middle">${d.attendancePercentage}%</text>

                                        <!-- Date label underneath -->
                                        <text x="${xPos}" y="188" font-size="9" font-weight="600"
                                              fill="#64748B" text-anchor="middle">
                                            ${d.date.length() > 5 ? d.date.substring(5) : d.date}
                                        </text>
                                    </c:forEach>
                                </svg>
                            </c:when>
                            <c:otherwise>
                                <div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--text-muted);">
                                    No daily session trend data available for this range.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Status Distribution Breakdown -->
                <div class="chart-card">
                    <h3 class="chart-title">Attendance Distribution</h3>
                    <p class="chart-subtitle">Cumulative breakdown of all recorded student attendances</p>

                    <div style="display: flex; flex-direction: column; gap: 0.85rem; margin-top: 1rem;">
                        <div>
                            <div class="flex-between text-xs" style="margin-bottom: 4px;">
                                <span class="font-bold text-green">&#x25CF; Present (${analytics.totalPresent})</span>
                                <span class="font-mono font-bold">${analytics.presentRate}%</span>
                            </div>
                            <div class="attendance-progress-wrap" style="max-width: 100%; height: 8px;">
                                <div class="attendance-progress-bar progress-safe"
                                     style="width: ${analytics.presentRate}%;"></div>
                            </div>
                        </div>

                        <div>
                            <div class="flex-between text-xs" style="margin-bottom: 4px;">
                                <span class="font-bold" style="color: #D97706;">&#x25CF; Late Arrivals (${analytics.totalLate})</span>
                                <span class="font-mono font-bold">${analytics.lateRate}%</span>
                            </div>
                            <div class="attendance-progress-wrap" style="max-width: 100%; height: 8px;">
                                <div class="attendance-progress-bar progress-warning"
                                     style="width: ${analytics.lateRate}%;"></div>
                            </div>
                        </div>

                        <div>
                            <div class="flex-between text-xs" style="margin-bottom: 4px;">
                                <span class="font-bold text-red">&#x25CF; Absent (${analytics.totalAbsent})</span>
                                <span class="font-mono font-bold">${analytics.absentRate}%</span>
                            </div>
                            <div class="attendance-progress-wrap" style="max-width: 100%; height: 8px;">
                                <div class="attendance-progress-bar progress-critical"
                                     style="width: ${analytics.absentRate}%;"></div>
                            </div>
                        </div>

                        <div>
                            <div class="flex-between text-xs" style="margin-bottom: 4px;">
                                <span class="font-bold text-blue">&#x25CF; Excused / Medical (${analytics.totalExcused})</span>
                                <span class="font-mono font-bold">${analytics.excusedRate}%</span>
                            </div>
                            <div class="attendance-progress-wrap" style="max-width: 100%; height: 8px;">
                                <div class="attendance-progress-bar" style="background: #3B82F6; width: ${analytics.excusedRate}%;"></div>
                            </div>
                        </div>
                    </div>

                    <div style="margin-top: 1.25rem; padding-top: 1rem; border-top: 1px solid var(--border-color);">
                        <div class="text-xs font-bold text-muted" style="margin-bottom: 6px;">Student Performance Tiers:</div>
                        <div class="flex gap-6" style="flex-wrap: wrap;">
                            <span class="tier-badge tier-badge-safe">&#x2713; Safe (&ge;75%): ${analytics.safeCount}</span>
                            <span class="tier-badge tier-badge-warning">&#x26A0; Warning (60-74%): ${analytics.warningCount}</span>
                            <span class="tier-badge tier-badge-critical">&#x2717; Critical (&lt;60%): ${analytics.criticalCount}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- List of Students At Risk / Shortage (<75%) -->
            <div class="portal-table-container">
                <div class="table-header-bar flex-between">
                    <div>
                        <h3 class="table-title text-red" style="color: var(--danger-red-start);">
                            &#x26A0; Students Below Institutional Threshold (&lt; 75% Attendance)
                        </h3>
                        <p class="table-subtitle">These students are currently ineligible for exams without mandatory condonation or makeup classes.</p>
                    </div>
                    <span class="tier-badge tier-badge-critical">${analytics.atRiskCount} Students In Danger</span>
                </div>

                <div class="portal-table-responsive">
                    <table class="portal-data-table">
                        <thead>
                            <tr>
                                <th>Student</th>
                                <th>Roll Number</th>
                                <th>Class & Batch</th>
                                <th style="text-align: center;">Conducted</th>
                                <th style="text-align: center;">Attended</th>
                                <th style="text-align: center;">Absent</th>
                                <th style="text-align: center;">Current Rate</th>
                                <th>Required to Reach 75%</th>
                                <th style="text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty analytics.atRiskStudents}">
                                    <tr>
                                        <td colspan="9" style="text-align: center; padding: 2.5rem; color: #059669; font-weight: 700;">
                                            &#x2713; Outstanding! All enrolled students currently satisfy or exceed the 75% institutional attendance threshold.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="risk" items="${analytics.atRiskStudents}">
                                        <tr>
                                            <td>
                                                <div class="flex items-center gap-8">
                                                    <div class="step-icon-circle bg-red-light text-red" style="width: 30px; height: 30px; font-weight: 700;">
                                                        ${risk.name.substring(0, 1)}
                                                    </div>
                                                    <span class="font-bold text-dark">${risk.name}</span>
                                                </div>
                                            </td>
                                            <td><span class="font-mono font-semibold">${risk.rollNo}</span></td>
                                            <td><span class="status-pill pill-neutral text-xs">${risk.classCode} &bull; ${risk.batchCode}</span></td>
                                            <td style="text-align: center;"><span class="font-mono font-bold">${risk.totalSessions}</span></td>
                                            <td style="text-align: center;"><span class="font-mono font-bold text-green">${risk.presentCount + risk.lateCount}</span></td>
                                            <td style="text-align: center;"><span class="font-mono font-bold text-red">${risk.absentCount}</span></td>
                                            <td style="text-align: center;">
                                                <span class="font-mono font-bold text-red" style="font-size: 1rem;">${risk.formattedPercentage}</span>
                                            </td>
                                            <td>
                                                <span class="text-xs text-muted">
                                                    Needs consecutive attendance in upcoming sessions to recover eligibility.
                                                </span>
                                            </td>
                                            <td style="text-align: right;">
                                                <button type="button" class="btn-primary-action btn-sm-action"
                                                        onclick="openStudentDrawer(${risk.studentId})">
                                                    Inspect History
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- =================================================================== -->
        <!-- TAB 3: ATTENDANCE CALENDAR & DAILY RECORDS -->
        <!-- =================================================================== -->
        <div id="tabContent-daily" style="display: ${activeTab == 'daily' ? 'block' : 'none'};">
            <!-- Daily Date Selector Bar -->
            <div class="card mb-16" style="padding: 1rem 1.25rem;">
                <form action="${pageContext.request.contextPath}/faculty/dashboard" method="get" class="flex-between" style="flex-wrap: wrap; gap: 1rem;">
                    <input type="hidden" name="tab" value="daily">
                    <input type="hidden" name="classId" value="${selectedClassId}">
                    <input type="hidden" name="batchId" value="${selectedBatchId}">
                    <input type="hidden" name="startDate" value="${startDate}">
                    <input type="hidden" name="endDate" value="${endDate}">

                    <div class="flex items-center gap-8">
                        <label for="dailyDateInput" class="font-bold text-sm">Select Attendance Date:</label>
                        <input type="date" name="dailyDate" id="dailyDateInput" class="filter-control" value="${selectedDailyDate}" onchange="this.form.submit()">
                        <button type="submit" class="btn-primary-action btn-sm-action">Load Date</button>
                    </div>

                    <!-- Daily Metric Stat Pills -->
                    <div class="flex gap-6 items-center" style="flex-wrap: wrap;">
                        <span class="status-pill pill-neutral font-mono font-bold">Enrolled: ${dailyTotal}</span>
                        <span class="status-pill stat-present font-mono font-bold">Present: ${dailyPresent}</span>
                        <span class="status-pill font-mono font-bold" style="background: #FFFBEB; color: #D97706; border: 1px solid #FDE68A;">Late: ${dailyLate}</span>
                        <span class="status-pill stat-absent font-mono font-bold">Absent: ${dailyAbsent}</span>
                        <span class="status-pill font-mono font-bold" style="background: #EFF6FF; color: #2563EB; border: 1px solid #BFDBFE;">Excused: ${dailyExcused}</span>
                    </div>
                </form>
            </div>

            <!-- Daily Roster Table -->
            <div class="portal-table-container">
                <div class="table-header-bar flex-between">
                    <div>
                        <h3 class="table-title">Daily Attendance Records & Check-in Verification</h3>
                        <p class="table-subtitle">Displaying enrolled students and check-in statuses for date <strong>${selectedDailyDate}</strong></p>
                    </div>
                    <button type="button" class="btn-primary-action btn-sm-action"
                            onclick="openOverrideModal('', '', '${selectedDailyDate}', 'PRESENT')">
                        + New Attendance Override
                    </button>
                </div>

                <div class="portal-table-responsive">
                    <table class="portal-data-table">
                        <thead>
                            <tr>
                                <th>Student</th>
                                <th>Roll Number</th>
                                <th>Batch</th>
                                <th>Attendance Status</th>
                                <th>Check-In Time</th>
                                <th>Check-Out Time</th>
                                <th>Recording Method & Audit</th>
                                <th style="text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty dailyAttendance}">
                                    <tr>
                                        <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                            No student records found for date ${selectedDailyDate}.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="item" items="${dailyAttendance}">
                                        <tr>
                                            <td>
                                                <div class="flex items-center gap-8">
                                                    <div class="step-icon-circle bg-blue-light text-blue" style="width: 32px; height: 32px; font-weight: 700; font-size: 0.8rem;">
                                                        ${item.studentName != null ? item.studentName.substring(0, 1) : 'S'}
                                                    </div>
                                                    <div>
                                                        <a href="javascript:void(0)" onclick="openStudentDrawer(${item.studentId})" class="font-bold text-dark hover-blue" style="text-decoration:none;">
                                                            ${item.studentName}
                                                        </a>
                                                    </div>
                                                </div>
                                            </td>
                                            <td><span class="font-mono text-muted font-semibold">${item.studentRoll}</span></td>
                                            <td><span class="status-pill pill-neutral text-xs">${item.batchName != null ? item.batchName : 'General'}</span></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${item.status == 'PRESENT'}">
                                                        <span class="badge-status badge-present">Present</span>
                                                    </c:when>
                                                    <c:when test="${item.status == 'LATE'}">
                                                        <span class="badge-status badge-late">Late</span>
                                                    </c:when>
                                                    <c:when test="${item.status == 'EXCUSED' || item.status == 'OD'}">
                                                        <span class="badge-status badge-excused">Excused</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-status badge-absent">Absent</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><span class="font-mono text-xs font-semibold">${item.formattedCheckInTime}</span></td>
                                            <td><span class="font-mono text-xs font-semibold">${item.formattedCheckOutTime}</span></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${item.override}">
                                                        <div class="flex flex-col gap-2">
                                                            <span class="badge-override-pill">&#x270E; Faculty Override</span>
                                                            <span class="text-xs text-muted" style="font-size: 0.7rem;">Reason: ${item.overrideReason}</span>
                                                            <c:if test="${not empty item.overriddenBy}">
                                                                <span class="text-xs text-muted" style="font-size: 0.68rem;">By: ${item.overriddenBy}</span>
                                                            </c:if>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-xs text-muted">&mdash; Automatic Mobile Check-in</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="text-align: right;">
                                                <button type="button" class="btn-primary-action btn-sm-action"
                                                        onclick="openOverrideModal('${item.studentId}', '${item.studentName} (${item.studentRoll})', '${selectedDailyDate}', '${item.status}')">
                                                    Override
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- =================================================================== -->
        <!-- TAB 4: ATTENDANCE REPORTS & OVERRIDES LOG -->
        <!-- =================================================================== -->
        <div id="tabContent-reports" style="display: ${activeTab == 'reports' ? 'block' : 'none'};">
            <div class="card mb-16 no-print" style="padding: 1.25rem 1.5rem;">
                <div class="flex-between" style="flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <h3 class="font-bold text-lg text-dark">Institutional Attendance Reports & CSV Export</h3>
                        <p class="text-xs text-muted">Generate certified attendance records for official department reporting and semester examinations.</p>
                    </div>

                    <div class="flex gap-8 items-center" style="flex-wrap: wrap;">
                        <a href="${pageContext.request.contextPath}/faculty/export?classId=${selectedClassId}&batchId=${selectedBatchId}&startDate=${startDate}&endDate=${endDate}&type=summary"
                           class="btn-primary-action" title="Download summary report as CSV">
                            &#x2193; Export Student Summary CSV
                        </a>
                        <a href="${pageContext.request.contextPath}/faculty/export?classId=${selectedClassId}&batchId=${selectedBatchId}&startDate=${startDate}&endDate=${endDate}&type=detailed"
                           class="btn-secondary-action" title="Download granular session-by-session records">
                            &#x2193; Export Detailed Sessions CSV
                        </a>
                        <button type="button" class="btn-secondary-action" onclick="window.print()" title="Print readable report">
                            &#x1F5B6; Print Official Report
                        </button>
                    </div>
                </div>
            </div>

            <!-- Recent Override Audit History -->
            <div class="portal-table-container">
                <div class="table-header-bar flex-between">
                    <div>
                        <h3 class="table-title">&#x1F4DC; Immutable Faculty Attendance Override Audit Log</h3>
                        <p class="table-subtitle">Permanent institutional log of all manual attendance status corrections and justifications.</p>
                    </div>
                    <span class="status-pill pill-neutral text-xs">Recent Records</span>
                </div>

                <div class="portal-table-responsive">
                    <table class="portal-data-table">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Attendance Date</th>
                                <th>Student</th>
                                <th>Roll Number</th>
                                <th>Original Status</th>
                                <th>Updated Status</th>
                                <th>Mandatory Reason</th>
                                <th>Authorized By</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty recentOverrides}">
                                    <tr>
                                        <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                            No attendance overrides recorded yet.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="ov" items="${recentOverrides}">
                                        <tr>
                                            <td><span class="font-mono text-xs text-muted">${ov.overrideTime}</span></td>
                                            <td><span class="font-mono font-bold">${ov.attendanceDate}</span></td>
                                            <td><span class="font-bold text-dark">${ov.studentName}</span></td>
                                            <td><span class="font-mono text-muted">${ov.studentRoll}</span></td>
                                            <td>
                                                <span class="badge-status badge-absent" style="font-size:0.68rem;">${ov.originalStatus}</span>
                                            </td>
                                            <td>
                                                <span class="badge-status badge-present" style="font-size:0.68rem;">${ov.updatedStatus}</span>
                                            </td>
                                            <td>
                                                <span class="text-xs text-dark" style="max-width: 250px; display: inline-block;">
                                                    ${ov.reason}
                                                </span>
                                            </td>
                                            <td>
                                                <span class="status-pill pill-neutral text-xs">
                                                    ${ov.facultyName} (${ov.facultyCode})
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- =================================================================== -->
        <!-- TAB 5: STUDENT RANDOM LOCATION VERIFICATION & GEOFENCE REVIEW -->
        <!-- =================================================================== -->
        <div id="tabContent-location" style="display: ${activeTab == 'location' ? 'block' : 'none'};">
            <!-- Header Bar -->
            <div class="card mb-16" style="background: linear-gradient(135deg, #0F172A 0%, #1E293B 100%); color: #FFFFFF; padding: 1.25rem 1.5rem; border-radius: 12px;">
                <div class="flex-between" style="flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <div class="flex items-center gap-8 mb-4">
                            <span style="font-size: 1.25rem;">🛰️</span>
                            <h3 class="font-bold text-lg text-white" style="margin: 0;">Student Random Location Verification &amp; Geofence Review</h3>
                        </div>
                        <p class="text-xs text-muted" style="color: #94A3B8; margin: 0;">
                            4&ndash;5 server-scheduled GPS verification events per student per day (9:05 AM &ndash; 3:40 PM, excluding 12:20&ndash;1:10 PM lunch).
                            Review outside-campus detections to verify if student had an authorized reason (e.g. medical, official duty) or confirm absence.
                        </p>
                    </div>
                    <div class="flex items-center gap-8">
                        <span class="status-pill" style="background: rgba(56, 189, 248, 0.2); color: #38BDF8; font-weight: 700;">
                            Jaipur Campus Geofence: 1,000m
                        </span>
                    </div>
                </div>
            </div>

            <!-- Telemetry Metrics Grid -->
            <div class="stats-grid-4 mb-16" style="grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));">
                <div class="card p-12" style="margin: 0; background: #FFFFFF; border: 1px solid #CBD5E1;">
                    <span class="text-xs text-muted font-bold" style="text-transform: uppercase;">Total Monitored</span>
                    <div style="font-size: 1.5rem; font-weight: 800; color: #0F172A; margin-top: 2px;">${locTotalChecks}</div>
                    <span class="text-xs text-muted">Checks for your students</span>
                </div>
                <div class="card p-12" style="margin: 0; background: #F0FDF4; border: 1px solid #86EFAC;">
                    <span class="text-xs font-bold" style="color: #166534; text-transform: uppercase;">Verified On Campus</span>
                    <div style="font-size: 1.5rem; font-weight: 800; color: #15803D; margin-top: 2px;">${locVerifiedCampus}</div>
                    <span class="text-xs text-muted">Within 1,000m geofence</span>
                </div>
                <div class="card p-12" style="margin: 0; background: ${locOutsideGeofence > 0 ? '#FEF2F2' : '#F8FAFC'}; border: 1px solid ${locOutsideGeofence > 0 ? '#F87171' : '#CBD5E1'};">
                    <span class="text-xs font-bold" style="color: ${locOutsideGeofence > 0 ? '#DC2626' : '#64748B'}; text-transform: uppercase;">Outside Geofence</span>
                    <div style="font-size: 1.5rem; font-weight: 800; color: ${locOutsideGeofence > 0 ? '#DC2626' : '#64748B'}; margin-top: 2px;">${locOutsideGeofence}</div>
                    <span class="text-xs" style="color: ${locOutsideGeofence > 0 ? '#B91C1C' : '#94A3B8'}; font-weight: ${locOutsideGeofence > 0 ? '700' : '400'};">
                        ${locOutsideGeofence > 0 ? 'Requires Faculty Action' : 'All clear on campus'}
                    </span>
                </div>
                <div class="card p-12" style="margin: 0; background: #FFFBEB; border: 1px solid #FDE68A;">
                    <span class="text-xs font-bold" style="color: #92400E; text-transform: uppercase;">GPS Inaccurate / Offline</span>
                    <div style="font-size: 1.5rem; font-weight: 800; color: #D97706; margin-top: 2px;">${locGpsFailures}</div>
                    <span class="text-xs text-muted">No penalty applied (Rule 11)</span>
                </div>
            </div>

            <!-- Location Records Table -->
            <div class="portal-table-container">
                <div class="table-header-bar flex-between" style="flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <h4 class="table-title">Live Student Random Location Verification Telemetry</h4>
                        <p class="table-subtitle">Fresh GPS coordinates collected autonomously during active student sessions</p>
                    </div>
                    <span class="status-pill pill-neutral font-mono text-xs">Total Records: ${facultyLocationChecks.size()}</span>
                </div>

                <div class="portal-table-responsive">
                    <table class="portal-data-table">
                        <thead>
                            <tr>
                                <th>Student</th>
                                <th>Roll Number</th>
                                <th>Class &amp; Section</th>
                                <th>Scheduled Slot</th>
                                <th>Check Time</th>
                                <th>Coordinates &amp; Accuracy</th>
                                <th>Campus Distance</th>
                                <th>Verification Result</th>
                                <th style="text-align: right;">Faculty Review Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty facultyLocationChecks}">
                                    <c:forEach var="chk" items="${facultyLocationChecks}">
                                        <tr style="${chk.pendingFacultyReview ? 'background: #FEF2F2;' : ''}">
                                            <td>
                                                <strong class="text-dark">${chk.studentName}</strong>
                                            </td>
                                            <td>
                                                <span class="font-mono text-xs font-semibold text-muted">${chk.studentRollNo}</span>
                                            </td>
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
                                                            ⚠️ OUTSIDE CAMPUS
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
                                                        <span class="badge-status pill-neutral">CANCELLED</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-status" style="background: #E0F2FE; color: #0369A1; font-weight: 700;">
                                                            ⏳ SCHEDULED
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="text-align: right;">
                                                <c:choose>
                                                    <c:when test="${chk.facultyReviewStatus == 'APPROVED_VALID_REASON'}">
                                                        <span class="badge-status badge-present" style="font-size: 0.72rem;">✓ Verified Present (True Reason)</span>
                                                        <c:if test="${not empty chk.facultyReviewNotes}">
                                                            <div class="text-xs text-muted" style="margin-top: 2px;">Note: ${chk.facultyReviewNotes}</div>
                                                        </c:if>
                                                    </c:when>
                                                    <c:when test="${chk.facultyReviewStatus == 'REJECTED_ABSENT'}">
                                                        <span class="badge-status badge-absent" style="font-size: 0.72rem;">✕ Confirmed Absent (Unauthorized)</span>
                                                        <c:if test="${not empty chk.facultyReviewNotes}">
                                                            <div class="text-xs text-muted" style="margin-top: 2px;">Note: ${chk.facultyReviewNotes}</div>
                                                        </c:if>
                                                    </c:when>
                                                    <c:when test="${chk.pendingFacultyReview}">
                                                        <button type="button" class="btn-primary-action btn-sm-action" style="background: #DC2626; color: #FFFFFF; font-weight: 800; border-radius: 6px; padding: 6px 12px; border: none; cursor: pointer;"
                                                                onclick="openLocationReviewModal('${chk.id}', '${chk.studentName}', '${chk.studentRollNo}', '${Math.round(chk.distanceMeters)}', '${chk.formattedScheduledTime}', '${chk.lat}, ${chk.lng}')">
                                                            ⚠️ Verify Attendance
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-xs text-muted">No review needed</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="9" class="text-center py-24 text-muted text-xs">
                                            No random location verification checks found for this date.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>

    <!-- =================================================================== -->
    <!-- 4. SLIDE-OVER INDIVIDUAL STUDENT ATTENDANCE DETAILS DRAWER -->
    <!-- =================================================================== -->
    <div class="drawer-overlay" id="studentDetailDrawer" onclick="closeDrawerOnOutside(event)">
        <div class="drawer-container">
            <div class="drawer-header">
                <div>
                    <h3 class="font-bold text-lg text-dark" id="drawerStudentName">Loading Student Profile...</h3>
                    <p class="text-xs text-muted" id="drawerStudentRoll">Roll Number: &mdash;</p>
                </div>
                <button type="button" onclick="closeStudentDrawer()" style="background:none;border:none;font-size:1.6rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>

            <div class="drawer-body">
                <!-- Student Profile Banner -->
                <div class="drawer-profile-card">
                    <div class="flex-between">
                        <div>
                            <span class="status-pill" style="background: rgba(37, 99, 235, 0.4); color: #93C5FD; margin-bottom: 4px;">
                                Enrolled Student &bull; Extremis Academic Portal
                            </span>
                            <h3 class="font-bold text-xl text-white" id="drawerCardName">&mdash;</h3>
                            <p class="text-xs" style="color: #94A3B8;" id="drawerCardDept">&mdash;</p>
                        </div>
                        <div style="text-align: right;">
                            <div class="text-xs text-muted" style="color: #94A3B8;">Attendance Rate</div>
                            <div class="font-bold font-mono" style="font-size: 1.8rem; color: #FFFFFF;" id="drawerCardPct">0.0%</div>
                            <div id="drawerCardTier"></div>
                        </div>
                    </div>
                </div>

                <!-- Aggregated Attendance Statistics Pills -->
                <div class="drawer-stats-row">
                    <div class="drawer-stat-pill">
                        <div class="drawer-stat-pill-val" id="drawerTotalSessions">0</div>
                        <div class="drawer-stat-pill-lbl">Conducted</div>
                    </div>
                    <div class="drawer-stat-pill">
                        <div class="drawer-stat-pill-val text-green" id="drawerPresentCount">0</div>
                        <div class="drawer-stat-pill-lbl">Present</div>
                    </div>
                    <div class="drawer-stat-pill">
                        <div class="drawer-stat-pill-val" style="color:#D97706;" id="drawerLateCount">0</div>
                        <div class="drawer-stat-pill-lbl">Late</div>
                    </div>
                    <div class="drawer-stat-pill">
                        <div class="drawer-stat-pill-val text-red" id="drawerAbsentCount">0</div>
                        <div class="drawer-stat-pill-lbl">Absent</div>
                    </div>
                    <div class="drawer-stat-pill">
                        <div class="drawer-stat-pill-val text-blue" id="drawerExcusedCount">0</div>
                        <div class="drawer-stat-pill-lbl">Excused</div>
                    </div>
                </div>

                <!-- Complete Date-Wise Attendance History -->
                <div class="portal-table-container" style="border: 1px solid var(--border-color); box-shadow: none;">
                    <div class="table-header-bar flex-between" style="padding: 0.75rem 1rem;">
                        <div>
                            <h4 class="font-bold text-sm text-dark">Date-Wise Session Attendance History</h4>
                            <p class="text-xs text-muted">Complete records for conducted academic classes</p>
                        </div>
                        <button type="button" class="btn-secondary-action btn-sm-action" onclick="toggleDrawerSort()">
                            <span id="drawerSortLabel">Sort: Newest First</span>
                        </button>
                    </div>

                    <div style="max-height: 380px; overflow-y: auto;">
                        <table class="history-table">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Subject / Session</th>
                                    <th>Status</th>
                                    <th>Time</th>
                                    <th>Method</th>
                                    <th style="text-align: right;">Action</th>
                                </tr>
                            </thead>
                            <tbody id="drawerSessionsBody">
                                <tr>
                                    <td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">
                                        Loading session records from database...
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- =================================================================== -->
    <!-- 8. ATTENDANCE OVERRIDE MODAL -->
    <!-- =================================================================== -->
    <div class="modal-overlay" id="overrideModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Faculty Attendance Override</h3>
                <button type="button" onclick="closeOverrideModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/faculty/override" method="post" id="overrideForm" onsubmit="return validateOverrideForm()">
                <div class="modal-body">
                    <input type="hidden" name="classId" value="${selectedClassId}">
                    <input type="hidden" name="batchId" value="${selectedBatchId}">
                    <input type="hidden" name="startDate" value="${startDate}">
                    <input type="hidden" name="endDate" value="${endDate}">
                    <input type="hidden" name="tab" value="${activeTab}">
                    <input type="hidden" name="dailyDate" value="${selectedDailyDate}">

                    <div class="filter-field">
                        <label for="modalStudentId">Student</label>
                        <select name="studentId" id="modalStudentId" class="filter-control" required>
                            <c:forEach var="st" items="${studentSummaries}">
                                <option value="${st.studentId}">${st.name} (${st.rollNo}) - Current: ${st.lastStatus}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-field">
                        <label for="modalDate">Session Attendance Date <span style="color:var(--danger-red-start);font-weight:700;">*</span></label>
                        <input type="date" name="date" id="modalDate" class="filter-control" value="${selectedDailyDate}" required>
                    </div>

                    <div class="filter-field">
                        <label for="modalStatus">Updated Attendance Status <span style="color:var(--danger-red-start);font-weight:700;">*</span></label>
                        <select name="status" id="modalStatus" class="filter-control" required>
                            <option value="PRESENT">Present</option>
                            <option value="LATE">Late Arrival</option>
                            <option value="EXCUSED">Excused / On-Duty</option>
                            <option value="ABSENT">Absent</option>
                        </select>
                    </div>

                    <div class="filter-field">
                        <label for="modalReason">Mandatory Reason for Override <span style="color:var(--danger-red-start);font-weight:700;">*</span></label>
                        <textarea name="reason" id="modalReason" class="filter-control" rows="3" placeholder="Enter legitimate explanation (e.g., Late arrival with traffic pass, medical excuse verified, verified physical attendance)..." required></textarea>
                    </div>

                    <div style="background: #F1F5F9; border-radius: 8px; padding: 0.75rem; border: 1px solid #E2E8F0;">
                        <label style="display: flex; align-items: flex-start; gap: 0.5rem; font-size: 0.78rem; color: var(--text-main); cursor: pointer;">
                            <input type="checkbox" id="modalConfirm" required style="margin-top: 2px;">
                            <span><strong>Confirmation:</strong> I verify that I am authorized to override attendance for this student and this update complies with academic attendance policies. This action is permanently logged in the audit trail.</span>
                        </label>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeOverrideModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action">Confirm & Save Override</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Faculty Location Review & Verification Modal -->
    <div class="modal-overlay" id="locationReviewModal" style="display: none;">
        <div class="modal-box">
            <div class="modal-header">
                <div>
                    <h3 class="modal-title" style="color: #991B1B;">⚠️ Verify Student Outside-Campus Attendance</h3>
                    <p class="modal-subtitle">Review location evidence to determine if the student had an authorized reason</p>
                </div>
                <button type="button" onclick="closeLocationReviewModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/faculty/dashboard" method="post" onsubmit="return validateLocationReviewForm()">
                <input type="hidden" name="action" value="verify_location">
                <input type="hidden" name="checkId" id="locReviewCheckId" value="">

                <div class="modal-body">
                    <!-- Student & Location Evidence Info Box -->
                    <div style="background: #FEF2F2; border: 1.5px solid #FCA5A5; border-radius: 8px; padding: 12px; margin-bottom: 1rem;">
                        <div class="flex-between mb-4">
                            <span class="text-xs font-bold" style="color: #991B1B;">STUDENT:</span>
                            <span class="font-bold text-dark text-sm" id="locReviewStudentName">&mdash;</span>
                        </div>
                        <div class="flex-between mb-4">
                            <span class="text-xs font-bold" style="color: #991B1B;">ROLL NUMBER:</span>
                            <span class="font-mono text-xs font-bold text-dark" id="locReviewStudentRoll">&mdash;</span>
                        </div>
                        <div class="flex-between mb-4">
                            <span class="text-xs font-bold" style="color: #991B1B;">DISTANCE FROM CAMPUS:</span>
                            <span class="font-mono text-xs font-bold text-red" id="locReviewDistance">&mdash;</span>
                        </div>
                        <div class="flex-between">
                            <span class="text-xs font-bold" style="color: #991B1B;">CHECK SLOT:</span>
                            <span class="font-mono text-xs font-bold text-dark" id="locReviewSlot">&mdash;</span>
                        </div>
                    </div>

                    <!-- Decision Selector -->
                    <div class="form-group mb-12">
                        <label class="form-label font-bold text-xs" style="color: #0F172A;">Faculty Verification Decision <span class="text-red">*</span></label>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <label style="display: flex; align-items: flex-start; gap: 8px; padding: 10px; border: 1.5px solid #86EFAC; background: #F0FDF4; border-radius: 8px; cursor: pointer;">
                                <input type="radio" name="decision" value="APPROVE_PRESENT" required style="margin-top: 3px;" checked>
                                <div>
                                    <strong style="color: #15803D; font-size: 0.88rem;">Valid / True Reason &mdash; Mark PRESENT</strong>
                                    <p class="text-xs text-muted" style="margin: 2px 0 0 0;">
                                        Student had an authorized reason to be outside campus (e.g. approved college duty, medical emergency, faculty-sanctioned work).
                                    </p>
                                </div>
                            </label>
                            <label style="display: flex; align-items: flex-start; gap: 8px; padding: 10px; border: 1.5px solid #FCA5A5; background: #FEF2F2; border-radius: 8px; cursor: pointer;">
                                <input type="radio" name="decision" value="REJECT_ABSENT" required style="margin-top: 3px;">
                                <div>
                                    <strong style="color: #B91C1C; font-size: 0.88rem;">Unauthorized Absence &mdash; Confirm ABSENT</strong>
                                    <p class="text-xs text-muted" style="margin: 2px 0 0 0;">
                                        Student left the campus geofence without valid permission or justification. Enforces full absence.
                                    </p>
                                </div>
                            </label>
                        </div>
                    </div>

                    <!-- Mandatory Reason Notes -->
                    <div class="form-group mb-12">
                        <label class="form-label font-bold text-xs" for="locReviewNotes">Verification Notes / Justification <span class="text-red">*</span></label>
                        <textarea name="reasonNotes" id="locReviewNotes" class="filter-control" rows="3" placeholder="Enter reason (e.g. 'Approved NSS camp duty verified with coordinator' or 'Left campus during class hours without permission')..." required></textarea>
                    </div>

                    <div style="background: #F1F5F9; border-radius: 8px; padding: 0.75rem; border: 1px solid #E2E8F0;">
                        <label style="display: flex; align-items: flex-start; gap: 0.5rem; font-size: 0.78rem; color: var(--text-main); cursor: pointer;">
                            <input type="checkbox" id="locReviewConfirm" required style="margin-top: 2px;">
                            <span><strong>Faculty Confirmation:</strong> I confirm that I have verified the location alert for this student and this update is final and complies with academic rules.</span>
                        </label>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeLocationReviewModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action" style="background: #0F172A;">Submit Verification Decision</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Interactive Client Scripts -->
    <script>
        let currentDrawerStudentId = null;
        let drawerSortOrder = 'desc';

        function switchTab(tabName) {
            document.querySelectorAll('[id^="tabContent-"]').forEach(el => el.style.display = 'none');
            const target = document.getElementById('tabContent-' + tabName);
            if (target) target.style.display = 'block';

            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.portal-nav-link').forEach(link => link.classList.remove('active'));

            const navLink = document.getElementById('nav-' + tabName);
            if (navLink) navLink.classList.add('active');

            const tabInput = document.getElementById('activeTabInput');
            if (tabInput) tabInput.value = tabName;
        }

        function onRangePresetChange(preset) {
            const startGroup = document.getElementById('startDateGroup');
            const endGroup = document.getElementById('endDateGroup');
            const startInput = document.getElementById('filterStartDate');
            const endInput = document.getElementById('filterEndDate');

            if (preset === 'this_month') {
                startInput.value = '2026-10-01';
                endInput.value = '2026-10-31';
            } else if (preset === 'last_7_days') {
                startInput.value = '2026-10-05';
                endInput.value = '2026-10-12';
            } else if (preset === 'today') {
                startInput.value = '2026-10-12';
                endInput.value = '2026-10-12';
            } else if (preset === 'all_time') {
                startInput.value = '2026-01-01';
                endInput.value = '2026-12-31';
            }
            document.getElementById('dashboardFilterForm').submit();
        }

        function filterStudentTable() {
            const query = document.getElementById('studentTableSearch').value.toLowerCase().trim();
            const rows = document.querySelectorAll('#studentOverviewTable tbody tr.student-row');
            rows.forEach(row => {
                const text = row.innerText.toLowerCase();
                row.style.display = text.includes(query) ? '' : 'none';
            });
        }

        function sortTable(column) {
            const url = new URL(window.location.href);
            const currentSort = url.searchParams.get('sort');
            const currentDir = url.searchParams.get('dir') || 'asc';
            let newDir = 'asc';
            if (currentSort === column && currentDir === 'asc') {
                newDir = 'desc';
            }
            url.searchParams.set('sort', column);
            url.searchParams.set('dir', newDir);
            window.location.href = url.toString();
        }

        // Student Detail Drawer logic
        function openStudentDrawer(studentId) {
            currentDrawerStudentId = studentId;
            const drawer = document.getElementById('studentDetailDrawer');
            drawer.classList.add('active');
            loadStudentDrawerData(studentId, drawerSortOrder);
        }

        function closeStudentDrawer() {
            document.getElementById('studentDetailDrawer').classList.remove('active');
        }

        function closeDrawerOnOutside(e) {
            if (e.target.id === 'studentDetailDrawer') {
                closeStudentDrawer();
            }
        }

        function toggleDrawerSort() {
            drawerSortOrder = (drawerSortOrder === 'desc') ? 'asc' : 'desc';
            document.getElementById('drawerSortLabel').innerText = (drawerSortOrder === 'desc') ? 'Sort: Newest First' : 'Sort: Oldest First';
            if (currentDrawerStudentId) {
                loadStudentDrawerData(currentDrawerStudentId, drawerSortOrder);
            }
        }

        function loadStudentDrawerData(studentId, sortOrder) {
            const url = '${pageContext.request.contextPath}/faculty/dashboard?action=student_history&studentId=' + studentId +
                        '&startDate=${startDate}&endDate=${endDate}&sortOrder=' + sortOrder;

            fetch(url)
                .then(res => res.json())
                .then(data => {
                    if (!data.success) {
                        alert(data.message || 'Error fetching student records');
                        return;
                    }
                    const s = data.student;
                    const sum = data.summary;
                    const sessions = data.sessions;

                    document.getElementById('drawerStudentName').innerText = s.name;
                    document.getElementById('drawerStudentRoll').innerText = 'Roll Number: ' + s.rollNo + ' | ' + s.branch + ' ' + s.section;
                    document.getElementById('drawerCardName').innerText = s.name;
                    document.getElementById('drawerCardDept').innerText = s.branch + ' - ' + s.section + ' (' + s.batchName + ')';
                    document.getElementById('drawerCardPct').innerText = sum.attendancePercentage + '%';

                    let tierHtml = '';
                    if (sum.performanceTier === 'SAFE') {
                        tierHtml = '<span class="tier-badge tier-badge-safe">&#x2713; Safe (&ge;75%)</span>';
                    } else if (sum.performanceTier === 'WARNING') {
                        tierHtml = '<span class="tier-badge tier-badge-warning">&#x26A0; Warning</span>';
                    } else {
                        tierHtml = '<span class="tier-badge tier-badge-critical">&#x2717; Critical (&lt;60%)</span>';
                    }
                    document.getElementById('drawerCardTier').innerHTML = tierHtml;

                    document.getElementById('drawerTotalSessions').innerText = sum.totalSessions;
                    document.getElementById('drawerPresentCount').innerText = sum.presentCount;
                    document.getElementById('drawerLateCount').innerText = sum.lateCount;
                    document.getElementById('drawerAbsentCount').innerText = sum.absentCount;
                    document.getElementById('drawerExcusedCount').innerText = sum.excusedCount;

                    const tbody = document.getElementById('drawerSessionsBody');
                    if (!sessions || sessions.length === 0) {
                        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted);">No attendance records found for this period.</td></tr>';
                        return;
                    }

                    let rows = '';
                    sessions.forEach(sess => {
                        let statusBadge = '';
                        if (sess.status === 'PRESENT') statusBadge = '<span class="badge-status badge-present">Present</span>';
                        else if (sess.status === 'LATE') statusBadge = '<span class="badge-status badge-late">Late</span>';
                        else if (sess.status === 'EXCUSED' || sess.status === 'OD') statusBadge = '<span class="badge-status badge-excused">Excused</span>';
                        else statusBadge = '<span class="badge-status badge-absent">Absent</span>';

                        let overrideTag = sess.isOverride ? '<div class="text-xs text-muted" style="color:#7C3AED;font-weight:700;">&#x270E; Overridden: ' + (sess.overrideReason || '') + '</div>' : '';

                        rows += '<tr>' +
                            '<td><strong class="font-mono">' + sess.date + '</strong></td>' +
                            '<td>' + (sess.subject || 'General') + '</td>' +
                            '<td>' + statusBadge + '</td>' +
                            '<td><span class="font-mono text-xs">' + sess.checkInTime + '</span></td>' +
                            '<td><div class="text-xs">' + sess.recordingMethod + '</div>' + overrideTag + '</td>' +
                            '<td style="text-align:right;">' +
                                '<button type="button" class="btn-primary-action btn-sm-action" onclick="openOverrideModal(\'' + s.id + '\', \'' + s.name.replace(/'/g, "\\'") + ' (' + s.rollNo + ')\', \'' + sess.date + '\', \'' + sess.status + '\')">Override</button>' +
                            '</td>' +
                        '</tr>';
                    });
                    tbody.innerHTML = rows;
                })
                .catch(err => {
                    console.error(err);
                    document.getElementById('drawerSessionsBody').innerHTML = '<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--danger-red-start);">Failed to load attendance records.</td></tr>';
                });
        }

        // Quick Override Modal
        function openOverrideModal(studentId, studentLabel, date, currentStatus) {
            const modal = document.getElementById('overrideModal');
            const studentSelect = document.getElementById('modalStudentId');
            const dateInput = document.getElementById('modalDate');
            const statusSelect = document.getElementById('modalStatus');
            const reasonInput = document.getElementById('modalReason');
            const confirmCheckbox = document.getElementById('modalConfirm');

            if (studentId && studentSelect) {
                studentSelect.value = studentId;
            }
            if (date && dateInput) {
                dateInput.value = date;
            }
            if (currentStatus && statusSelect) {
                if (currentStatus === 'ABSENT' || currentStatus === 'NOT_RECORDED') {
                    statusSelect.value = 'PRESENT';
                } else {
                    statusSelect.value = currentStatus;
                }
            }
            if (reasonInput) reasonInput.value = '';
            if (confirmCheckbox) confirmCheckbox.checked = false;

            modal.classList.add('active');
        }

        function closeOverrideModal() {
            document.getElementById('overrideModal').classList.remove('active');
        }

        function validateOverrideForm() {
            const reason = document.getElementById('modalReason').value.trim();
            const confirm = document.getElementById('modalConfirm').checked;
            if (!reason) {
                alert('A mandatory reason is required to perform an attendance override.');
                return false;
            }
            if (!confirm) {
                alert('You must confirm the verification checkbox before saving the override.');
                return false;
            }
            return true;
        }

        function openLocationReviewModal(checkId, studentName, rollNo, distanceMeters, slot, coords) {
            document.getElementById('locReviewCheckId').value = checkId;
            document.getElementById('locReviewStudentName').innerText = studentName;
            document.getElementById('locReviewStudentRoll').innerText = rollNo;
            document.getElementById('locReviewDistance').innerText = distanceMeters + 'm outside campus geofence (' + coords + ')';
            document.getElementById('locReviewSlot').innerText = slot;
            document.getElementById('locReviewNotes').value = '';
            document.getElementById('locReviewConfirm').checked = false;

            const modal = document.getElementById('locationReviewModal');
            modal.style.display = 'flex';
            modal.classList.add('active');
        }

        function closeLocationReviewModal() {
            const modal = document.getElementById('locationReviewModal');
            modal.style.display = 'none';
            modal.classList.remove('active');
        }

        function validateLocationReviewForm() {
            const notes = document.getElementById('locReviewNotes').value.trim();
            const confirm = document.getElementById('locReviewConfirm').checked;
            if (!notes) {
                alert('Please enter verification remarks explaining why this student was outside the campus geofence (or confirming unauthorized absence).');
                return false;
            }
            if (!confirm) {
                alert('Please check the confirmation box before submitting the verification decision.');
                return false;
            }
            return true;
        }

        window.onclick = function(event) {
            const modal = document.getElementById('overrideModal');
            if (event.target === modal) {
                closeOverrideModal();
            }
            const locModal = document.getElementById('locationReviewModal');
            if (event.target === locModal) {
                closeLocationReviewModal();
            }
        };
    </script>
</body>
</html>
