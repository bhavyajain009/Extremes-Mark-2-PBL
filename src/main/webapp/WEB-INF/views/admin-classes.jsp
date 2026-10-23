<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis - Class and Batch Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-portal.css">
</head>
<body class="admin-body">

    <div class="admin-layout">
        <!-- Left Dark Navy Sidebar Navigation -->
        <jsp:include page="/WEB-INF/views/includes/admin-sidebar.jsp">
            <jsp:param name="activeSection" value="classes" />
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

        <div class="flex-between mb-16" style="flex-wrap: wrap; gap: 1rem;">
            <div>
                <h2 class="font-bold text-2xl text-dark">Class and Batch Management</h2>
                <p class="text-xs text-muted">Create academic classes, organize student cohorts into batches, and link teachers</p>
            </div>
            <div class="flex gap-8">
                <button type="button" class="btn-secondary-action" onclick="openAddClassModal()">+ Add Class</button>
                <button type="button" class="btn-primary-action" onclick="openAddBatchModal()">+ Add Batch</button>
            </div>
        </div>

        <!-- Section 1: Academic Classes Table -->
        <div class="portal-table-container">
            <div class="table-header-bar">
                <div>
                    <h3 class="table-title">Academic Classes</h3>
                    <p class="table-subtitle">Primary cohorts and degree streams</p>
                </div>
                <span class="status-pill pill-neutral font-mono text-xs">Total: ${classes.size()}</span>
            </div>

            <div class="portal-table-responsive">
                <table class="portal-data-table">
                    <thead>
                        <tr>
                            <th>Class Code</th>
                            <th>Class Name</th>
                            <th>Department</th>
                            <th>Semester</th>
                            <th>Enrolled Students</th>
                            <th>Batches</th>
                            <th>Assigned Teachers</th>
                            <th>Status</th>
                            <th style="text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="c" items="${classes}">
                            <tr>
                                <td><span class="font-mono font-bold text-blue">${c.code}</span></td>
                                <td><span class="font-bold text-dark">${c.name}</span></td>
                                <td><span class="status-pill pill-neutral text-xs font-semibold">${c.department}</span></td>
                                <td>${c.semester}</td>
                                <td><span class="font-semibold text-dark">${c.studentCount}</span></td>
                                <td><span class="font-semibold text-dark">${c.batchCount}</span></td>
                                <td><span class="font-semibold text-dark">${c.teacherCount}</span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${c.active}">
                                            <span class="badge-status badge-present">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-status badge-absent">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: right;">
                                    <div class="flex justify-end gap-6" style="align-items: center;">
                                        <button type="button" class="btn-sm-action btn-secondary-action"
                                                onclick="openEditClassModal('${c.id}', '${c.code}', '${c.name}', '${c.department}', '${c.semester}')">
                                            Edit
                                        </button>
                                        <form action="${pageContext.request.contextPath}/admin/classes" method="post" style="display:inline; margin: 0;">
                                            <input type="hidden" name="action" value="toggle_class">
                                            <input type="hidden" name="id" value="${c.id}">
                                            <input type="hidden" name="active" value="${!c.active}">
                                            <button type="submit" class="btn-sm-action btn-secondary-action" style="${c.active ? 'color: var(--danger-red-start);' : ''}">
                                                ${c.active ? 'Deactivate' : 'Activate'}
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section 2: Batches Table -->
        <div class="portal-table-container">
            <div class="table-header-bar">
                <div>
                    <h3 class="table-title">Batches / Sub-Sections</h3>
                    <p class="table-subtitle">Lab groups and practical tutorial batches associated with classes</p>
                </div>
                <span class="status-pill pill-neutral font-mono text-xs">Total: ${batches.size()}</span>
            </div>

            <div class="portal-table-responsive">
                <table class="portal-data-table">
                    <thead>
                        <tr>
                            <th>Parent Class</th>
                            <th>Batch Code</th>
                            <th>Batch Name</th>
                            <th>Students Enrolled</th>
                            <th>Assigned Teachers</th>
                            <th>Status</th>
                            <th style="text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="b" items="${batches}">
                            <tr>
                                <td><span class="font-mono font-bold text-blue">${b.classCode}</span> (${b.className})</td>
                                <td><span class="font-mono font-bold text-dark">${b.code}</span></td>
                                <td>${b.name}</td>
                                <td><span class="font-semibold text-dark">${b.studentCount}</span></td>
                                <td><span class="font-semibold text-dark">${b.teacherCount}</span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${b.active}">
                                            <span class="badge-status badge-present">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-status badge-absent">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: right;">
                                    <div class="flex justify-end gap-6" style="align-items: center;">
                                        <button type="button" class="btn-sm-action btn-secondary-action"
                                                onclick="openEditBatchModal('${b.id}', '${b.classId}', '${b.code}', '${b.name}')">
                                            Edit
                                        </button>
                                        <form action="${pageContext.request.contextPath}/admin/classes" method="post" style="display:inline; margin: 0;">
                                            <input type="hidden" name="action" value="toggle_batch">
                                            <input type="hidden" name="id" value="${b.id}">
                                            <input type="hidden" name="active" value="${!b.active}">
                                            <button type="submit" class="btn-sm-action btn-secondary-action" style="${b.active ? 'color: var(--danger-red-start);' : ''}">
                                                ${b.active ? 'Deactivate' : 'Activate'}
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Section 3: Student-to-Batch Assignment Card -->
        <div class="card mb-16">
            <h3 class="font-bold text-dark text-base mb-8">Assign Student to Class &amp; Batch</h3>
            <p class="text-xs text-muted mb-16">Map student enrollments to specific academic classes and lab batches.</p>
            <form action="${pageContext.request.contextPath}/admin/classes" method="post" class="filter-grid">
                <input type="hidden" name="action" value="assign_student_batch">

                <div class="filter-field">
                    <label for="assignStudentId">Select Student</label>
                    <select name="studentId" id="assignStudentId" class="filter-control" required>
                        <c:forEach var="s" items="${students}">
                            <option value="${s.id}">${s.name} (${s.rollNo}) - Current: ${s.branch}-${s.section} / ${s.batchName != null ? s.batchName : 'Unassigned'}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="assignClassId">Select Class</label>
                    <select name="classId" id="assignClassId" class="filter-control" required>
                        <c:forEach var="c" items="${classes}">
                            <option value="${c.id}">${c.code} - ${c.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="assignBatchId">Select Batch (Optional)</label>
                    <select name="batchId" id="assignBatchId" class="filter-control">
                        <option value="">No Specific Batch (Class-wide)</option>
                        <c:forEach var="b" items="${batches}">
                            <option value="${b.id}">${b.classCode} - Batch ${b.code} (${b.name})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-field">
                    <button type="submit" class="btn-primary-action">Assign Student</button>
                </div>
            </form>
        </div>
            </main>

    <!-- Add Class Modal -->
    <div class="modal-overlay" id="addClassModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Create Academic Class</h3>
                <button type="button" onclick="closeAddClassModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/classes" method="post">
                <input type="hidden" name="action" value="create_class">
                <div class="modal-body">
                    <div class="filter-field">
                        <label for="addClassCode">Class Code <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="text" name="code" id="addClassCode" class="filter-control" placeholder="e.g. AI-A, CS-B, MECH-A" required>
                    </div>
                    <div class="filter-field">
                        <label for="addClassName">Class Name <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="text" name="name" id="addClassName" class="filter-control" placeholder="e.g. Artificial Intelligence - Section A" required>
                    </div>
                    <div class="filter-field">
                        <label for="addClassDept">Department <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="text" name="department" id="addClassDept" class="filter-control" placeholder="e.g. AI, CS, ECE" required>
                    </div>
                    <div class="filter-field">
                        <label for="addClassSem">Academic Semester / Year</label>
                        <input type="text" name="semester" id="addClassSem" class="filter-control" placeholder="e.g. 5th Semester, 3rd Year">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeAddClassModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action">Create Class</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Edit Class Modal -->
    <div class="modal-overlay" id="editClassModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Edit Academic Class</h3>
                <button type="button" onclick="closeEditClassModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/classes" method="post">
                <input type="hidden" name="action" value="edit_class">
                <input type="hidden" name="id" id="editClassId">
                <div class="modal-body">
                    <div class="filter-field">
                        <label for="editClassCode">Class Code</label>
                        <input type="text" name="code" id="editClassCode" class="filter-control" required>
                    </div>
                    <div class="filter-field">
                        <label for="editClassName">Class Name</label>
                        <input type="text" name="name" id="editClassName" class="filter-control" required>
                    </div>
                    <div class="filter-field">
                        <label for="editClassDept">Department</label>
                        <input type="text" name="department" id="editClassDept" class="filter-control" required>
                    </div>
                    <div class="filter-field">
                        <label for="editClassSem">Semester</label>
                        <input type="text" name="semester" id="editClassSem" class="filter-control">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeEditClassModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action">Save Changes</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Add Batch Modal -->
    <div class="modal-overlay" id="addBatchModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Create Batch</h3>
                <button type="button" onclick="closeAddBatchModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/classes" method="post">
                <input type="hidden" name="action" value="create_batch">
                <div class="modal-body">
                    <div class="filter-field">
                        <label for="addBatchClassId">Parent Class <span style="color:var(--danger-red-start)">*</span></label>
                        <select name="classId" id="addBatchClassId" class="filter-control" required>
                            <c:forEach var="c" items="${classes}">
                                <option value="${c.id}">${c.code} - ${c.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="filter-field">
                        <label for="addBatchCode">Batch Code <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="text" name="code" id="addBatchCode" class="filter-control" placeholder="e.g. A1, A2, B1, Lab-Group-1" required>
                    </div>
                    <div class="filter-field">
                        <label for="addBatchName">Batch Name <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="text" name="name" id="addBatchName" class="filter-control" placeholder="e.g. AI Practical Batch A1" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeAddBatchModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action">Create Batch</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Edit Batch Modal -->
    <div class="modal-overlay" id="editBatchModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Edit Batch</h3>
                <button type="button" onclick="closeEditBatchModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/classes" method="post">
                <input type="hidden" name="action" value="edit_batch">
                <input type="hidden" name="id" id="editBatchId">
                <div class="modal-body">
                    <div class="filter-field">
                        <label for="editBatchClassId">Parent Class</label>
                        <select name="classId" id="editBatchClassId" class="filter-control" required>
                            <c:forEach var="c" items="${classes}">
                                <option value="${c.id}">${c.code} - ${c.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="filter-field">
                        <label for="editBatchCode">Batch Code</label>
                        <input type="text" name="code" id="editBatchCode" class="filter-control" required>
                    </div>
                    <div class="filter-field">
                        <label for="editBatchName">Batch Name</label>
                        <input type="text" name="name" id="editBatchName" class="filter-control" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeEditBatchModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action">Save Changes</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openAddClassModal() { document.getElementById('addClassModal').classList.add('active'); }
        function closeAddClassModal() { document.getElementById('addClassModal').classList.remove('active'); }

        function openEditClassModal(id, code, name, dept, sem) {
            document.getElementById('editClassId').value = id;
            document.getElementById('editClassCode').value = code;
            document.getElementById('editClassName').value = name;
            document.getElementById('editClassDept').value = dept;
            document.getElementById('editClassSem').value = sem;
            document.getElementById('editClassModal').classList.add('active');
        }
        function closeEditClassModal() { document.getElementById('editClassModal').classList.remove('active'); }

        function openAddBatchModal() { document.getElementById('addBatchModal').classList.add('active'); }
        function closeAddBatchModal() { document.getElementById('addBatchModal').classList.remove('active'); }

        function openEditBatchModal(id, classId, code, name) {
            document.getElementById('editBatchId').value = id;
            document.getElementById('editBatchClassId').value = classId;
            document.getElementById('editBatchCode').value = code;
            document.getElementById('editBatchName').value = name;
            document.getElementById('editBatchModal').classList.add('active');
        }
        function closeEditBatchModal() { document.getElementById('editBatchModal').classList.remove('active'); }

        window.onclick = function(event) {
            if (event.target === document.getElementById('addClassModal')) closeAddClassModal();
            if (event.target === document.getElementById('editClassModal')) closeEditClassModal();
            if (event.target === document.getElementById('addBatchModal')) closeAddBatchModal();
            if (event.target === document.getElementById('editBatchModal')) closeEditBatchModal();
        };
    </script>

    <!-- Reusable Modals (Reports, Settings, Notifications) -->
    <jsp:include page="/WEB-INF/views/includes/admin-modals.jsp" />
        </div>
    </div>

    <!-- Admin Portal Script -->
    <script src="${pageContext.request.contextPath}/js/admin-portal.js"></script>
</body>
</html>
