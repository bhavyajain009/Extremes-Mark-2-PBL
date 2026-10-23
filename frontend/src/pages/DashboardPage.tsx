import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  Megaphone,
  ChevronRight,
  GraduationCap,
  Calendar,
  BarChart2,
  FileText,
  Info,
  ArrowRight,
} from 'lucide-react';
import { authService, noticeService } from '../services/api';
import { useAttendanceWindow } from '../hooks/useAttendanceWindow';
import type { Notice, User } from '../types';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [notice, setNotice] = useState<Notice | null>(null);
  const [hasUnread, setHasUnread] = useState(false);

  const {
    canCheckIn,
    canCheckOut,
    isEveningCheckOut,
    buttonSubtext,
    hasCheckedInToday,
    hasCheckedOutToday,
    todayCheckInTime,
  } = useAttendanceWindow();

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (!currentUser) {
      navigate('/login');
      return;
    }
    setUser(currentUser);

    noticeService.getLatestImportantNotice().then(setNotice);
    noticeService.hasUnreadNotices().then(setHasUnread);
  }, [navigate]);

  const handleActionClick = () => {
    if (isEveningCheckOut) {
      if (canCheckOut) {
        navigate('/check-out');
      }
    } else {
      if (canCheckIn) {
        navigate('/check-in');
      }
    }
  };

  if (!user) return null;

  return (
    <div className="flex-1 flex flex-col px-4 pt-3 pb-6">
      {/* 1. Header: Extremis + Student Dashboard + Desktop Navigation + Bell Icon */}
      <header className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-xl md:text-2xl font-black text-slate-900 tracking-tight">Extremis</h1>
          <p className="text-xs text-slate-500 font-medium">Student Dashboard</p>
        </div>

        {/* Desktop Navigation Links */}
        <nav className="hidden md:flex items-center gap-1.5 bg-blue-50/70 p-1 rounded-full border border-blue-100">
          <button
            onClick={() => navigate('/dashboard')}
            className="px-4 py-1.5 rounded-full text-xs font-bold bg-[#1D4ED8] text-white shadow-xs"
          >
            Home
          </button>
          <button
            onClick={() => navigate('/report')}
            className="px-4 py-1.5 rounded-full text-xs font-semibold text-slate-600 hover:text-[#1D4ED8] hover:bg-white/80 transition-colors"
          >
            Report
          </button>
          <button
            onClick={() => navigate('/profile')}
            className="px-4 py-1.5 rounded-full text-xs font-semibold text-slate-600 hover:text-[#1D4ED8] hover:bg-white/80 transition-colors"
          >
            Profile
          </button>
        </nav>

        <button
          onClick={() => navigate('/notifications')}
          className="relative w-10 h-10 rounded-full flex items-center justify-center text-slate-700 hover:bg-white transition-colors"
          title="Notifications"
        >
          <Bell className="w-5 h-5 text-slate-700 stroke-[2.2px]" />
          {hasUnread && (
            <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-red-500 ring-2 ring-[#F5F9FF]" />
          )}
        </button>
      </header>

      {/* Responsive Dashboard Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left / Main Column */}
        <div className="lg:col-span-7 flex flex-col gap-3.5">
          {/* 2. Important Notice Card */}
          {notice && (
            <div
              onClick={() => navigate('/notifications')}
              className="bg-gradient-to-r from-blue-50 to-indigo-50/60 rounded-2xl p-3.5 border border-blue-100 flex items-center justify-between shadow-xs cursor-pointer hover:border-blue-200 transition-all active:scale-[0.99]"
            >
              <div className="flex items-center gap-3 min-w-0">
                <div className="w-10 h-10 rounded-xl bg-blue-100/80 text-[#1D4ED8] flex items-center justify-center shrink-0">
                  <Megaphone className="w-5 h-5 stroke-[2.2px]" />
                </div>
                <div className="min-w-0">
                  <div className="font-extrabold text-[#1D4ED8] text-xs leading-none">Important Notice</div>
                  <div className="font-medium text-slate-700 text-xs mt-1 truncate">
                    {notice.subTitle || notice.message}
                  </div>
                  {notice.dueDate && (
                    <div className="text-[11px] text-slate-500 font-normal">{notice.dueDate}</div>
                  )}
                </div>
              </div>
              <ChevronRight className="w-5 h-5 text-slate-400 shrink-0 ml-2" />
            </div>
          )}

          {/* 3. Profile Card */}
          <div className="bg-white rounded-2xl p-4 border border-slate-100 shadow-sm flex items-center gap-3.5">
            <div className="w-13 h-13 rounded-full bg-blue-100/70 text-[#1D4ED8] flex items-center justify-center shrink-0 border-2 border-blue-200/50">
              <svg className="w-7 h-7 fill-current" viewBox="0 0 24 24">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
              </svg>
            </div>

            <div>
              <h2 className="font-extrabold text-slate-900 text-base leading-snug">{user.fullName}</h2>
              <div className="text-xs font-bold text-slate-500 mt-0.5">{user.rtuRollNumber}</div>
              <div className="flex items-center gap-1.5 text-xs text-slate-500 mt-1 font-medium">
                <GraduationCap className="w-3.5 h-3.5 text-slate-500" />
                <span>{user.branch} – Section {user.section}</span>
              </div>
            </div>
          </div>

          {/* 4. Two Cards Side by Side (Today's Attendance + Overall Attendance) */}
          <div className="grid grid-cols-2 gap-3">
            {/* Card A: Today's Attendance */}
            <div className="bg-white rounded-2xl p-3.5 border border-slate-100 shadow-sm flex flex-col justify-between">
              <div className="flex items-center gap-2 mb-2">
                <Calendar className="w-4 h-4 text-[#1D4ED8]" />
                <div className="text-xs font-bold text-slate-800 leading-tight">
                  <div>Today's</div>
                  <div>Attendance</div>
                </div>
              </div>

              <div className="my-1.5">
                {hasCheckedInToday ? (
                  <span className="inline-block px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-700">
                    {hasCheckedOutToday ? 'Checked Out' : 'Checked In'}
                  </span>
                ) : (
                  <span className="inline-block px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-slate-100 text-slate-600">
                    Not Marked
                  </span>
                )}
                <div className="text-xs font-bold text-slate-800 mt-1.5">
                  {todayCheckInTime || '—'}
                </div>
              </div>

              <div className="text-[11px] text-slate-400 font-medium">Out of 5 lectures</div>
            </div>

            {/* Card B: Overall Attendance (Progress Ring) */}
            <div className="bg-white rounded-2xl p-3.5 border border-slate-100 shadow-sm flex flex-col justify-between items-center text-center">
              <div className="w-full flex items-center gap-2 mb-1 justify-start">
                <BarChart2 className="w-4 h-4 text-[#1D4ED8]" />
                <div className="text-xs font-bold text-slate-800 text-left leading-tight">
                  <div>Overall</div>
                  <div>Attendance</div>
                </div>
              </div>

              {/* Circular Ring 78% */}
              <div className="relative w-16 h-16 my-1 flex items-center justify-center">
                <svg className="w-full h-full -rotate-90" viewBox="0 0 36 36">
                  <path
                    className="text-slate-100 stroke-current"
                    strokeWidth="3.5"
                    fill="none"
                    d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  />
                  <path
                    className="text-[#1D4ED8] stroke-current stroke-linecap-round"
                    strokeWidth="3.5"
                    strokeDasharray="78, 100"
                    fill="none"
                    d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  />
                </svg>
                <span className="absolute font-black text-slate-900 text-xs">78%</span>
              </div>

              <div className="text-[11px] text-slate-400 font-medium">78 / 100 classes</div>
            </div>
          </div>

          {/* 5. Attendance Report Teaser Card */}
          <div
            onClick={() => navigate('/report')}
            className="bg-white rounded-2xl p-3.5 border border-slate-100 shadow-sm flex items-center justify-between cursor-pointer hover:border-blue-100 transition-all active:scale-[0.99]"
          >
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-blue-50 text-[#1D4ED8] flex items-center justify-center">
                <FileText className="w-4 h-4" />
              </div>
              <div>
                <div className="font-extrabold text-slate-900 text-xs">Attendance Report</div>
                <div className="text-[11px] text-slate-400">View monthly attendance</div>
              </div>
            </div>
            <ChevronRight className="w-5 h-5 text-slate-400" />
          </div>
        </div>

        {/* Right / Action Column */}
        <div className="lg:col-span-5 bg-white rounded-2xl p-6 border border-slate-100 shadow-sm flex flex-col items-center justify-center lg:sticky lg:top-6">
          <div className="w-full flex items-center justify-between pb-3 mb-4 border-b border-slate-100">
            <span className="text-xs font-extrabold text-slate-800">Campus Verification</span>
            <span className="text-[11px] font-bold px-2 py-0.5 rounded-full bg-blue-50 text-blue-600">
              {isEveningCheckOut ? 'Check Out' : 'Check In'}
            </span>
          </div>

          {isEveningCheckOut ? (
            /* Red Check Out Button (After 3:40 PM) */
            <button
              onClick={handleActionClick}
              disabled={!canCheckOut}
              className={`w-36 h-36 rounded-full flex flex-col items-center justify-center text-white transition-all shadow-xl select-none ${
                canCheckOut
                  ? 'bg-gradient-to-tr from-[#E11D48] via-[#F43F5E] to-[#BE123C] shadow-rose-500/35 hover:scale-105 active:scale-95 cursor-pointer ring-8 ring-rose-100'
                  : 'bg-slate-300 shadow-none cursor-not-allowed opacity-75 ring-4 ring-slate-100'
              }`}
            >
              <div className="w-10 h-10 rounded-full border-2 border-white flex items-center justify-center mb-1">
                <ArrowRight className="w-6 h-6 stroke-[3px]" />
              </div>
              <span className="font-black text-sm tracking-wide">Check Out</span>
            </button>
          ) : (
            /* Blue Check In Button (Morning / Day) */
            <button
              onClick={handleActionClick}
              disabled={!canCheckIn}
              className={`w-36 h-36 rounded-full flex flex-col items-center justify-center text-white transition-all shadow-xl select-none ${
                canCheckIn
                  ? 'bg-gradient-to-tr from-[#1D4ED8] via-[#2563EB] to-[#1E40AF] shadow-blue-500/40 hover:scale-105 active:scale-95 cursor-pointer ring-8 ring-blue-100'
                  : hasCheckedInToday
                  ? 'bg-emerald-600 shadow-emerald-500/20 cursor-default ring-8 ring-emerald-100'
                  : 'bg-slate-300 shadow-none cursor-not-allowed opacity-80 ring-4 ring-slate-100'
              }`}
            >
              <div className="relative w-11 h-11 flex items-center justify-center mb-1">
                <div className="absolute inset-0 rounded-full border-2 border-white/80" />
                <div className="w-6 h-6 rounded-full border-2 border-white/90 flex items-center justify-center">
                  <div className="w-2 h-2 rounded-full bg-white" />
                </div>
              </div>
              <span className="font-black text-sm tracking-wide">
                {hasCheckedInToday ? 'Checked In' : 'Check In'}
              </span>
            </button>
          )}

          {/* Timing Subtext */}
          <p className="text-xs font-bold text-slate-700 mt-4 text-center">
            {buttonSubtext}
          </p>

          {/* Info Note */}
          <div className="flex items-start gap-2 bg-blue-50/70 border border-blue-100/80 rounded-xl p-2.5 mt-3 max-w-xs text-[11px] text-slate-600 leading-normal">
            <Info className="w-4 h-4 text-[#1D4ED8] shrink-0 mt-0.5" />
            <span>
              After 3:40 PM, this option will change to <strong>Check Out</strong> instead of Check In.
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
