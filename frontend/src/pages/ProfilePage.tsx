import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, User as UserIcon, Mail, Hash, BookOpen, Layers, LogOut, GraduationCap, ShieldCheck } from 'lucide-react';
import { authService } from '../services/api';
import type { User } from '../types';

export const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (!currentUser) {
      navigate('/login');
      return;
    }
    setUser(currentUser);
  }, [navigate]);

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <div className="flex-1 flex flex-col px-4 pt-3 pb-6">
      {/* Top Header */}
      <div className="flex items-center gap-3 py-2 mb-3">
        <button
          onClick={() => navigate('/dashboard')}
          className="p-1 -ml-1 text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-lg font-black text-slate-900 tracking-tight">My Profile</h1>
      </div>

      {/* Profile Card Header (Avatar + Name + Roll + Branch/Section) */}
      <div className="bg-white rounded-2xl p-4 border border-slate-100 shadow-sm flex flex-col items-center text-center mb-3.5">
        <div className="w-16 h-16 rounded-full bg-blue-100/70 text-[#1D4ED8] flex items-center justify-center mb-2.5 border-2 border-blue-200/50">
          <svg className="w-9 h-9 fill-current" viewBox="0 0 24 24">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
          </svg>
        </div>

        <h2 className="font-extrabold text-slate-900 text-base">{user.fullName}</h2>
        <div className="text-xs font-bold text-slate-500 mt-0.5">{user.rtuRollNumber}</div>

        <div className="flex items-center gap-1.5 text-xs text-slate-500 mt-1 font-medium">
          <GraduationCap className="w-3.5 h-3.5 text-slate-500" />
          <span>{user.branch} – Section {user.section}</span>
        </div>
      </div>

      {/* Details Rows (Screen 5 Mockup) */}
      <div className="bg-white rounded-2xl p-4 border border-slate-100 shadow-sm divide-y divide-slate-100 mb-4">
        {/* Full Name */}
        <div className="flex items-center gap-3.5 py-2.5 first:pt-0 last:pb-0">
          <div className="w-9 h-9 rounded-xl bg-blue-50 text-[#1D4ED8] flex items-center justify-center shrink-0">
            <UserIcon className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Full Name</div>
            <div className="text-xs font-bold text-slate-900 mt-0.5">{user.fullName}</div>
          </div>
        </div>

        {/* College Email */}
        <div className="flex items-center gap-3.5 py-2.5">
          <div className="w-9 h-9 rounded-xl bg-blue-50 text-[#1D4ED8] flex items-center justify-center shrink-0">
            <Mail className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">College Email</div>
            <div className="text-xs font-bold text-slate-900 mt-0.5 break-all">{user.collegeEmail}</div>
          </div>
        </div>

        {/* RTU Roll Number */}
        <div className="flex items-center gap-3.5 py-2.5">
          <div className="w-9 h-9 rounded-xl bg-blue-50 text-[#1D4ED8] flex items-center justify-center shrink-0">
            <Hash className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">RTU Roll Number</div>
            <div className="text-xs font-bold text-slate-900 mt-0.5">{user.rtuRollNumber}</div>
          </div>
        </div>

        {/* Branch */}
        <div className="flex items-center gap-3.5 py-2.5">
          <div className="w-9 h-9 rounded-xl bg-blue-50 text-[#1D4ED8] flex items-center justify-center shrink-0">
            <BookOpen className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Branch</div>
            <div className="text-xs font-bold text-slate-900 mt-0.5">{user.branch}</div>
          </div>
        </div>

        {/* Section */}
        <div className="flex items-center gap-3.5 py-2.5">
          <div className="w-9 h-9 rounded-xl bg-blue-50 text-[#1D4ED8] flex items-center justify-center shrink-0">
            <Layers className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Section</div>
            <div className="text-xs font-bold text-slate-900 mt-0.5">{user.section}</div>
          </div>
        </div>
      </div>

      {/* Device Lock Info */}
      <div className="bg-slate-50 border border-slate-200/80 rounded-2xl p-3 flex items-start gap-2.5 text-xs text-slate-600 mb-6">
        <ShieldCheck className="w-4 h-4 text-[#1D4ED8] shrink-0 mt-0.5" />
        <div className="text-[11px] leading-relaxed">
          <strong>Device Binding:</strong> Permanently bound to your registered phone. Secondary logins from new devices are blocked.
        </div>
      </div>

      {/* Logout Button */}
      <button
        onClick={handleLogout}
        className="w-full h-11 bg-rose-50 hover:bg-rose-100 text-rose-600 font-bold rounded-xl text-xs flex items-center justify-center gap-2 transition-all cursor-pointer border border-rose-200/60 active:scale-[0.99] mt-auto"
      >
        <LogOut className="w-4 h-4" />
        <span>Log Out</span>
      </button>
    </div>
  );
};
