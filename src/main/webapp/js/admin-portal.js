/**
 * Extremis Admin Control Center JavaScript
 * Handles real-time clock, dropdowns, modals, mobile drawer, search filter, and section transitions.
 */

document.addEventListener('DOMContentLoaded', function () {
    initAdminClock();
    initProfileDropdownListener();
});

// 1. Real-Time Date & Time Clock (Asia/Kolkata)
function initAdminClock() {
    var dateEl = document.getElementById('adminCurrentDate');
    var timeEl = document.getElementById('adminCurrentTime');
    if (!dateEl && !timeEl) return;

    function updateClock() {
        var now = new Date();
        // Format options for Asia/Kolkata
        try {
            var dateOpts = {
                timeZone: 'Asia/Kolkata',
                weekday: 'long',
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            };
            var timeOpts = {
                timeZone: 'Asia/Kolkata',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: true
            };

            var formattedDate = new Intl.DateTimeFormat('en-IN', dateOpts).format(now);
            var formattedTime = new Intl.DateTimeFormat('en-IN', timeOpts).format(now);

            if (dateEl) dateEl.textContent = formattedDate;
            if (timeEl) timeEl.textContent = formattedTime + ' | Asia/Kolkata';
        } catch (e) {
            // Fallback
            if (dateEl) dateEl.textContent = now.toDateString();
            if (timeEl) timeEl.textContent = now.toLocaleTimeString() + ' | Asia/Kolkata';
        }
    }

    updateClock();
    setInterval(updateClock, 1000);
}

// 2. Profile Dropdown Toggle
function toggleAdminProfileMenu() {
    var dd = document.getElementById('adminProfileDropdown');
    if (dd) {
        dd.classList.toggle('show');
    }
}

function initProfileDropdownListener() {
    document.addEventListener('click', function (e) {
        var container = document.querySelector('.admin-profile-container');
        var dd = document.getElementById('adminProfileDropdown');
        if (container && dd && !container.contains(e.target)) {
            dd.classList.remove('show');
        }
    });
}

// 3. Mobile Sidebar Drawer Controls
function toggleAdminSidebar() {
    var sidebar = document.getElementById('adminSidebar');
    var overlay = document.getElementById('adminDrawerOverlay');
    if (sidebar && overlay) {
        sidebar.classList.toggle('open');
        overlay.classList.toggle('open');
    }
}

function closeAdminSidebar() {
    var sidebar = document.getElementById('adminSidebar');
    var overlay = document.getElementById('adminDrawerOverlay');
    if (sidebar && overlay) {
        sidebar.classList.remove('open');
        overlay.classList.remove('open');
    }
}

// 4. Global Search Filter
function handleAdminGlobalSearch(query) {
    var q = (query || '').trim().toLowerCase();
    
    // Filter rows in any active admin data table
    var tables = document.querySelectorAll('.admin-table tbody, .portal-data-table tbody');
    tables.forEach(function (tbody) {
        var rows = tbody.querySelectorAll('tr');
        rows.forEach(function (row) {
            var text = row.textContent.toLowerCase();
            if (!q || text.indexOf(q) !== -1) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });

    // Also filter cards with searchable text if present
    var cards = document.querySelectorAll('.admin-searchable-card');
    cards.forEach(function (card) {
        var text = card.textContent.toLowerCase();
        if (!q || text.indexOf(q) !== -1) {
            card.style.display = '';
        } else {
            card.style.display = 'none';
        }
    });
}

// 5. Modals (Reports, Settings, Notifications)
function openAdminReportsModal() {
    var m = document.getElementById('adminReportsModal');
    if (m) m.classList.add('show');
    closeAdminSidebar();
}
function closeAdminReportsModal() {
    var m = document.getElementById('adminReportsModal');
    if (m) m.classList.remove('show');
}

function openAdminSettingsModal() {
    var m = document.getElementById('adminSettingsModal');
    if (m) m.classList.add('show');
    closeAdminSidebar();
}
function closeAdminSettingsModal() {
    var m = document.getElementById('adminSettingsModal');
    if (m) m.classList.remove('show');
}

function toggleAdminNotifications() {
    var m = document.getElementById('adminNotifModal');
    if (m) m.classList.toggle('show');
}
function closeAdminNotifModal() {
    var m = document.getElementById('adminNotifModal');
    if (m) m.classList.remove('show');
}

function closeModalOnBackdrop(event, modalId) {
    if (event.target.id === modalId) {
        var m = document.getElementById(modalId);
        if (m) m.classList.remove('show');
    }
}

// 6. Smooth Section Navigation (SPA Enhancement without breaking routes)
document.addEventListener('click', function (e) {
    var navLink = e.target.closest('.admin-nav-item');
    if (!navLink) return;

    var href = navLink.getAttribute('href');
    if (!href || href.startsWith('javascript:') || href === '#') return;

    // Only apply SPA transition for internal admin GET routes
    if (href.indexOf('/admin/') !== -1 && !e.ctrlKey && !e.metaKey && !e.shiftKey) {
        e.preventDefault();
        navigateToAdminSection(href, navLink);
    }
});

function navigateToAdminSection(url, targetNavLink) {
    // Show smooth exit transition
    var mainWrapper = document.querySelector('.admin-main-wrapper');
    if (!mainWrapper) {
        window.location.href = url;
        return;
    }

    mainWrapper.style.opacity = '0.5';
    mainWrapper.style.transition = 'opacity 0.15s ease';

    fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(function (res) {
            if (!res.ok) throw new Error('Network error: ' + res.status);
            return res.text();
        })
        .then(function (html) {
            var parser = new DOMParser();
            var doc = parser.parseFromString(html, 'text/html');

            var newContent = doc.querySelector('.admin-content-area');
            var currentContent = document.querySelector('.admin-content-area');

            if (newContent && currentContent) {
                currentContent.innerHTML = newContent.innerHTML;
                document.title = doc.title;
                history.pushState(null, doc.title, url);

                // Update active nav item
                document.querySelectorAll('.admin-nav-item').forEach(function (el) {
                    el.classList.remove('active');
                });
                if (targetNavLink) {
                    targetNavLink.classList.add('active');
                } else {
                    var matching = document.querySelector('.admin-nav-item[href="' + url + '"]');
                    if (matching) matching.classList.add('active');
                }

                // Scroll to top of content
                window.scrollTo({ top: 0, behavior: 'smooth' });
                initAdminClock();
            } else {
                window.location.href = url;
            }
        })
        .catch(function (err) {
            // Fallback to standard HTTP navigation on any error
            window.location.href = url;
        })
        .finally(function () {
            mainWrapper.style.opacity = '1';
            closeAdminSidebar();
        });
}

// Handle browser Back / Forward buttons
window.addEventListener('popstate', function () {
    window.location.reload();
});
