import {
  AttendanceRecord,
  AttendanceReportSummary,
  CalendarDay,
  Notice,
  Role,
  User,
} from '../types';

// Storage keys
const STORAGE_USER = 'extremis_current_user';
const STORAGE_SESSION_TOKEN = 'extremis_session_token';
const STORAGE_TODAY_CHECKIN = 'extremis_today_checkin';
const STORAGE_TODAY_CHECKOUT = 'extremis_today_checkout';

// Default mock student matching the reference mockup
const DEFAULT_STUDENT: User = {
  id: 'STU2024001',
  username: 'STU2024001',
  role: 'student',
  fullName: 'Bhavya Kumar Bohra',
  collegeEmail: 'bhavya.24earad031@aryacollege.edu.in',
  rtuRollNumber: '24EARAD031',
  branch: 'AI',
  section: 'A',
  hasAcceptedPermissions: false,
};

const MOCK_NOTICES: Notice[] = [
  {
    id: 'n1',
    title: 'Important Notice',
    subTitle: 'Midterm exam form submission',
    dueDate: 'last date: 15 Oct 2026',
    dateStr: '10 Oct 2026',
    category: 'IMPORTANT',
    message: 'Midterm exam form submission last date: 15 Oct 2026.',
    isRead: false,
  },
  {
    id: 'n2',
    title: 'College Holiday',
    subTitle: 'Campus will remain closed',
    dateStr: '8 Oct 2026',
    category: 'GENERAL',
    message: 'College will remain closed on 12 Oct 2026.',
    isRead: true,
  },
  {
    id: 'n3',
    title: 'Regular Academic Classes',
    subTitle: 'Timetable schedule notice',
    dateStr: '5 Oct 2026',
    category: 'ACADEMIC',
    message: 'Regular classes will resume from 13 Oct 2026.',
    isRead: true,
  },
  {
    id: 'n4',
    title: 'Attendance Reminder',
    subTitle: 'Academic compliance',
    dateStr: '1 Oct 2026',
    category: 'GENERAL',
    message: 'Review your attendance regularly.',
    isRead: true,
  },
];

const MOCK_RECORDS: AttendanceRecord[] = [
  { id: 'r1', dateStr: '10 Oct', fullDate: '2026-10-10', checkInTime: '08:52 AM', checkOutTime: null, status: 'PRESENT' },
  { id: 'r2', dateStr: '09 Oct', fullDate: '2026-10-09', checkInTime: '08:55 AM', checkOutTime: '03:42 PM', status: 'PRESENT' },
  { id: 'r3', dateStr: '08 Oct', fullDate: '2026-10-08', checkInTime: '08:58 AM', checkOutTime: '03:45 PM', status: 'PRESENT' },
  { id: 'r4', dateStr: '07 Oct', fullDate: '2026-10-07', checkInTime: null, checkOutTime: null, status: 'ABSENT' },
  { id: 'r5', dateStr: '06 Oct', fullDate: '2026-10-06', checkInTime: '08:51 AM', checkOutTime: '03:41 PM', status: 'PRESENT' },
  { id: 'r6', dateStr: '05 Oct', fullDate: '2026-10-05', checkInTime: '08:53 AM', checkOutTime: '03:43 PM', status: 'PRESENT' },
  { id: 'r7', dateStr: '04 Oct', fullDate: '2026-10-04', checkInTime: null, checkOutTime: null, status: 'ABSENT' },
  { id: 'r8', dateStr: '03 Oct', fullDate: '2026-10-03', checkInTime: '08:50 AM', checkOutTime: '03:40 PM', status: 'PRESENT' },
];

/**
 * Authentication Service
 */
export const authService = {
  async login(username: string, _password: string, role: Role, deviceId?: string): Promise<{ success: boolean; user?: User; error?: string }> {
    if (role !== 'student') {
      return { success: false, error: `${role.toUpperCase()} portal is coming soon.` };
    }

    // Single active session simulation
    // Generates a unique session token for this device. If another device logs in,
    // the previous token becomes invalid.
    const newSessionToken = 'sess_' + Math.random().toString(36).substring(2) + Date.now();
    localStorage.setItem(STORAGE_SESSION_TOKEN, newSessionToken);

    // Retrieve or default student
    const existing = localStorage.getItem(STORAGE_USER);
    let user: User = DEFAULT_STUDENT;
    if (existing) {
      try {
        user = { ...DEFAULT_STUDENT, ...JSON.parse(existing) };
      } catch {
        user = DEFAULT_STUDENT;
      }
    }

    user = {
      ...user,
      username: username.trim() || DEFAULT_STUDENT.username,
      sessionToken: newSessionToken,
      deviceId: deviceId || 'DEV-' + Math.random().toString(36).substring(2, 8).toUpperCase(),
    };

    localStorage.setItem(STORAGE_USER, JSON.stringify(user));
    return { success: true, user };
  },

  getCurrentUser(): User | null {
    const data = localStorage.getItem(STORAGE_USER);
    if (!data) return null;
    try {
      const user = JSON.parse(data) as User;
      // Session validation check
      const currentToken = localStorage.getItem(STORAGE_SESSION_TOKEN);
      if (user.sessionToken && user.sessionToken !== currentToken) {
        // Session invalidated by new login on another device
        this.logout();
        return null;
      }
      return user;
    } catch {
      return null;
    }
  },

  updatePermissions(granted: boolean): void {
    const user = this.getCurrentUser();
    if (user) {
      user.hasAcceptedPermissions = granted;
      localStorage.setItem(STORAGE_USER, JSON.stringify(user));
    }
  },

  logout(): void {
    localStorage.removeItem(STORAGE_USER);
    localStorage.removeItem(STORAGE_SESSION_TOKEN);
  },
};

