<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Reports Modal -->
<div class="admin-modal-backdrop" id="adminReportsModal" onclick="closeModalOnBackdrop(event, 'adminReportsModal')">
    <div class="admin-modal-card">
        <div class="admin-modal-header">
            <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 1.25rem;">📊</span>
                <h3 class="admin-modal-title">Attendance &amp; System Analytics Report</h3>
            </div>
            <button type="button" class="admin-modal-close" onclick="closeAdminReportsModal()">&times;</button>
        </div>
        <div class="admin-modal-body">
            <p class="text-xs text-muted" style="margin-top: 0;">Comprehensive administrative attendance statistics and geofence verification summary.</p>
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px;">
                <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px;">
                    <div style="font-size: 0.72rem; font-weight: 700; color: #64748B; text-transform: uppercase;">Total Active Students</div>
                    <div style="font-size: 1.5rem; font-weight: 800; color: #1D64F2; margin-top: 2px;">${totalStudents}</div>
                </div>
                <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px;">
                    <div style="font-size: 0.72rem; font-weight: 700; color: #64748B; text-transform: uppercase;">Today Present / Check-Ins</div>
                    <div style="font-size: 1.5rem; font-weight: 800; color: #16A34A; margin-top: 2px;">${todayCheckIns}</div>
                </div>
            </div>

            <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 14px; margin-bottom: 16px;">
                <div style="font-weight: 700; font-size: 0.85rem; color: #0F172A; margin-bottom: 8px;">Random GPS Verification Breakdown</div>
                <div style="display: flex; justify-content: space-between; font-size: 0.8rem; padding: 4px 0; border-bottom: 1px solid #EDF2F7;">
                    <span style="color: #64748B;">Total Scheduled Checks</span>
                    <span style="font-weight: 700; color: #0F172A;">${totalLocationChecks}</span>
                </div>
                <div style="display: flex; justify-content: space-between; font-size: 0.8rem; padding: 4px 0; border-bottom: 1px solid #EDF2F7;">
                    <span style="color: #64748B;">Verified On Campus (&lt;1000m)</span>
                    <span style="font-weight: 700; color: #16A34A;">${verifiedCampusCount}</span>
                </div>
                <div style="display: flex; justify-content: space-between; font-size: 0.8rem; padding: 4px 0; border-bottom: 1px solid #EDF2F7;">
                    <span style="color: #64748B;">Outside Geofence (Exceptions)</span>
                    <span style="font-weight: 700; color: #DC2626;">${outsideGeofenceCount}</span>
                </div>
                <div style="display: flex; justify-content: space-between; font-size: 0.8rem; padding: 4px 0;">
                    <span style="color: #64748B;">Pending / Scheduled Checks</span>
                    <span style="font-weight: 700; color: #0284C7;">${pendingChecksCount}</span>
                </div>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <button type="button" class="btn-sm-action" style="background: #E2E8F0; color: #334155; border: none; padding: 8px 16px; border-radius: 8px; font-weight: 700; cursor: pointer;" onclick="closeAdminReportsModal()">
                    Close
                </button>
                <a href="${pageContext.request.contextPath}/admin/overrides" class="btn-sm-action" style="background: #1D64F2; color: #FFFFFF; text-decoration: none; padding: 8px 16px; border-radius: 8px; font-weight: 700;">
                    View Override Audit Trail &rarr;
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Settings Modal -->
<div class="admin-modal-backdrop" id="adminSettingsModal" onclick="closeModalOnBackdrop(event, 'adminSettingsModal')">
    <div class="admin-modal-card">
        <div class="admin-modal-header">
            <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 1.25rem;">⚙️</span>
                <h3 class="admin-modal-title">Administrative System Settings</h3>
            </div>
            <button type="button" class="admin-modal-close" onclick="closeAdminSettingsModal()">&times;</button>
        </div>
        <div class="admin-modal-body">
            <div style="margin-bottom: 16px;">
                <label style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: #64748B; display: block; margin-bottom: 4px;">Logged Administrator</label>
                <div style="font-weight: 700; font-size: 0.95rem; color: #0F172A;">${admin.name} (@${admin.username})</div>
            </div>

            <div style="background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 14px; margin-bottom: 16px;">
                <div style="font-weight: 700; font-size: 0.85rem; color: #0F172A; margin-bottom: 8px;">Attendance Policy Standards</div>
                <div style="font-size: 0.78rem; color: #475569; line-height: 1.5;">
                    <div>• <strong>Morning Check-In Window:</strong> 8:50 AM &ndash; 9:05 AM</div>
                    <div>• <strong>Random Verification Active Window:</strong> 9:05 AM &ndash; 3:40 PM</div>
                    <div>• <strong>Lunch Break Exclusion:</strong> 12:20 PM &ndash; 1:10 PM</div>
                    <div>• <strong>Departure Check-Out Window:</strong> 3:40 PM Onwards</div>
                    <div>• <strong>Campus Geofence Radius:</strong> 1,000 meters tolerance</div>
                </div>
            </div>

            <div style="background: #EFF6FF; border: 1px solid #BFDBFE; border-radius: 10px; padding: 12px; margin-bottom: 16px; font-size: 0.8rem; color: #1E40AF;">
                🔒 <strong>Single-Device Lock Policy:</strong> Students are strictly locked to their first authenticated browser/device. Admins can reset device tokens on the dashboard.
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <button type="button" class="btn-sm-action" style="background: #E2E8F0; color: #334155; border: none; padding: 8px 16px; border-radius: 8px; font-weight: 700; cursor: pointer;" onclick="closeAdminSettingsModal()">
                    Close
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Notifications Popover Backdrop / Modal -->
<div class="admin-modal-backdrop" id="adminNotifModal" onclick="closeModalOnBackdrop(event, 'adminNotifModal')">
    <div class="admin-modal-card" style="max-width: 440px;">
        <div class="admin-modal-header">
            <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 1.25rem;">🔔</span>
                <h3 class="admin-modal-title">Live System Alerts</h3>
            </div>
            <button type="button" class="admin-modal-close" onclick="closeAdminNotifModal()">&times;</button>
        </div>
        <div class="admin-modal-body" style="padding: 1rem 1.25rem;">
            <div style="display: flex; flex-direction: column; gap: 10px;">
                <div style="padding: 10px 12px; background: #F0FDF4; border: 1px solid #BBF7D0; border-radius: 8px;">
                    <div style="font-weight: 700; font-size: 0.82rem; color: #166534;">Attendance System Operational</div>
                    <div style="font-size: 0.75rem; color: #15803D; margin-top: 2px;">Timers, geofence validations, and server clocks synchronized to Asia/Kolkata.</div>
                </div>
                <c:if test="${outsideGeofenceCount > 0}">
                    <div style="padding: 10px 12px; background: #FEF2F2; border: 1px solid #FECACA; border-radius: 8px;">
                        <div style="font-weight: 700; font-size: 0.82rem; color: #991B1B;">⚠️ Geofence Exception Alert</div>
                        <div style="font-size: 0.75rem; color: #B91C1C; margin-top: 2px;">${outsideGeofenceCount} student check(s) flagged outside the 1,000m campus boundary.</div>
                    </div>
                </c:if>
                <div style="padding: 10px 12px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px;">
                    <div style="font-weight: 700; font-size: 0.82rem; color: #0F172A;">Single-Device Security</div>
                    <div style="font-size: 0.75rem; color: #64748B; margin-top: 2px;">${activeSessions} device token session(s) actively registered.</div>
                </div>
            </div>
            <div style="margin-top: 14px; text-align: right;">
                <button type="button" class="btn-sm-action" style="background: #E2E8F0; color: #334155; border: none; padding: 6px 14px; border-radius: 6px; font-weight: 700; cursor: pointer;" onclick="closeAdminNotifModal()">
                    Dismiss
                </button>
            </div>
        </div>
    </div>
</div>
