<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Extremis - College Attendance System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/logo.png">
</head>
<body class="bg-light flex items-center justify-center min-h-screen">

<div class="mobile-container login-wrapper">

    <!-- Top Logo & Brand Area -->
    <div class="login-brand-section text-center pt-32 pb-20">
        <div class="mx-auto mb-12" style="width: 72px; height: 72px; display: flex; align-items: center; justify-content: center;">
            <img src="${pageContext.request.contextPath}/images/logo.png" alt="Extremis Logo" style="width: 72px; height: 72px; border-radius: 18px; box-shadow: 0 8px 24px rgba(29, 100, 242, 0.25); object-fit: cover;">
        </div>
        <h1 class="brand-title font-bold text-2xl text-dark">Extremis</h1>
        <p class="brand-subtitle text-xs text-muted mt-2">Smart College Attendance System</p>
    </div>

    <!-- Main Login Card -->
    <div class="card login-card mx-16 p-24">

        <!-- Role Selector Segmented Controls -->
        <div class="role-segmented-control mb-20">
            <button type="button" class="role-btn ${selectedRole == 'faculty' ? '' : (selectedRole == 'admin' ? '' : 'active')}" id="roleStudent" onclick="selectRole('student')">Student</button>
            <button type="button" class="role-btn ${selectedRole == 'faculty' ? 'active' : ''}" id="roleFaculty" onclick="selectRole('faculty')">Faculty</button>
            <button type="button" class="role-btn ${selectedRole == 'admin' ? 'active' : ''}" id="roleAdmin" onclick="selectRole('admin')">Admin</button>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger mb-16">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="15" y1="9" x2="9" y2="15"/>
                    <line x1="9" y1="9" x2="15" y2="15"/>
                </svg>
                <span>${errorMessage}</span>
            </div>
        </c:if>

        <c:if test="${not empty sessionScope.sessionError or not empty sessionScope.flashError}">
            <div class="alert alert-warning mb-16">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                    <line x1="12" y1="9" x2="12" y2="13"/>
                    <line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
                <span>${not empty sessionScope.sessionError ? sessionScope.sessionError : sessionScope.flashError}</span>
            </div>
            <% session.removeAttribute("sessionError"); session.removeAttribute("flashError"); %>
        </c:if>

        <!-- Login Form -->
        <form method="post" action="${pageContext.request.contextPath}/login" id="loginForm">
            <input type="hidden" name="role" id="selectedRoleInput" value="${selectedRole != null ? selectedRole : 'student'}">
            <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">

            <div class="form-group mb-16">
                <label class="form-label" for="rollNo" id="identifierLabel">Student ID / RTU Roll No</label>
                <div class="input-with-icon">
                    <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                    </svg>
                    <input type="text" id="rollNo" name="rollNo" class="form-input" placeholder="e.g. 24EARAD031" value="${rollNo != null ? rollNo : '24EARAD031'}" required autocomplete="username">
                </div>
            </div>

            <div class="form-group mb-20">
                <label class="form-label" for="password">Password</label>
                <div class="input-with-icon">
                    <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                    <input type="password" id="password" name="password" class="form-input" placeholder="Enter password" value="StudentPassword123!" required autocomplete="current-password">
                </div>
            </div>

            <button type="submit" class="btn btn-primary w-full py-14 font-semibold text-base" id="btnLoginSubmit">
                Sign In to Extremis
            </button>
        </form>

        <!-- Demo credentials info card -->
        <div class="demo-creds-box mt-20 p-12 bg-light rounded-xl">
            <p class="font-bold text-xs text-dark mb-6">Demo Credentials (Tap to Auto-fill):</p>
            
            <div class="mb-6 pb-6 border-b border-gray-200">
                <a href="javascript:void(0)" onclick="fillCreds('student', '24EARAD031', 'StudentPassword123!')" class="text-xs link-blue font-bold">
                    👨‍🎓 Demo Student: 24EARAD031
                </a>
                <p class="text-xs text-muted">Bhavya Kumar Bohra (AI-A) &bull; Pass: <code>StudentPassword123!</code></p>
            </div>

            <div class="mb-6 pb-6 border-b border-gray-200">
                <a href="javascript:void(0)" onclick="fillCreds('student', '24EARAD001', 'StudentPassword123!')" class="text-xs link-blue font-bold">
                    👨‍🎓 Student 1: 24EARAD001
                </a>
                <p class="text-xs text-muted">Aarav Sharma (AI-A) &bull; Pass: <code>StudentPassword123!</code></p>
            </div>

            <div class="mb-6 pb-6 border-b border-gray-200">
                <a href="javascript:void(0)" onclick="fillCreds('faculty', 'FAC001', 'FacultyPassword123!')" class="text-xs link-blue font-bold">
                    👩‍🏫 Faculty (AI-A): FAC001
                </a>
                <p class="text-xs text-muted">Dr. Rajesh Sharma &bull; Pass: <code>FacultyPassword123!</code></p>
            </div>

            <div>
                <a href="javascript:void(0)" onclick="fillCreds('admin', 'admin', 'AdminPassword123!')" class="text-xs link-blue font-bold">
                    🛡️ Administrator: admin
                </a>
                <p class="text-xs text-muted">Chief Administrator &bull; Pass: <code>AdminPassword123!</code></p>
            </div>
        </div>

    </div>

    <!-- Security & device note -->
    <div class="text-center px-24 pt-16 pb-32">
        <p class="text-xs text-muted flex items-center justify-center gap-4">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            Single-Device Session Protected
        </p>
    </div>

</div>

<script>
    function selectRole(role) {
        document.querySelectorAll('.role-btn').forEach(btn => btn.classList.remove('active'));
        const roleInput = document.getElementById('selectedRoleInput');
        const idLabel = document.getElementById('identifierLabel');
        const idInput = document.getElementById('rollNo');
        const passInput = document.getElementById('password');

        roleInput.value = role;

        if (role === 'student') {
            document.getElementById('roleStudent').classList.add('active');
            idLabel.innerText = "Student ID / RTU Roll No";
            idInput.placeholder = "e.g. 24EARAD031";
            idInput.value = "24EARAD031";
            passInput.value = "StudentPassword123!";
        } else if (role === 'faculty') {
            document.getElementById('roleFaculty').classList.add('active');
            idLabel.innerText = "Faculty ID";
            idInput.placeholder = "e.g. FAC001";
            idInput.value = "FAC001";
            passInput.value = "FacultyPassword123!";
        } else if (role === 'admin') {
            document.getElementById('roleAdmin').classList.add('active');
            idLabel.innerText = "Administrator Username";
            idInput.placeholder = "e.g. admin";
            idInput.value = "admin";
            passInput.value = "AdminPassword123!";
        }
    }

    function fillCreds(role, id, pass) {
        selectRole(role);
        document.getElementById('rollNo').value = id;
        document.getElementById('password').value = pass;
    }
</script>
</body>
</html>
