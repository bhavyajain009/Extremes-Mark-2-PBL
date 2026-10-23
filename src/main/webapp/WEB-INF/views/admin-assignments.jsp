<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis - Teacher Assignments</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-portal.css">
</head>
<body class="admin-body">

    <div class="admin-layout">
        <!-- Left Dark Navy Sidebar Navigation -->
        <jsp:include page="/WEB-INF/views/includes/admin-sidebar.jsp">
            <jsp:param name="activeSection" value="assignments" />
        </jsp:include>

        <!-- Main Content Area -->
        <div class="admin-main-wrapper">
            <!-- Top White Header Bar -->
            <jsp:include page="/WEB-INF/views/includes/admin-header.jsp" />

            <main class="admin-content-area">

        <!-- Feedback Alerts -->
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

        <div class="mb-16">
            <h2 class="font-bold text-2xl text-dark">Teacher-to-Class and Batch Assignments</h2>
            <p class="text-xs text-muted">Assign teachers to classes, specific batches, and academic subjects with authorization enforcement</p>
        </div>

        <!-- Assignment Creator Form Card -->
        <div class="card mb-16" style="border: 1.5px solid #E2E8F0;">
            <h3 class="font-bold text-dark text-base mb-12">Create New Assignment</h3>
            <form action="${pageContext.request.contextPath}/admin/assignments" method="post" class="filter-grid" id="assignmentForm">
                <input type="hidden" name="action" value="create">

                <div class="filter-field">
                    <label for="assignTeacher">Select Teacher <span style="color:var(--danger-red-start)">*</span></label>
                    <select name="facultyId" id="assignTeacher" class="filter-control" required>
                        <c:forEach var="t" items="${teachers}">
                            <option value="${t.id}">${t.name} (${t.facultyId}) - ${t.department != null ? t.department : t.branch}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="assignClass">Select Class <span style="color:var(--danger-red-start)">*</span></label>
                    <select name="classId" id="assignClass" class="filter-control" onchange="filterBatchesForClass()" required>
                        <c:forEach var="c" items="${classes}">
                            <option value="${c.id}">${c.code} - ${c.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="assignBatch">Select Batch</label>
                    <select name="batchId" id="assignBatch" class="filter-control">
                        <option value="ALL">All Batches (Entire Class)</option>
                        <c:forEach var="b" items="${batches}">
                            <option value="${b.id}" data-class-id="${b.classId}">Batch ${b.code} (${b.name})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="assignSubject">Subject / Course <span style="color:var(--danger-red-start)">*</span></label>
                    <input type="text" name="subject" id="assignSubject" class="filter-control" placeholder="e.g. Deep Learning, Data Structures" required>
                </div>

                <div class="filter-field">
                    <button type="submit" class="btn-primary-action">Save Assignment</button>
                </div>
            </form>
        </div>

        <!-- Assignments Table -->
        <div class="portal-table-container">
            <div class="table-header-bar">
                <div>
                    <h3 class="table-title">Active Teacher Assignments</h3>
                    <p class="table-subtitle">Teachers authorized to manage class attendance and perform student overrides</p>
                </div>
                <span class="status-pill pill-neutral font-mono text-xs">Total: ${assignments.size()}</span>
            </div>

            <div class="portal-table-responsive">
                <table class="portal-data-table">
                    <thead>
                        <tr>
                            <th>Teacher</th>
                            <th>Assigned Class</th>
                            <th>Assigned Batch</th>
                            <th>Subject / Course</th>
                            <th>Assigned Since</th>
                            <th style="text-align: right;">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty assignments}">
                                <tr>
                                    <td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                        No active teacher assignments recorded yet.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="a" items="${assignments}">
                                    <tr>
                                        <td>
                                            <div class="flex items-center gap-8">
                                                <div class="step-icon-circle bg-blue-light text-blue" style="width: 32px; height: 32px; font-weight: 700; font-size: 0.8rem;">
                                                    ${a.facultyName.substring(0, 1)}
                                                </div>
                                                <div>
                                                    <span class="font-bold text-dark">${a.facultyName}</span>
                                                    <span class="text-xs text-muted font-mono" style="display:block;">${a.facultyCode}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="font-bold text-blue font-mono">${a.classCode}</span>
                                            <span class="text-xs text-muted" style="display:block;">${a.className}</span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty a.batchCode}">
                                                    <span class="status-pill pill-neutral font-mono text-xs font-semibold">
                                                        Batch ${a.batchCode} (${a.batchName})
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-pill pill-present text-xs">
                                                        Class-wide (All Batches)
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><span class="font-semibold text-dark">${a.subject}</span></td>
                                        <td><span class="text-xs text-muted font-mono">${a.createdAt}</span></td>
                                        <td style="text-align: right;">
                                            <form action="${pageContext.request.contextPath}/admin/assignments" method="post" style="display:inline; margin: 0;" onsubmit="return confirm('Are you sure you want to remove this assignment?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${a.id}">
                                                <button type="submit" class="btn-sm-action btn-secondary-action" style="color: var(--danger-red-start);">
                                                    Remove
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
            </main>

    <script>
        function filterBatchesForClass() {
            const classSelect = document.getElementById('assignClass');
            const batchSelect = document.getElementById('assignBatch');
            const selectedClassId = classSelect.value;

            for (let i = 0; i < batchSelect.options.length; i++) {
                const opt = batchSelect.options[i];
                if (opt.value === 'ALL') {
                    opt.style.display = 'block';
                    continue;
                }
                const optClassId = opt.getAttribute('data-class-id');
                if (optClassId === selectedClassId) {
                    opt.style.display = 'block';
                } else {
                    opt.style.display = 'none';
                }
            }
            batchSelect.value = 'ALL';
        }

        // Initialize on load
        document.addEventListener('DOMContentLoaded', filterBatchesForClass);
    </script>

    <!-- Reusable Modals (Reports, Settings, Notifications) -->
    <jsp:include page="/WEB-INF/views/includes/admin-modals.jsp" />
        </div>
    </div>

    <!-- Admin Portal Script -->
    <script src="${pageContext.request.contextPath}/js/admin-portal.js"></script>
</body>
</html>
