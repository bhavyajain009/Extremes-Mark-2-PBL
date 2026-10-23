<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="admin-header">
    <div class="admin-header-left">
        <!-- Mobile Drawer Hamburger Toggle -->
        <button class="admin-menu-toggle" onclick="toggleAdminSidebar()" aria-label="Toggle Navigation">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
        </button>

        <!-- Search Bar with Live Filter -->
        <div class="admin-search-box">
            <svg class="admin-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input type="text" 
                   id="adminGlobalSearch" 
                   class="admin-search-input" 
                   placeholder="Search students, teachers, classes..." 
                   oninput="handleAdminGlobalSearch(this.value)"
                   autocomplete="off">
        </div>
    </div>

    <div class="admin-header-right">
        <!-- Notification Bell Icon Button -->
        <button class="admin-notif-btn" onclick="toggleAdminNotifications()" title="Notifications" aria-label="Notifications">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            <span class="admin-notif-badge"></span>
        </button>

        <!-- Administrator Profile Badge -->
        <div class="admin-profile-container">
            <div class="admin-profile-badge" onclick="toggleAdminProfileMenu()">
                <div class="admin-avatar">
                    <c:choose>
                        <c:when test="${not empty admin.name}">
                            ${admin.name.substring(0, 1).toUpperCase()}
                        </c:when>
                        <c:otherwise>A</c:otherwise>
                    </c:choose>
                </div>
                <div class="admin-profile-meta">
                    <span class="admin-profile-name">
                        <c:choose>
                            <c:when test="${not empty admin.name}">${admin.name}</c:when>
                            <c:otherwise>Chief Administrator</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="admin-profile-role">Administrator</span>
                </div>
                <svg class="admin-profile-chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
            </div>

            <!-- Profile Dropdown Menu -->
            <div class="admin-dropdown-menu" id="adminProfileDropdown">
                <div class="admin-dropdown-header">
                    <div style="font-weight: 700; font-size: 0.88rem; color: #0F172A;">${admin.name}</div>
                    <div style="font-size: 0.72rem; color: #64748B;">@${admin.username}</div>
                </div>
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="admin-dropdown-item">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                    <span>Dashboard Home</span>
                </a>
                <button type="button" onclick="openAdminSettingsModal()" class="admin-dropdown-item">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
                    <span>System Settings</span>
                </button>
                <button type="button" onclick="openAdminReportsModal()" class="admin-dropdown-item">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>
                    <span>Attendance Reports</span>
                </button>
                <div style="height: 1px; background: #F1F5F9; margin: 0.35rem 0;"></div>
                <form action="${pageContext.request.contextPath}/logout" method="post" style="margin: 0;">
                    <button type="submit" class="admin-dropdown-item danger">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                        <span>Sign Out</span>
                    </button>
                </form>
            </div>
        </div>
    </div>
</header>