/**
 * Attendance Service
 */
export const attendanceService = {
  async getTodayAttendance(): Promise<{ checkInTime: string | null; checkOutTime: string | null }> {
    const checkIn = localStorage.getItem(STORAGE_TODAY_CHECKIN) || '08:52 AM';
    const checkOut = localStorage.getItem(STORAGE_TODAY_CHECKOUT) || null;
    return { checkInTime: checkIn, checkOutTime: checkOut };
  },

  async recordCheckIn(timeStr: string = '08:52 AM'): Promise<boolean> {
    localStorage.setItem(STORAGE_TODAY_CHECKIN, timeStr);
    return true;
  },

  async recordCheckOut(timeStr: string = '03:42 PM'): Promise<boolean> {
    localStorage.setItem(STORAGE_TODAY_CHECKOUT, timeStr);
    return true;
  },

  async getReportSummary(filter: string = 'ALL'): Promise<AttendanceReportSummary> {
    let filtered = [...MOCK_RECORDS];
    if (filter === 'PRESENT') {
      filtered = filtered.filter((r) => r.status === 'PRESENT');
    } else if (filter === 'ABSENT') {
      filtered = filtered.filter((r) => r.status === 'ABSENT');
    } else if (filter === 'OD') {
      filtered = filtered.filter((r) => r.status === 'OD');
    }

    return {
      presentCount: 22,
      absentCount: 3,
      odCount: 0,
      totalClasses: 25,
      overallPercentage: 78,
      records: filtered,
    };
  },

  getCalendarDays(year: number = 2026, month: number = 10): CalendarDay[] {
    // 31 days in October 2026
    // October 1, 2026 starts on Thursday (index 4: Sun=0, Mon=1, Tue=2, Wed=3, Thu=4, Fri=5, Sat=6)
    const days: CalendarDay[] = [];

    // Mock day statuses matching Screen 7 Mockup:
    // Days marked Present (green): 6, 7, 8, 9, 10
    // Days marked Absent (red): 4, 11 (Sundays/Absent)
    // Day 12 is Today (blue)
    const presentDays = new Set([6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 27, 28, 29]);
    const absentDays = new Set([4, 18, 25]); // Sundays or recorded absents

    for (let day = 1; day <= 31; day++) {
      const date = new Date(year, month - 1, day);
      const isSunday = date.getDay() === 0;
      const isToday = day === 12;

      let status: AttendanceRecord['status'] = 'NOT_MARKED';
      if (isSunday || absentDays.has(day)) {
        status = 'ABSENT';
      } else if (isToday) {
        status = 'PRESENT';
      } else if (presentDays.has(day)) {
        status = 'PRESENT';
      }

      days.push({
        dayNumber: day,
        dateStr: `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
        status,
        isSunday,
        isToday,
      });
    }

    return days;
  },

  async submitCheckInCapture(_payload: { lat: number; lng: number; accuracy: number; photoBase64: string }): Promise<{ success: boolean; message: string }> {
    // Simulate API delay
    await new Promise((resolve) => setTimeout(resolve, 600));
    await this.recordCheckIn();
    return { success: true, message: 'Check In submitted successfully!' };
  },

  async submitCheckOutCapture(_payload: { lat: number; lng: number; accuracy: number; photoBase64: string }): Promise<{ success: boolean; message: string }> {
    await new Promise((resolve) => setTimeout(resolve, 600));
    await this.recordCheckOut();
    return { success: true, message: 'Check Out submitted successfully!' };
  },
};

/**
 * Notice Service
 */
export const noticeService = {
  async getNotices(): Promise<Notice[]> {
    return MOCK_NOTICES;
  },

  async getLatestImportantNotice(): Promise<Notice | null> {
    const important = MOCK_NOTICES.find((n) => n.category === 'IMPORTANT');
    return important || null;
  },

  async hasUnreadNotices(): Promise<boolean> {
    return MOCK_NOTICES.some((n) => !n.isRead);
  },
};
