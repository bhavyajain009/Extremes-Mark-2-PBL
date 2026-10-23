import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ChevronLeft, ChevronRight, Calendar as CalendarIcon, List } from 'lucide-react';
import { attendanceService } from '../services/api';
import type { AttendanceReportSummary, CalendarDay } from '../types';

export const AttendanceReportPage: React.FC = () => {
  const navigate = useNavigate();
  const [viewMode, setViewMode] = useState<'list' | 'calendar'>('list');
  const [selectedFilter, setSelectedFilter] = useState('ALL');
  const [selectedMonth, setSelectedMonth] = useState('October 2026');
  const [reportData, setReportData] = useState<AttendanceReportSummary | null>(null);
  const [calendarDays, setCalendarDays] = useState<CalendarDay[]>([]);

  useEffect(() => {
    attendanceService.getReportSummary(selectedFilter).then(setReportData);
    setCalendarDays(attendanceService.getCalendarDays(2026, 10));
  }, [selectedFilter]);

  return (
    <div className="flex-1 flex flex-col px-4 pt-3 pb-6">
      {/* Header */}
      <div className="flex items-center justify-between py-2 mb-3">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-1 -ml-1 text-slate-600 hover:text-slate-900 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-lg font-black text-slate-900 tracking-tight">
            {viewMode === 'calendar' ? 'Monthly Attendance' : 'Attendance Report'}
          </h1>
        </div>

        {/* View Mode Toggle Pill */}
        <button
          onClick={() => setViewMode(viewMode === 'list' ? 'calendar' : 'list')}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-slate-200/80 rounded-xl text-xs font-bold text-[#1D4ED8] shadow-2xs hover:bg-blue-50 transition-all cursor-pointer"
        >
          {viewMode === 'list' ? (
            <>
              <CalendarIcon className="w-3.5 h-3.5" />
              <span>Calendar</span>
            </>
          ) : (
            <>
              <List className="w-3.5 h-3.5" />
              <span>List View</span>
            </>
          )}
        </button>
      </div>

      {/* Month Dropdown + Filter Pill Controls (Screen 3 Top Row) */}
      <div className="grid grid-cols-5 gap-2.5 mb-3.5">
        <div className="col-span-3 bg-white rounded-xl border border-slate-200/80 px-3 py-2 flex items-center gap-2 shadow-2xs">
          <CalendarIcon className="w-4 h-4 text-slate-500 shrink-0" />
          <select
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
            className="w-full text-xs font-bold text-slate-800 bg-transparent focus:outline-none cursor-pointer"
          >
            <option value="October 2026">October 2026</option>
            <option value="September 2026">September 2026</option>
            <option value="August 2026">August 2026</option>
          </select>
        </div>

        <div className="col-span-2 bg-white rounded-xl border border-slate-200/80 px-3 py-2 flex items-center shadow-2xs">
          <select
            value={selectedFilter}
            onChange={(e) => setSelectedFilter(e.target.value)}
            className="w-full text-xs font-bold text-slate-800 bg-transparent focus:outline-none cursor-pointer"
          >
            <option value="ALL">All</option>
            <option value="PRESENT">Present</option>
            <option value="ABSENT">Absent</option>
            <option value="OD">OD</option>
          </select>
        </div>
      </div>

      {/* 4 Summary Tiles (Present 22, Absent 3, OD 0, Total 25) */}
      {reportData && (
        <div className="grid grid-cols-4 gap-2 mb-4">
          {/* Present */}
          <div className="bg-emerald-50/70 border-t-2 border-emerald-500 rounded-xl p-2.5 text-center shadow-2xs">
            <div className="text-xl font-black text-emerald-600 leading-none">{reportData.presentCount}</div>
            <div className="text-[10px] font-bold text-slate-500 mt-1">Present</div>
          </div>

          {/* Absent */}
          <div className="bg-rose-50/70 border-t-2 border-rose-500 rounded-xl p-2.5 text-center shadow-2xs">
            <div className="text-xl font-black text-rose-600 leading-none">{reportData.absentCount}</div>
            <div className="text-[10px] font-bold text-slate-500 mt-1">Absent</div>
          </div>

          {/* OD */}
          <div className="bg-amber-50/70 border-t-2 border-amber-500 rounded-xl p-2.5 text-center shadow-2xs">
            <div className="text-xl font-black text-amber-600 leading-none">{reportData.odCount}</div>
            <div className="text-[10px] font-bold text-slate-500 mt-1">OD</div>
          </div>

          {/* Total */}
          <div className="bg-blue-50/70 border-t-2 border-[#1D4ED8] rounded-xl p-2.5 text-center shadow-2xs">
            <div className="text-xl font-black text-[#1D4ED8] leading-none">{reportData.totalClasses}</div>
            <div className="text-[10px] font-bold text-slate-500 mt-1">Total</div>
          </div>
        </div>
      )}

      {/* VIEW 1: TABLE LIST VIEW (Screen 3 Mockup) */}
      {viewMode === 'list' && reportData && (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
          <div className="overflow-x-auto no-scrollbar">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-100 text-slate-400 font-bold text-[11px] bg-slate-50/50">
                  <th className="py-2.5 pl-4 pr-2">Date</th>
                  <th className="py-2.5 px-2">Check In</th>
                  <th className="py-2.5 px-2">Check Out</th>
                  <th className="py-2.5 pr-4 pl-2 text-right">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700 font-medium">
                {reportData.records.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="py-3 pl-4 pr-2 font-bold text-slate-900 whitespace-nowrap">
                      {r.dateStr}
                    </td>
                    <td className="py-3 px-2 text-slate-600 whitespace-nowrap">
                      {r.checkInTime || '—'}
                    </td>
                    <td className="py-3 px-2 text-slate-600 whitespace-nowrap">
                      {r.checkOutTime || '—'}
                    </td>
                    <td className="py-3 pr-4 pl-2 text-right whitespace-nowrap">
                      {r.status === 'PRESENT' && (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-700">
                          Present
                        </span>
                      )}
                      {r.status === 'ABSENT' && (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-rose-100 text-rose-700">
                          Absent
                        </span>
                      )}
                      {r.status === 'OD' && (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-700">
                          OD
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* VIEW 2: MONTHLY CALENDAR VIEW (Screen 7 Mockup) */}
      {viewMode === 'calendar' && (
        <div className="bg-white rounded-2xl p-4 border border-slate-100 shadow-sm flex flex-col">
          {/* Calendar Header with Navigation */}
          <div className="flex items-center justify-between mb-4">
            <button className="p-1 rounded-full text-slate-400 hover:text-slate-700 hover:bg-slate-100">
              <ChevronLeft className="w-5 h-5" />
            </button>
            <h3 className="font-extrabold text-slate-900 text-sm">October 2026</h3>
            <button className="p-1 rounded-full text-slate-400 hover:text-slate-700 hover:bg-slate-100">
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>

          {/* Weekday headers: Sun - Sat */}
          <div className="grid grid-cols-7 text-center text-[11px] font-bold text-slate-400 mb-2">
            <span className="text-rose-500">Sun</span>
            <span>Mon</span>
            <span>Tue</span>
            <span>Wed</span>
            <span>Thu</span>
            <span>Fri</span>
            <span>Sat</span>
          </div>

          {/* Grid: 1 Oct 2026 started on Thursday (offset 4) */}
          <div className="grid grid-cols-7 gap-y-2.5 text-center text-xs font-bold">
            {/* 4 blank cells before Oct 1 */}
            <div />
            <div />
            <div />
            <div />

            {/* Days of October */}
            {calendarDays.map((d) => {
              let circleStyle = 'text-slate-700 hover:bg-slate-100';

              if (d.isToday) {
                // Today: Solid Blue (day 12)
                circleStyle = 'bg-[#1D4ED8] text-white shadow-xs';
              } else if (d.isSunday || d.status === 'ABSENT') {
                // Sunday / Absent: Solid Red
                circleStyle = 'bg-rose-500 text-white';
              } else if (d.status === 'PRESENT') {
                // Present: Solid Green
                circleStyle = 'bg-emerald-500 text-white';
              } else if (d.status === 'OD') {
                circleStyle = 'bg-amber-500 text-white';
              }

              return (
                <div key={d.dayNumber} className="flex items-center justify-center">
                  <div
                    className={`w-7 h-7 rounded-full flex items-center justify-center transition-all ${circleStyle}`}
                  >
                    {d.dayNumber}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Legend at bottom matching Screen 7 */}
          <div className="mt-6 pt-3 border-t border-slate-100 flex items-center justify-around text-[11px] text-slate-500 font-medium">
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500" />
              <span>Present</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-rose-500" />
              <span>Absent</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-[#1D4ED8]" />
              <span>Today</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-slate-300" />
              <span>Not Marked</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
