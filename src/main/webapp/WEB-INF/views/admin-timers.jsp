<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis - Attendance Timers &amp; Time Slot Control</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-portal.css">
    <style>
        .timer-card {
            background: #FFFFFF;
            border: 1.5px solid #CBD5E1;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
            transition: all 0.2s ease;
        }
        .timer-card:hover {
            border-color: #3B82F6;
            box-shadow: 0 6px 20px rgba(59, 130, 246, 0.08);
        }
        .form-control {
            width: 100%;
            padding: 10px 14px;
            border-radius: 8px;
            border: 1.5px solid #CBD5E1;
            font-size: 0.95rem;
            font-family: inherit;
            box-sizing: border-box;
            outline: none;
            transition: border-color 0.15s ease;
        }
        .form-control:focus {
            border-color: #2563EB;
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
        }
    </style>
</head>
<body class="admin-body">

    <div class="admin-layout">
        <!-- Left Dark Navy Sidebar Navigation -->
        <jsp:include page="/WEB-INF/views/includes/admin-sidebar.jsp">
            <jsp:param name="activeSection" value="timers" />
        </jsp:include>

        <!-- Main Content Area -->
        <div class="admin-main-wrapper">
            <!-- Top White Header Bar -->
            <jsp:include page="/WEB-INF/views/includes/admin-header.jsp" />

            <main class="admin-content-area">

        <!-- Flash messages -->
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert-banner alert-banner-success" style="margin-bottom: 1rem;">
                <span><strong>Success:</strong> ${sessionScope.flashSuccess}</span>
            </div>
            <% session.removeAttribute("flashSuccess"); %>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert-banner" style="background: #FEF2F2; border: 1.5px solid #EF4444; color: #991B1B; padding: 0.85rem 1rem; border-radius: 10px; margin-bottom: 1rem; font-size: 0.9rem;">
                <span><strong>Notice:</strong> ${sessionScope.flashError}</span>
            </div>
            <% session.removeAttribute("flashError"); %>
        </c:if>

        <!-- Header Card -->
        <div class="card mb-16" style="background: linear-gradient(135deg, #1E3A8A 0%, #0F172A 100%); color: #FFFFFF; padding: 1.5rem; border: none;">
            <div class="flex-between items-center" style="flex-wrap: wrap; gap: 1rem;">
                <div>
                    <span class="status-pill" style="background: rgba(59, 130, 246, 0.3); color: #93C5FD; margin-bottom: 6px;">
                        Administrative Rights Control
                    </span>
                    <h2 class="font-bold text-2xl text-white" style="margin: 4px 0 2px 0;">Attendance Timers &amp; Time Slot Duration Control</h2>
                    <p class="text-xs text-muted" style="color: #94A3B8; margin: 0;">
                        Control check-in and checkout timer restrictions, customize start and end time windows, and configure active slot durations.
                    </p>
                </div>
                <div class="flex items-center gap-8">
                    <span class="status-pill pill-neutral font-mono text-xs" style="background: #1E293B; color: #38BDF8; border: 1px solid #334155;">
                        Server Time: ${effectiveTime.toString().substring(0, 5)} (Asia/Kolkata)
                    </span>
                </div>
            </div>
        </div>

        <!-- Live Status Banner: Shows current window status to admin -->
        <%
            com.extremis.model.AttendanceTimerSettings _ts = (com.extremis.model.AttendanceTimerSettings) request.getAttribute("timerSettings");
            java.time.LocalTime _now = (java.time.LocalTime) request.getAttribute("effectiveTime");
            boolean _cinOpen = (_ts != null && _now != null) ? _ts.isCheckinWindowActive(_now) : true;
            boolean _coutOpen = (_ts != null && _now != null) ? _ts.isCheckoutWindowActive(_now) : true;
            boolean _cinEnabled = _ts != null && _ts.isCheckinTimerEnabled();
            boolean _coutEnabled = _ts != null && _ts.isCheckoutTimerEnabled();
            request.setAttribute("_cinOpen", _cinOpen);
            request.setAttribute("_coutOpen", _coutOpen);
            request.setAttribute("_cinEnabled", _cinEnabled);
            request.setAttribute("_coutEnabled", _coutEnabled);
        %>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 12px; margin-bottom: 1.5rem;">
            <!-- Check-In Live Status -->
            <div style="padding: 1rem; border-radius: 10px; border: 2px solid ${_cinEnabled ? (_cinOpen ? '#22C55E' : '#EF4444') : '#CBD5E1'}; background: ${_cinEnabled ? (_cinOpen ? '#F0FDF4' : '#FEF2F2') : '#F8FAFC'};">
                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
                    <span style="font-size: 1.1rem;">${_cinEnabled ? (_cinOpen ? '🟢' : '🔴') : '⚪'}</span>
                    <span class="font-bold text-sm" style="color: ${_cinEnabled ? (_cinOpen ? '#166534' : '#991B1B') : '#64748B'};">
                        Check-In: ${_cinEnabled ? (_cinOpen ? 'WINDOW OPEN NOW' : 'BLOCKED — Outside Window') : 'Timer OFF (Open Anytime)'}
                    </span>
                </div>
                <p class="text-xs text-muted" style="margin: 0;">
                    Window: <strong>${timerSettings.formattedCheckinWindow}</strong> &bull; Server time: <strong>${effectiveTime.toString().substring(0, 5)}</strong>
                </p>
            </div>
            <!-- Check-Out Live Status -->
            <div style="padding: 1rem; border-radius: 10px; border: 2px solid ${_coutEnabled ? (_coutOpen ? '#22C55E' : '#EF4444') : '#CBD5E1'}; background: ${_coutEnabled ? (_coutOpen ? '#F0FDF4' : '#FEF2F2') : '#F8FAFC'};">
                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
                    <span style="font-size: 1.1rem;">${_coutEnabled ? (_coutOpen ? '🟢' : '🔴') : '⚪'}</span>
                    <span class="font-bold text-sm" style="color: ${_coutEnabled ? (_coutOpen ? '#166534' : '#991B1B') : '#64748B'};">
                        Check-Out: ${_coutEnabled ? (_coutOpen ? 'WINDOW OPEN NOW' : 'BLOCKED — Outside Window') : 'Timer OFF (Open Anytime)'}
                    </span>
                </div>
                <p class="text-xs text-muted" style="margin: 0;">
                    Window: <strong>${timerSettings.formattedCheckoutWindow}</strong> &bull; Server time: <strong>${effectiveTime.toString().substring(0, 5)}</strong>
                </p>
            </div>
        </div>

        <!-- Quick 1-Click Action Switches -->
        <div class="card p-16 mb-24" style="background: #FFFFFF; border: 2px solid #CBD5E1; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);">
            <h3 class="font-bold text-lg text-dark mb-4">Quick Timer Power Switches</h3>
            <p class="text-xs text-muted mb-16">Instantly activate or bypass timer restrictions with a single click without changing slot times.</p>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1rem;">
                <!-- Check-in Quick Switch Card -->
                <div style="background: #F8FAFC; border: 1.5px solid ${timerSettings.checkinTimerEnabled ? '#86EFAC' : '#CBD5E1'}; border-radius: 10px; padding: 1rem;">
                    <div class="flex-between items-center mb-8">
                        <div class="flex items-center gap-8">
                            <span style="font-size: 1.25rem;">📥</span>
                            <div>
                                <span class="font-bold text-sm text-dark">Check-In Timer</span>
                                <div class="text-xs text-muted">Morning attendance window</div>
                            </div>
                        </div>
                        <c:choose>
                            <c:when test="${timerSettings.checkinTimerEnabled}">
                                <span class="status-pill" style="background: #16A34A; color: #FFFFFF; font-weight: 800; font-size: 0.78rem;">● ACTIVE (Enforced)</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status-pill" style="background: #64748B; color: #FFFFFF; font-weight: 800; font-size: 0.78rem;">○ OFF (Open / Flexible)</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="text-xs text-muted mb-12">
                        Window: <strong>${timerSettings.formattedCheckinWindow}</strong> (${timerSettings.checkinDurationMinutes} mins)
                    </div>
                    <form method="post" action="${pageContext.request.contextPath}/admin/timers" style="margin: 0;">
                        <input type="hidden" name="action" value="toggle_checkin_timer">
                        <input type="hidden" name="enable" value="${timerSettings.checkinTimerEnabled ? '0' : '1'}">
                        <button type="submit" class="btn-sm-action" style="width: 100%; background: ${timerSettings.checkinTimerEnabled ? '#DC2626' : '#16A34A'}; color: #FFFFFF; border: none; padding: 8px 14px; border-radius: 8px; font-weight: 800; cursor: pointer; text-align: center;">
                            ${timerSettings.checkinTimerEnabled ? '🛑 Turn OFF Check-In Timer' : '🚀 Turn ON Check-In Timer'}
                        </button>
                    </form>
                </div>

                <!-- Checkout Quick Switch Card -->
                <div style="background: #F8FAFC; border: 1.5px solid ${timerSettings.checkoutTimerEnabled ? '#86EFAC' : '#CBD5E1'}; border-radius: 10px; padding: 1rem;">
                    <div class="flex-between items-center mb-8">
                        <div class="flex items-center gap-8">
                            <span style="font-size: 1.25rem;">📤</span>
                            <div>
                                <span class="font-bold text-sm text-dark">Check-Out Timer</span>
                                <div class="text-xs text-muted">Departure attendance window</div>
                            </div>
                        </div>
                        <c:choose>
                            <c:when test="${timerSettings.checkoutTimerEnabled}">
                                <span class="status-pill" style="background: #16A34A; color: #FFFFFF; font-weight: 800; font-size: 0.78rem;">● ACTIVE (Enforced)</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status-pill" style="background: #64748B; color: #FFFFFF; font-weight: 800; font-size: 0.78rem;">○ OFF (Open / Flexible)</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="text-xs text-muted mb-12">
                        Window: <strong>${timerSettings.formattedCheckoutWindow}</strong> (${timerSettings.checkoutDurationMinutes} mins)
                    </div>
                    <form method="post" action="${pageContext.request.contextPath}/admin/timers" style="margin: 0;">
                        <input type="hidden" name="action" value="toggle_checkout_timer">
                        <input type="hidden" name="enable" value="${timerSettings.checkoutTimerEnabled ? '0' : '1'}">
                        <button type="submit" class="btn-sm-action" style="width: 100%; background: ${timerSettings.checkoutTimerEnabled ? '#DC2626' : '#16A34A'}; color: #FFFFFF; border: none; padding: 8px 14px; border-radius: 8px; font-weight: 800; cursor: pointer; text-align: center;">
                            ${timerSettings.checkoutTimerEnabled ? '🛑 Turn OFF Check-Out Timer' : '🚀 Turn ON Check-Out Timer'}
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <!-- Full Time Slot & Duration Configuration Form -->
        <div class="portal-table-container mb-24" style="border: 2px solid #3B82F6; box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08); border-radius: 12px; overflow: hidden;">
            <div class="table-header-bar" style="background: #0F172A; color: #FFFFFF; padding: 1.25rem 1.5rem;">
                <div>
                    <h3 class="table-title" style="color: #FFFFFF; font-size: 1.15rem; margin: 0;">Time Slot &amp; Duration Configurator</h3>
                    <p class="table-subtitle" style="color: #94A3B8; margin: 0;">Adjust exact start times, end times, and duration minutes according to college academic schedule</p>
                </div>
            </div>

            <div style="background: #F8FAFC; padding: 1.5rem;">
                <form method="post" action="${pageContext.request.contextPath}/admin/timers">
                    <input type="hidden" name="action" value="update_attendance_timers">

                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
                        <!-- Check-In Slot Settings -->
                        <div class="timer-card">
                            <div class="flex-between items-center mb-12">
                                <div class="flex items-center gap-8">
                                    <div class="step-icon-circle bg-blue-light text-blue" style="width: 34px; height: 34px; font-size: 1rem;">📥</div>
                                    <div>
                                        <h4 class="font-bold text-dark text-base" style="margin: 0;">Check-In Time Slot</h4>
                                        <p class="text-xs text-muted" style="margin: 0;">Morning student arrival window</p>
                                    </div>
                                </div>
                                <label style="display: flex; align-items: center; gap: 6px; font-size: 0.85rem; font-weight: 700; cursor: pointer; color: #1E293B;">
                                    <input type="checkbox" name="checkinEnabled" value="true" ${timerSettings.checkinTimerEnabled ? 'checked' : ''}>
                                    Enforce Window
                                </label>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px;">
                                <div>
                                    <label class="text-xs text-muted font-bold" for="cinStart" style="text-transform: uppercase;">Window Opens (Start)</label>
                                    <input type="time" id="cinStart" name="checkinStartTime" value="${timerSettings.checkinStartTime.toString().substring(0,5)}" required class="form-control" style="font-weight: 700;" onchange="updateCheckinDuration()">
                                </div>
                                <div>
                                    <label class="text-xs text-muted font-bold" for="cinEnd" style="text-transform: uppercase;">Window Closes (End)</label>
                                    <input type="time" id="cinEnd" name="checkinEndTime" value="${timerSettings.checkinEndTime.toString().substring(0,5)}" required class="form-control" style="font-weight: 700;" onchange="updateCheckinDuration()">
                                </div>
                            </div>

                            <div class="mb-12">
                                <label class="text-xs text-muted font-bold" for="cinDur" style="text-transform: uppercase;">Slot Duration (Minutes)</label>
                                <input type="number" id="cinDur" name="checkinDurationMinutes" min="1" max="300" value="${timerSettings.checkinDurationMinutes}" required class="form-control" style="font-weight: 700;" onchange="updateCheckinEndTime()">
                                <span class="text-xs text-muted" style="margin-top: 4px; display: block;">Modifying duration automatically recalculates the window closing time.</span>
                            </div>

                            <!-- Presets -->
                            <div style="background: #F1F5F9; padding: 8px 12px; border-radius: 8px;">
                                <span class="text-xs font-bold text-muted">Quick Presets:</span>
                                <div style="display: flex; gap: 6px; margin-top: 4px; flex-wrap: wrap;">
                                    <button type="button" onclick="setPreset('cin', '08:50', '09:05', 15)" class="btn-sm-action btn-secondary-action" style="font-size: 0.72rem; padding: 3px 8px;">Standard (8:50-9:05 AM)</button>
                                    <button type="button" onclick="setPreset('cin', '08:30', '09:30', 60)" class="btn-sm-action btn-secondary-action" style="font-size: 0.72rem; padding: 3px 8px;">Extended (8:30-9:30 AM)</button>
                                    <button type="button" onclick="setPreset('cin', '08:00', '12:00', 240)" class="btn-sm-action btn-secondary-action" style="font-size: 0.72rem; padding: 3px 8px;">Half-Day (8:00 AM-12:00 PM)</button>
                                </div>
                            </div>
                        </div>

                        <!-- Check-Out Slot Settings -->
                        <div class="timer-card">
                            <div class="flex-between items-center mb-12">
                                <div class="flex items-center gap-8">
                                    <div class="step-icon-circle bg-blue-light text-blue" style="width: 34px; height: 34px; font-size: 1rem;">📤</div>
                                    <div>
                                        <h4 class="font-bold text-dark text-base" style="margin: 0;">Check-Out Time Slot</h4>
                                        <p class="text-xs text-muted" style="margin: 0;">Evening departure window</p>
                                    </div>
                                </div>
                                <label style="display: flex; align-items: center; gap: 6px; font-size: 0.85rem; font-weight: 700; cursor: pointer; color: #1E293B;">
                                    <input type="checkbox" name="checkoutEnabled" value="true" ${timerSettings.checkoutTimerEnabled ? 'checked' : ''}>
                                    Enforce Window
                                </label>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px;">
                                <div>
                                    <label class="text-xs text-muted font-bold" for="coutStart" style="text-transform: uppercase;">Window Opens (Start)</label>
                                    <input type="time" id="coutStart" name="checkoutStartTime" value="${timerSettings.checkoutStartTime.toString().substring(0,5)}" required class="form-control" style="font-weight: 700;" onchange="updateCheckoutDuration()">
                                </div>
                                <div>
                                    <label class="text-xs text-muted font-bold" for="coutEnd" style="text-transform: uppercase;">Window Closes (End)</label>
                                    <input type="time" id="coutEnd" name="checkoutEndTime" value="${timerSettings.checkoutEndTime.toString().substring(0,5)}" required class="form-control" style="font-weight: 700;" onchange="updateCheckoutDuration()">
                                </div>
                            </div>

                            <div class="mb-12">
                                <label class="text-xs text-muted font-bold" for="coutDur" style="text-transform: uppercase;">Slot Duration (Minutes)</label>
                                <input type="number" id="coutDur" name="checkoutDurationMinutes" min="1" max="600" value="${timerSettings.checkoutDurationMinutes}" required class="form-control" style="font-weight: 700;" onchange="updateCheckoutEndTime()">
                                <span class="text-xs text-muted" style="margin-top: 4px; display: block;">Modifying duration automatically recalculates the departure closing time.</span>
                            </div>

                            <!-- Presets -->
                            <div style="background: #F1F5F9; padding: 8px 12px; border-radius: 8px;">
                                <span class="text-xs font-bold text-muted">Quick Presets:</span>
                                <div style="display: flex; gap: 6px; margin-top: 4px; flex-wrap: wrap;">
                                    <button type="button" onclick="setPreset('cout', '15:40', '18:00', 140)" class="btn-sm-action btn-secondary-action" style="font-size: 0.72rem; padding: 3px 8px;">Standard (3:40-6:00 PM)</button>
                                    <button type="button" onclick="setPreset('cout', '14:30', '17:30', 180)" class="btn-sm-action btn-secondary-action" style="font-size: 0.72rem; padding: 3px 8px;">Early (2:30-5:30 PM)</button>
                                    <button type="button" onclick="setPreset('cout', '16:00', '19:00', 180)" class="btn-sm-action btn-secondary-action" style="font-size: 0.72rem; padding: 3px 8px;">Late Evening (4:00-7:00 PM)</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="flex-between items-center" style="flex-wrap: wrap; gap: 12px; background: #FFFFFF; padding: 1rem 1.25rem; border-radius: 10px; border: 1.5px solid #CBD5E1;">
                        <div>
                            <span class="font-bold text-sm text-dark">Live Policy Impact:</span>
                            <p class="text-xs text-muted" style="margin: 2px 0 0 0;">
                                Saved slots and timer switches are enforced immediately on student and faculty portals.
                            </p>
                        </div>
                        <button type="submit" class="btn-sm-action" style="background: #2563EB; color: #FFFFFF; border: none; padding: 12px 28px; border-radius: 8px; font-weight: 800; cursor: pointer; font-size: 0.95rem; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);">
                            💾 Save &amp; Apply Attendance Timer Settings
                        </button>
                    </div>
                </form>
            </div>
        </div>
            </main>

            <!-- Reusable Modals (Reports, Settings, Notifications) -->
            <jsp:include page="/WEB-INF/views/includes/admin-modals.jsp" />
        </div>
    </div>

    <!-- Admin Portal Script -->
    <script src="${pageContext.request.contextPath}/js/admin-portal.js"></script>

    <script>
        function timeToMinutes(tStr) {
            if (!tStr) return 0;
            var parts = tStr.split(':');
            return parseInt(parts[0], 10) * 60 + parseInt(parts[1], 10);
        }
        function minutesToTime(mins) {
            mins = (mins % (24 * 60) + 24 * 60) % (24 * 60);
            var h = Math.floor(mins / 60);
            var m = mins % 60;
            return (h < 10 ? '0' : '') + h + ':' + (m < 10 ? '0' : '') + m;
        }
        function updateCheckinDuration() {
            var s = document.getElementById('cinStart').value;
            var e = document.getElementById('cinEnd').value;
            if (s && e) {
                var diff = timeToMinutes(e) - timeToMinutes(s);
                if (diff > 0) document.getElementById('cinDur').value = diff;
            }
        }
        function updateCheckinEndTime() {
            var s = document.getElementById('cinStart').value;
            var dur = parseInt(document.getElementById('cinDur').value, 10);
            if (s && !isNaN(dur) && dur > 0) {
                document.getElementById('cinEnd').value = minutesToTime(timeToMinutes(s) + dur);
            }
        }
        function updateCheckoutDuration() {
            var s = document.getElementById('coutStart').value;
            var e = document.getElementById('coutEnd').value;
            if (s && e) {
                var diff = timeToMinutes(e) - timeToMinutes(s);
                if (diff > 0) document.getElementById('coutDur').value = diff;
            }
        }
        function updateCheckoutEndTime() {
            var s = document.getElementById('coutStart').value;
            var dur = parseInt(document.getElementById('coutDur').value, 10);
            if (s && !isNaN(dur) && dur > 0) {
                document.getElementById('coutEnd').value = minutesToTime(timeToMinutes(s) + dur);
            }
        }
        function setPreset(type, start, end, dur) {
            if (type === 'cin') {
                document.getElementById('cinStart').value = start;
                document.getElementById('cinEnd').value = end;
                document.getElementById('cinDur').value = dur;
            } else if (type === 'cout') {
                document.getElementById('coutStart').value = start;
                document.getElementById('coutEnd').value = end;
                document.getElementById('coutDur').value = dur;
            }
        }
    </script>
</body>
</html>
