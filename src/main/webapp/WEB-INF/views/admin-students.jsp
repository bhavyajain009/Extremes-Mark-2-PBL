<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis - Student Management &amp; Class Directory</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        .class-filter-pill {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            border-radius: 9999px;
            font-size: 0.85rem;
            font-weight: 700;
            text-decoration: none;
            transition: all 0.2s ease;
            border: 1.5px solid #CBD5E1;
            background: #FFFFFF;
            color: #334155;
        }
        .class-filter-pill:hover {
            border-color: #0284C7;
            color: #0284C7;
            background: #F0F9FF;
        }
        .class-filter-pill.active {
            background: #0F172A;
            color: #FFFFFF;
            border-color: #0F172A;
            box-shadow: 0 2px 8px rgba(15, 23, 42, 0.2);
        }
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.65);
            backdrop-filter: blur(4px);
            z-index: 1000;
            align-items: center;
            justify-content: center;
            padding: 1rem;
        }
        .modal-overlay.active {
            display: flex;
        }
        .modal-card {
            background: #FFFFFF;
            border-radius: 16px;
            width: 100%;
            max-width: 580px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.25);
            overflow: hidden;
            animation: modalFadeIn 0.2s ease-out;
        }
        @keyframes modalFadeIn {
            from { opacity: 0; transform: scale(0.96); }
            to { opacity: 1; transform: scale(1); }
        }
        .modal-header {
            background: #0F172A;
            color: #FFFFFF;
            padding: 1.25rem 1.5rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .modal-body {
            padding: 1.5rem;
            max-height: 80vh;
            overflow-y: auto;
        }
        .form-group {
            margin-bottom: 1.1rem;
        }
        .form-group label {
            display: block;
            font-size: 0.82rem;
            font-weight: 700;
            color: #334155;
            margin-bottom: 6px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .form-control {
            width: 100%;
            padding: 10px 14px;
            border-radius: 8px;
            border: 1.5px solid #CBD5E1;
            font-size: 0.92rem;
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-portal.css">
</head>
<body class="admin-body">

    <div class="admin-layout">
        <!-- Left Dark Navy Sidebar Navigation -->
        <jsp:include page="/WEB-INF/views/includes/admin-sidebar.jsp">
            <jsp:param name="activeSection" value="students" />
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
                <span>${sessionScope.flashError}</span>
            </div>
            <% session.removeAttribute("flashError"); %>
        </c:if>

        <!-- Top Header & Primary Action Toolbar -->
        <div class="flex-between items-center mb-16" style="flex-wrap: wrap; gap: 1rem;">
            <div>
                <h2 class="font-bold text-2xl text-dark" style="margin: 0;">Student Management &amp; Class Directory</h2>
                <p class="text-xs text-muted" style="margin-top: 4px;">
                    View class-wise enrollment, register new students, and perform mass Excel imports with templates.
                </p>
            </div>

            <!-- Action Buttons: Add Student & Import Excel -->
            <div class="flex items-center gap-12">
                <button type="button" onclick="openModal('addStudentModal')" class="btn-sm-action" style="background: #2563EB; color: #FFFFFF; border: none; padding: 10px 18px; border-radius: 8px; font-weight: 800; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3);">
                    <span>➕ Add New Student</span>
                </button>

                <button type="button" onclick="openModal('importExcelModal')" class="btn-sm-action" style="background: #059669; color: #FFFFFF; border: none; padding: 10px 18px; border-radius: 8px; font-weight: 800; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; box-shadow: 0 2px 6px rgba(5, 150, 105, 0.3);">
                    <span>📥 Mass Import (Excel / CSV)</span>
                </button>
            </div>
        </div>

        <!-- Class Filter Bar (Pills) -->
        <div class="card p-16 mb-16" style="background: #FFFFFF; border: 1.5px solid #E2E8F0;">
            <div class="flex items-center gap-8 mb-12">
                <span style="font-size: 1.1rem;">🏫</span>
                <span class="font-bold text-sm text-dark">Filter Class-Wise:</span>
            </div>

            <div style="display: flex; flex-wrap: wrap; gap: 8px; align-items: center;">
                <a href="${pageContext.request.contextPath}/admin/students" class="class-filter-pill ${empty selectedClassId ? 'active' : ''}">
                    All Classes (${allStudentsCount})
                </a>
                <c:forEach var="c" items="${allClasses}">
                    <a href="${pageContext.request.contextPath}/admin/students?classId=${c.id}"
                       class="class-filter-pill ${selectedClassId == c.id ? 'active' : ''}">
                        <strong>${c.code}</strong> &ndash; ${c.name} (${c.studentCount})
                    </a>
                </c:forEach>
            </div>

            <!-- Secondary Filters: Batch & Search -->
            <form method="get" action="${pageContext.request.contextPath}/admin/students" style="margin: 12px 0 0 0; display: flex; flex-wrap: wrap; gap: 10px; align-items: center;">
                <c:if test="${not empty selectedClassId}">
                    <input type="hidden" name="classId" value="${selectedClassId}">
                </c:if>

                <div style="display: flex; align-items: center; gap: 6px;">
                    <label class="text-xs text-muted font-bold" for="batchSelect">Batch:</label>
                    <select id="batchSelect" name="batchId" class="form-control" style="padding: 6px 10px; font-size: 0.85rem; width: auto;" onchange="this.form.submit()">
                        <option value="">All Batches</option>
                        <c:forEach var="b" items="${classBatches}">
                            <option value="${b.id}" ${selectedBatchId == b.id ? 'selected' : ''}>
                                ${b.code} &ndash; ${b.name}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div style="display: flex; align-items: center; gap: 6px; flex: 1; min-width: 200px;">
                    <input type="text" name="search" value="${searchQuery}" placeholder="Search student name, roll number, or email..." class="form-control" style="padding: 6px 12px; font-size: 0.85rem;">
                    <button type="submit" class="btn-sm-action" style="background: #0F172A; color: #FFFFFF; border: none; padding: 7px 14px; border-radius: 6px; font-weight: 700; cursor: pointer;">
                        Search
                    </button>
                    <c:if test="${not empty searchQuery || not empty selectedBatchId}">
                        <a href="${pageContext.request.contextPath}/admin/students${not empty selectedClassId ? '?classId='.concat(selectedClassId) : ''}" class="text-xs link-blue font-bold" style="white-space: nowrap;">Clear Filters</a>
                    </c:if>
                </div>
            </form>
        </div>

        <!-- Class Details Banner (When filtered by class) -->
        <c:if test="${not empty selectedClass}">
            <div class="card mb-16" style="background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%); color: #FFFFFF; padding: 1.25rem 1.5rem; border: none;">
                <div class="flex-between items-center" style="flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <span class="status-pill" style="background: rgba(56, 189, 248, 0.2); color: #38BDF8; font-weight: 800; font-size: 0.75rem;">
                            Class: ${selectedClass.code}
                        </span>
                        <h3 class="font-bold text-xl text-white" style="margin: 4px 0 2px 0;">${selectedClass.name}</h3>
                        <p class="text-xs text-muted" style="color: #94A3B8; margin: 0;">
                            Department: <strong>${selectedClass.department}</strong> | Semester: <strong>${selectedClass.semester}</strong>
                        </p>
                    </div>

                    <div style="display: flex; gap: 1rem;">
                        <div style="text-align: center; background: rgba(255,255,255,0.06); padding: 8px 16px; border-radius: 8px;">
                            <div style="font-size: 1.35rem; font-weight: 800; color: #38BDF8;">${totalStudents}</div>
                            <div style="font-size: 0.72rem; color: #94A3B8; text-transform: uppercase;">Students</div>
                        </div>
                        <div style="text-align: center; background: rgba(255,255,255,0.06); padding: 8px 16px; border-radius: 8px;">
                            <div style="font-size: 1.35rem; font-weight: 800; color: #4ADE80;">${presentCount}</div>
                            <div style="font-size: 0.72rem; color: #94A3B8; text-transform: uppercase;">Present Today</div>
                        </div>
                        <div style="text-align: center; background: rgba(255,255,255,0.06); padding: 8px 16px; border-radius: 8px;">
                            <div style="font-size: 1.35rem; font-weight: 800; color: #F87171;">${lockedCount}</div>
                            <div style="font-size: 0.72rem; color: #94A3B8; text-transform: uppercase;">Device Locked</div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- Students Directory Table -->
        <div class="portal-table-container">
            <div class="table-header-bar">
                <div>
                    <h3 class="table-title">
                        <c:choose>
                            <c:when test="${not empty selectedClass}">Students in ${selectedClass.code} &ndash; ${selectedClass.name}</c:when>
                            <c:otherwise>All Enrolled Students (Class-Wise Directory)</c:otherwise>
                        </c:choose>
                    </h3>
                    <p class="table-subtitle">Showing ${students.size()} registered student record(s)</p>
                </div>

                <div class="flex items-center gap-8">
                    <span class="status-pill pill-neutral font-mono text-xs">Total: ${students.size()}</span>
                </div>
            </div>

            <div class="portal-table-responsive">
                <table class="portal-data-table">
                    <thead>
                        <tr>
                            <th>Student</th>
                            <th>Roll Number</th>
                            <th>Class &amp; Dept</th>
                            <th>Batch</th>
                            <th>Today's Attendance</th>
                            <th>Device Lock Status</th>
                            <th style="text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty students}">
                                <c:forEach var="s" items="${students}">
                                    <c:set var="att" value="${todayAttendanceMap[s.id]}"/>
                                    <tr>
                                        <td>
                                            <div class="flex items-center gap-8">
                                                <div class="step-icon-circle bg-blue-light text-blue" style="width: 32px; height: 32px; font-weight: 800; font-size: 0.85rem;">
                                                    ${s.name.substring(0, 1)}
                                                </div>
                                                <div>
                                                    <div class="font-bold text-dark">${s.name}</div>
                                                    <div class="text-xs text-muted">${s.email}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="font-mono font-bold" style="color: #0F172A; background: #F1F5F9; padding: 2px 8px; border-radius: 6px; font-size: 0.85rem;">
                                                ${s.rollNo}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="font-bold text-dark">${s.branch}</span>
                                            <div class="text-xs text-muted">${s.section}</div>
                                        </td>
                                        <td>
                                            <span class="status-pill font-mono" style="background: #EFF6FF; color: #1D4ED8; font-weight: 700; font-size: 0.78rem;">
                                                ${not empty s.batchCode ? s.batchCode : (not empty s.batchName ? s.batchName : 'A1')}
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty att && not empty att.checkInTime}">
                                                    <span class="badge-status badge-present">
                                                        ✓ Present (${att.formattedCheckInTime})
                                                    </span>
                                                    <c:if test="${not empty att.checkOutTime}">
                                                        <div class="text-xs text-muted" style="margin-top: 2px;">Out: ${att.formattedCheckOutTime}</div>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-status badge-absent">
                                                        ○ Not Checked In
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty s.sessionToken}">
                                                    <div class="flex items-center gap-6">
                                                        <span class="badge-status badge-absent" style="font-size: 0.75rem;">
                                                            🔒 Device Locked
                                                        </span>
                                                        <form method="post" action="${pageContext.request.contextPath}/admin/students" style="margin: 0;">
                                                            <input type="hidden" name="action" value="reset_session">
                                                            <input type="hidden" name="studentId" value="${s.id}">
                                                            <c:if test="${not empty selectedClassId}">
                                                                <input type="hidden" name="returnClassId" value="${selectedClassId}">
                                                            </c:if>
                                                            <button type="submit" class="btn-sm-action btn-secondary-action" style="padding: 2px 6px; font-size: 0.72rem; color: #DC2626;" title="Clear device session lock">
                                                                Reset
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-status" style="background: #F1F5F9; color: #64748B; font-size: 0.75rem;">
                                                        ✓ Unlocked
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <form method="post" action="${pageContext.request.contextPath}/admin/students" style="display:inline; margin: 0;" onsubmit="return confirm('Are you sure you want to delete student ${s.name} (${s.rollNo}) from the database?');">
                                                <input type="hidden" name="action" value="delete_student">
                                                <input type="hidden" name="studentId" value="${s.id}">
                                                <c:if test="${not empty selectedClassId}">
                                                    <input type="hidden" name="returnClassId" value="${selectedClassId}">
                                                </c:if>
                                                <button type="submit" class="btn-sm-action btn-secondary-action" style="color: #DC2626; padding: 4px 10px; font-size: 0.75rem;" title="Delete Student">
                                                    🗑️ Delete
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="text-center py-20 text-muted text-sm">
                                        No students found matching the selected class or search filter.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
            </main>

    <!-- 1. Add Single Student Modal -->
    <div id="addStudentModal" class="modal-overlay">
        <div class="modal-card">
            <div class="modal-header">
                <div class="flex items-center gap-8">
                    <span style="font-size: 1.25rem;">➕</span>
                    <h3 style="margin: 0; font-size: 1.15rem; font-weight: 800;">Register New Student</h3>
                </div>
                <button type="button" onclick="closeModal('addStudentModal')" style="background: none; border: none; color: #94A3B8; font-size: 1.5rem; cursor: pointer; line-height: 1;">&times;</button>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/admin/students">
                <input type="hidden" name="action" value="add_student">
                <c:if test="${not empty selectedClassId}">
                    <input type="hidden" name="returnClassId" value="${selectedClassId}">
                </c:if>

                <div class="modal-body">
                    <div class="form-group">
                        <label for="newRollNo">Roll Number *</label>
                        <input type="text" id="newRollNo" name="rollNo" required placeholder="e.g. 24EARAD032" class="form-control" style="font-family: monospace; font-weight: 700;">
                        <span class="text-xs text-muted">Must be unique across the college system.</span>
                    </div>

                    <div class="form-group">
                        <label for="newName">Full Name *</label>
                        <input type="text" id="newName" name="name" required placeholder="e.g. Kavya Sharma" class="form-control">
                    </div>

                    <div class="form-group">
                        <label for="newEmail">Email Address (Optional)</label>
                        <input type="email" id="newEmail" name="email" placeholder="e.g. kavya.24earad032@aryacollege.edu.in" class="form-control">
                        <span class="text-xs text-muted">Leave blank to auto-generate from roll number.</span>
                    </div>

                    <div class="form-group">
                        <label for="newClassId">Class &amp; Department *</label>
                        <select id="newClassId" name="classId" required class="form-control" onchange="filterModalBatches(this.value)">
                            <c:forEach var="c" items="${allClasses}">
                                <option value="${c.id}" ${selectedClassId == c.id ? 'selected' : ''}>
                                    ${c.code} &ndash; ${c.name} (${c.department})
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="newBatchId">Batch *</label>
                        <select id="newBatchId" name="batchId" required class="form-control">
                            <c:forEach var="b" items="${allBatches}">
                                <option value="${b.id}" data-class-id="${b.classId}">
                                    ${b.code} &ndash; ${b.name} (${b.classCode})
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="newPassword">Initial Password (Optional)</label>
                        <input type="password" id="newPassword" name="password" placeholder="Default: StudentPassword123!" class="form-control">
                        <span class="text-xs text-muted">Default is <code>StudentPassword123!</code> if omitted.</span>
                    </div>
                </div>

                <div style="background: #F8FAFC; padding: 1rem 1.5rem; display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid #E2E8F0;">
                    <button type="button" onclick="closeModal('addStudentModal')" class="btn-sm-action btn-secondary-action" style="padding: 8px 16px;">Cancel</button>
                    <button type="submit" class="btn-sm-action" style="background: #2563EB; color: #FFFFFF; border: none; padding: 8px 20px; border-radius: 8px; font-weight: 800; cursor: pointer;">
                        Save Student
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- 2. Mass Import from Excel / CSV Modal -->
    <div id="importExcelModal" class="modal-overlay">
        <div class="modal-card" style="max-width: 640px;">
            <div class="modal-header" style="background: #064E3B;">
                <div class="flex items-center gap-8">
                    <span style="font-size: 1.25rem;">📊</span>
                    <h3 style="margin: 0; font-size: 1.15rem; font-weight: 800; color: #FFFFFF;">Mass Import Students via Excel</h3>
                </div>
                <button type="button" onclick="closeModal('importExcelModal')" style="background: none; border: none; color: #A7F3D0; font-size: 1.5rem; cursor: pointer; line-height: 1;">&times;</button>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/admin/students" enctype="multipart/form-data">
                <input type="hidden" name="action" value="import_excel">
                <c:if test="${not empty selectedClassId}">
                    <input type="hidden" name="returnClassId" value="${selectedClassId}">
                </c:if>

                <div class="modal-body">
                    <!-- Step 1: Download Template -->
                    <div class="card p-12 mb-16" style="background: #F0FDF4; border: 1.5px solid #86EFAC;">
                        <div class="flex-between items-center" style="flex-wrap: wrap; gap: 8px;">
                            <div>
                                <span class="font-bold text-sm" style="color: #166534;">Step 1: Download Official Template</span>
                                <p class="text-xs text-muted" style="margin: 2px 0 0 0;">Download the pre-structured Excel or CSV spreadsheet template with instructions.</p>
                            </div>
                            <div class="flex items-center gap-6">
                                <a href="${pageContext.request.contextPath}/admin/students/template?format=xlsx" class="btn-sm-action" style="background: #15803D; color: #FFFFFF; text-decoration: none; padding: 6px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 800;">
                                    📥 Excel (.xlsx)
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/students/template?format=csv" class="btn-sm-action" style="background: #334155; color: #FFFFFF; text-decoration: none; padding: 6px 12px; border-radius: 6px; font-size: 0.82rem; font-weight: 700;">
                                    📄 CSV (.csv)
                                </a>
                            </div>
                        </div>
                    </div>

                    <!-- Step 2: Upload File -->
                    <div class="form-group mb-16">
                        <label for="excelFile" class="font-bold" style="color: #0F172A;">Step 2: Select Completed File (.xlsx, .xls, .csv) *</label>
                        <input type="file" id="excelFile" name="file" required accept=".xlsx,.xls,.csv" class="form-control" style="padding: 12px; border: 2px dashed #059669; background: #F8FAFC;">
                        <span class="text-xs text-muted" style="margin-top: 4px; display: block;">Supports Microsoft Excel (.xlsx, .xls) and CSV files up to 10MB.</span>
                    </div>

                    <!-- Step 3: Cheatsheet & Valid Codes -->
                    <div class="card p-12" style="background: #F8FAFC; border: 1px solid #CBD5E1; font-size: 0.78rem;">
                        <span class="font-bold text-dark" style="text-transform: uppercase;">Class &amp; Batch Reference Codes:</span>
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 6px; margin-top: 6px;">
                            <c:forEach var="c" items="${allClasses}">
                                <div style="background: #FFFFFF; padding: 4px 8px; border-radius: 4px; border: 1px solid #E2E8F0;">
                                    <strong style="color: #2563EB;">${c.code}</strong>: ${c.name}
                                </div>
                            </c:forEach>
                        </div>
                        <div class="text-xs text-muted" style="margin-top: 6px;">
                            Valid batch codes: <code>A1</code>, <code>A2</code>, <code>C1</code>, <code>C2</code>, <code>E1</code>, <code>E2</code>. If left blank, the first batch in the class is assigned.
                        </div>
                    </div>
                </div>

                <div style="background: #F8FAFC; padding: 1rem 1.5rem; display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid #E2E8F0;">
                    <button type="button" onclick="closeModal('importExcelModal')" class="btn-sm-action btn-secondary-action" style="padding: 8px 16px;">Cancel</button>
                    <button type="submit" class="btn-sm-action" style="background: #059669; color: #FFFFFF; border: none; padding: 8px 20px; border-radius: 8px; font-weight: 800; cursor: pointer;">
                        Start Mass Import
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openModal(id) {
            var m = document.getElementById(id);
            if (m) m.classList.add('active');
        }

        function closeModal(id) {
            var m = document.getElementById(id);
            if (m) m.classList.remove('active');
        }

        function filterModalBatches(classId) {
            var batchSelect = document.getElementById('newBatchId');
            if (!batchSelect) return;
            var options = batchSelect.options;
            var firstValid = null;
            for (var i = 0; i < options.length; i++) {
                var opt = options[i];
                var optClassId = opt.getAttribute('data-class-id');
                if (!classId || optClassId === classId) {
                    opt.style.display = '';
                    if (!firstValid) firstValid = opt.value;
                } else {
                    opt.style.display = 'none';
                }
            }
            if (firstValid) {
                batchSelect.value = firstValid;
            }
        }

        // Initialize batch dropdown filter in modal
        document.addEventListener('DOMContentLoaded', function() {
            var classSelect = document.getElementById('newClassId');
            if (classSelect) {
                filterModalBatches(classSelect.value);
            }
        });
    </script>

    <!-- Reusable Modals (Reports, Settings, Notifications) -->
    <jsp:include page="/WEB-INF/views/includes/admin-modals.jsp" />
        </div>
    </div>

    <!-- Admin Portal Script -->
    <script src="${pageContext.request.contextPath}/js/admin-portal.js"></script>
</body>
</html>
