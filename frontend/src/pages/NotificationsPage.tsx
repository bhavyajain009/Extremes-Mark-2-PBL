import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Megaphone, Calendar, Info } from 'lucide-react';
import { noticeService } from '../services/api';
import type { Notice, NoticeCategory } from '../types';

export const NotificationsPage: React.FC = () => {
  const navigate = useNavigate();
  const [notices, setNotices] = useState<Notice[]>([]);

  useEffect(() => {
    noticeService.getNotices().then(setNotices);
  }, []);

  const getCategoryBadge = (cat: NoticeCategory) => {
    switch (cat) {
      case 'IMPORTANT':
        return (
          <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-rose-100 text-rose-600">
            Important
          </span>
        );
      case 'ACADEMIC':
        return (
          <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-600">
            Academic
          </span>
        );
      case 'GENERAL':
      default:
        return (
          <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-blue-100 text-[#1D4ED8]">
            General
          </span>
        );
    }
  };

  const getCategoryIcon = (cat: NoticeCategory) => {
    switch (cat) {
      case 'IMPORTANT':
        return (
          <div className="w-10 h-10 rounded-xl bg-rose-50 text-rose-500 flex items-center justify-center shrink-0">
            <Megaphone className="w-5 h-5 stroke-[2.2px]" />
          </div>
        );
      case 'ACADEMIC':
        return (
          <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
            <Calendar className="w-5 h-5 stroke-[2.2px]" />
          </div>
        );
      case 'GENERAL':
      default:
        return (
          <div className="w-10 h-10 rounded-xl bg-blue-50 text-[#1D4ED8] flex items-center justify-center shrink-0">
            <Info className="w-5 h-5 stroke-[2.2px]" />
          </div>
        );
    }
  };

  return (
    <div className="flex-1 flex flex-col px-4 py-3">
      {/* Top Header */}
      <div className="flex items-center gap-3 py-2 mb-3">
        <button
          onClick={() => navigate('/dashboard')}
          className="p-1 -ml-1 text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="text-lg font-black text-slate-900 tracking-tight">Notifications</h1>
      </div>

      {/* Notice List Cards matching Screen 2 */}
      <div className="space-y-3">
        {notices.map((n) => (
          <div
            key={n.id}
            className={`bg-white rounded-2xl p-4 border shadow-xs transition-all ${
              !n.isRead ? 'border-blue-200 bg-blue-50/20' : 'border-slate-100'
            }`}
          >
            <div className="flex items-start gap-3.5">
              {getCategoryIcon(n.category)}

              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-1.5">
                  {getCategoryBadge(n.category)}
                  <span className="text-[11px] font-medium text-slate-400">{n.dateStr}</span>
                </div>

                <p className="text-xs font-semibold text-slate-800 leading-relaxed">
                  {n.message}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
