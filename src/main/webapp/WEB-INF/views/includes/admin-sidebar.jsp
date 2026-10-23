<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Mobile Drawer Backdrop Overlay -->
<div class="admin-drawer-overlay" id="adminDrawerOverlay" onclick="closeAdminSidebar()"></div>

<!-- Fixed Dark Navy-Blue Left Sidebar -->
<aside class="admin-sidebar" id="adminSidebar">
    <!-- Brand Header -->
    <div class="admin-brand">
        <img src="${pageContext.request.contextPath}/images/logo.png" alt="Extremis Logo" class="admin-brand-logo-img">
        <div class="admin-brand-info">
            <span class="admin-brand-title">Extremis</span>
            <span class="admin-brand-subtitle">ADMIN CONTROL CENTER</span>
        </div>
    </div>

    <!-- Navigation Menu Items -->
    <nav class="admin-nav">
        <!-- 1. Dashboard -->
        <a href="${pageContext.request.contextPath}/admin/dashboard" 
           class="admin-nav-item ${activeSection == 'dashboard' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                        <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
                    </svg>
                </span>
                <span>Dashboard</span>
            </div>
        </a>

        <!-- 2. Attendance Timers -->
        <a href="${pageContext.request.contextPath}/admin/timers" 
           class="admin-nav-item ${activeSection == 'timers' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                </span>
                <span>Attendance Timers</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 3. Random Verification -->
        <a href="${pageContext.request.contextPath}/admin/randomizer" 
           class="admin-nav-item ${activeSection == 'randomizer' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                        <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                    </svg>
                </span>
                <span>Random Verification</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 4. Students -->
        <a href="${pageContext.request.contextPath}/admin/students" 
           class="admin-nav-item ${activeSection == 'students' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                    </svg>
                </span>
                <span>Students</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 5. Teachers -->
        <a href="${pageContext.request.contextPath}/admin/teachers" 
           class="admin-nav-item ${activeSection == 'teachers' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                </span>
                <span>Teachers</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 6. Classes & Batches -->
        <a href="${pageContext.request.contextPath}/admin/classes" 
           class="admin-nav-item ${activeSection == 'classes' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
                        <polyline points="2 17 12 22 22 17"></polyline>
                        <polyline points="2 12 12 17 22 12"></polyline>
                    </svg>
                </span>
                <span>Classes &amp; Batches</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 7. Assignments -->
        <a href="${pageContext.request.contextPath}/admin/assignments" 
           class="admin-nav-item ${activeSection == 'assignments' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"></path>
                        <rect x="8" y="2" width="8" height="4" rx="1" ry="1"></rect>
                    </svg>
                </span>
                <span>Assignments</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 8. Override History -->
        <a href="${pageContext.request.contextPath}/admin/overrides" 
           class="admin-nav-item ${activeSection == 'overrides' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="1 4 1 10 7 10"></polyline>
                        <polyline points="23 20 23 14 17 14"></polyline>
                        <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"></path>
                    </svg>
                </span>
                <span>Override History</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 9. Reports -->
        <a href="javascript:void(0)" onclick="openAdminReportsModal()" 
           class="admin-nav-item ${activeSection == 'reports' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="18" y1="20" x2="18" y2="10"></line>
                        <line x1="12" y1="20" x2="12" y2="4"></line>
                        <line x1="6" y1="20" x2="6" y2="14"></line>
                    </svg>
                </span>
                <span>Reports</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>

        <!-- 10. Settings -->
        <a href="javascript:void(0)" onclick="openAdminSettingsModal()" 
           class="admin-nav-item ${activeSection == 'settings' ? 'active' : ''}">
            <div class="admin-nav-left">
                <span class="admin-nav-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="3"></circle>
                        <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                    </svg>
                </span>
                <span>Settings</span>
            </div>
            <span class="admin-nav-chevron">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
            </span>
        </a>
    </nav>

    <!-- Sidebar Bottom Logout Action -->
    <div class="admin-sidebar-footer">
        <form action="${pageContext.request.contextPath}/logout" method="post" style="margin: 0;">
            <button type="submit" class="admin-logout-btn">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                <span>Sign Out</span>
            </button>
        </form>
    </div>
</aside>
