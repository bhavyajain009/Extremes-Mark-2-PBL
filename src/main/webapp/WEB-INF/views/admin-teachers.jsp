<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extremis - Teacher Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-portal.css">
</head>
<body class="admin-body">

    <div class="admin-layout">
        <!-- Left Dark Navy Sidebar Navigation -->
        <jsp:include page="/WEB-INF/views/includes/admin-sidebar.jsp">
            <jsp:param name="activeSection" value="teachers" />
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
                <h2 class="font-bold text-2xl text-dark">Teacher Management</h2>
                <p class="text-xs text-muted">Create teachers, manage departments, toggle status, and inspect assigned classes</p>
            </div>
            <button type="button" class="btn-primary-action" onclick="openAddTeacherModal()">
                + Add New Teacher
            </button>
        </div>

        <!-- Filter Card -->
        <div class="filter-card">
            <form action="${pageContext.request.contextPath}/admin/teachers" method="get" class="filter-grid">
                <div class="filter-field">
                    <label for="searchQuery">Search Teacher</label>
                    <input type="text" name="q" id="searchQuery" class="filter-control" placeholder="Search by name, ID, or email..." value="${searchQuery}">
                </div>

                <div class="filter-field">
                    <label for="filterDept">Department</label>
                    <select name="dept" id="filterDept" class="filter-control">
                        <option value="ALL" ${selectedDept == 'ALL' ? 'selected' : ''}>All Departments</option>
                        <option value="AI" ${selectedDept == 'AI' ? 'selected' : ''}>Artificial Intelligence</option>
                        <option value="CS" ${selectedDept == 'CS' ? 'selected' : ''}>Computer Science</option>
                        <option value="ECE" ${selectedDept == 'ECE' ? 'selected' : ''}>Electronics &amp; Comm</option>
                    </select>
                </div>

                <div class="filter-field">
                    <label for="filterStatus">Account Status</label>
                    <select name="status" id="filterStatus" class="filter-control">
                        <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                        <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Active Only</option>
                        <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>Inactive Only</option>
                    </select>
                </div>

                <div class="filter-field" style="display: flex; gap: 0.5rem; flex-direction: row;">
                    <button type="submit" class="btn-primary-action" style="flex: 1;">Search</button>
                    <a href="${pageContext.request.contextPath}/admin/teachers" class="btn-secondary-action">Clear</a>
                </div>
            </form>
        </div>

        <!-- Teachers Table -->
        <div class="portal-table-container">
            <div class="table-header-bar">
                <div>
                    <h3 class="table-title">Registered Faculty Members</h3>
                    <p class="table-subtitle">Total: ${teachers.size()} teachers registered</p>
                </div>
            </div>

            <div class="portal-table-responsive">
                <table class="portal-data-table">
                    <thead>
                        <tr>
                            <th>Teacher ID</th>
                            <th>Full Name</th>
                            <th>Email Address</th>
                            <th>Department</th>
                            <th>Assigned Classes &amp; Batches</th>
                            <th>Status</th>
                            <th style="text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty teachers}">
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                                        No teachers found matching your search.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="t" items="${teachers}">
                                    <tr>
                                        <td><span class="font-mono font-bold text-blue">${t.facultyId}</span></td>
                                        <td>
                                            <div class="flex items-center gap-8">
                                                <div class="step-icon-circle bg-blue-light text-blue" style="width: 32px; height: 32px; font-weight: 700; font-size: 0.8rem;">
                                                    ${t.name.substring(0, 1)}
                                                </div>
                                                <span class="font-bold text-dark">${t.name}</span>
                                            </div>
                                        </td>
                                        <td><span class="text-xs text-muted font-mono">${t.email}</span></td>
                                        <td><span class="status-pill pill-neutral text-xs font-semibold">${t.department != null ? t.department : t.branch}</span></td>
                                        <td>
                                            <c:set var="assigns" value="${teacherAssignmentsMap[t.id]}" />
                                            <c:choose>
                                                <c:when test="${not empty assigns}">
                                                    <div class="flex flex-col gap-2">
                                                        <c:forEach var="a" items="${assigns}">
                                                            <span class="badge-status badge-excused" style="font-size: 0.7rem;">
                                                                ${a.classCode} &bull; ${a.batchCode != null ? a.batchCode : 'All Batches'} (${a.subject})
                                                            </span>
                                                        </c:forEach>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-xs text-muted">Legacy: ${t.branch}-${t.section}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${t.active}">
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
                                                        onclick="openEditTeacherModal('${t.id}', '${t.facultyId}', '${t.name}', '${t.email}', '${t.department}')">
                                                    Edit
                                                </button>

                                                <form action="${pageContext.request.contextPath}/admin/teachers" method="post" style="display:inline; margin: 0;">
                                                    <input type="hidden" name="action" value="toggle_status">
                                                    <input type="hidden" name="id" value="${t.id}">
                                                    <input type="hidden" name="active" value="${!t.active}">
                                                    <button type="submit" class="btn-sm-action ${t.active ? 'btn-secondary-action' : 'btn-primary-action'}"
                                                            style="${t.active ? 'color: var(--danger-red-start);' : ''}">
                                                        ${t.active ? 'Deactivate' : 'Activate'}
                                                    </button>
                                                </form>

                                                <a href="${pageContext.request.contextPath}/admin/assignments" class="btn-sm-action btn-primary-action" title="Assign to Class">
                                                    Assign
                                                </a>
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
            </main>

    <!-- Add Teacher Modal -->
    <div class="modal-overlay" id="addTeacherModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Add New Teacher</h3>
                <button type="button" onclick="closeAddTeacherModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/teachers" method="post">
                <input type="hidden" name="action" value="create">
                <div class="modal-body">
                    <div class="filter-field">
                        <label for="addFacultyId">Teacher Employee ID <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="text" name="facultyId" id="addFacultyId" class="filter-control" placeholder="e.g. FAC002, TCH101" required>
                    </div>

                    <div class="filter-field">
                        <label for="addName">Full Name <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="text" name="name" id="addName" class="filter-control" placeholder="e.g. Dr. Jane Smith" required>
                    </div>

                    <div class="filter-field">
                        <label for="addEmail">Email Address <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="email" name="email" id="addEmail" class="filter-control" placeholder="e.g. jane.smith@extremis.edu" required>
                    </div>

                    <div class="filter-field">
                        <label for="addDept">Department <span style="color:var(--danger-red-start)">*</span></label>
                        <select name="department" id="addDept" class="filter-control" required>
                            <option value="AI">Artificial Intelligence (AI)</option>
                            <option value="CS">Computer Science (CS)</option>
                            <option value="ECE">Electronics &amp; Communication (ECE)</option>
                        </select>
                    </div>

                    <div class="filter-field">
                        <label for="addPassword">Initial Password <span style="color:var(--danger-red-start)">*</span></label>
                        <input type="password" name="password" id="addPassword" class="filter-control" placeholder="Secure password for teacher login" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeAddTeacherModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action">Create Teacher</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Edit Teacher Modal -->
    <div class="modal-overlay" id="editTeacherModal">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Edit Teacher Profile</h3>
                <button type="button" onclick="closeEditTeacherModal()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:var(--text-muted);">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/teachers" method="post">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="id" id="editTeacherId">
                <div class="modal-body">
                    <div class="filter-field">
                        <label>Teacher ID</label>
                        <input type="text" id="editFacultyIdDisplay" class="filter-control" disabled style="background:#F1F5F9;">
                    </div>

                    <div class="filter-field">
                        <label for="editName">Full Name</label>
                        <input type="text" name="name" id="editName" class="filter-control" required>
                    </div>

                    <div class="filter-field">
                        <label for="editEmail">Email Address</label>
                        <input type="email" name="email" id="editEmail" class="filter-control" required>
                    </div>

                    <div class="filter-field">
                        <label for="editDept">Department</label>
                        <select name="department" id="editDept" class="filter-control">
                            <option value="AI">Artificial Intelligence (AI)</option>
                            <option value="CS">Computer Science (CS)</option>
                            <option value="ECE">Electronics &amp; Communication (ECE)</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary-action" onclick="closeEditTeacherModal()">Cancel</button>
                    <button type="submit" class="btn-primary-action">Save Changes</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openAddTeacherModal() {
            document.getElementById('addTeacherModal').classList.add('active');
        }
        function closeAddTeacherModal() {
            document.getElementById('addTeacherModal').classList.remove('active');
        }

        function openEditTeacherModal(id, facultyId, name, email, department) {
            document.getElementById('editTeacherId').value = id;
            document.getElementById('editFacultyIdDisplay').value = facultyId;
            document.getElementById('editName').value = name;
            document.getElementById('editEmail').value = email;
            if (department) document.getElementById('editDept').value = department;
            document.getElementById('editTeacherModal').classList.add('active');
        }
        function closeEditTeacherModal() {
            document.getElementById('editTeacherModal').classList.remove('active');
        }

        window.onclick = function(event) {
            if (event.target === document.getElementById('addTeacherModal')) closeAddTeacherModal();
            if (event.target === document.getElementById('editTeacherModal')) closeEditTeacherModal();
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
