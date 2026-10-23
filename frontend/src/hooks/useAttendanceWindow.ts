import { useEffect, useState } from 'react';
import type { AttendanceWindowState } from '../types';
import { attendanceService } from '../services/api';

// Global override for developer demonstration
let devTimeOverride: string | null = null; // format: "HH:mm" e.g. "08:55"
const listeners: Array<() => void> = [];

export const setDevTimeOverride = (timeStr: string | null) => {
  devTimeOverride = timeStr;
  listeners.forEach((l) => l());
};

export const getDevTimeOverride = () => devTimeOverride;

export function useAttendanceWindow(): AttendanceWindowState & {
  overrideTime: (timeStr: string | null) => void;
  resetCheckState: () => void;
} {
  const [now, setNow] = useState<Date>(new Date());
  const [todayCheckIn, setTodayCheckIn] = useState<string | null>(null);
  const [todayCheckOut, setTodayCheckOut] = useState<string | null>(null);

  // Load check-in status
  const refreshAttendance = () => {
    attendanceService.getTodayAttendance().then((res) => {
      setTodayCheckIn(res.checkInTime);
      setTodayCheckOut(res.checkOutTime);
    });
  };

  useEffect(() => {
    refreshAttendance();

    const handleUpdate = () => {
      setNow(new Date());
      refreshAttendance();
    };

    listeners.push(handleUpdate);

    // Update real time every 10 seconds if not overridden
    const interval = setInterval(() => {
      if (!devTimeOverride) {
        setNow(new Date());
      }
    }, 10000);

    return () => {
      const idx = listeners.indexOf(handleUpdate);
      if (idx !== -1) listeners.splice(idx, 1);
      clearInterval(interval);
    };
  }, []);

  // Determine effective hours & minutes
  let currentHours: number;
  let currentMinutes: number;

  if (devTimeOverride) {
    const [h, m] = devTimeOverride.split(':').map(Number);
    currentHours = h;
    currentMinutes = m;
  } else {
    currentHours = now.getHours();
    currentMinutes = now.getMinutes();
  }

  const currentTotalMinutes = currentHours * 60 + currentMinutes;

  // Key thresholds:
  // 08:50 -> 8 * 60 + 50 = 530
  // 09:05 -> 9 * 60 + 5 = 545
  // 15:40 -> 15 * 60 + 40 = 940 (3:40 PM)
  const windowStart = 8 * 60 + 50; // 08:50 AM
  const windowEnd = 9 * 60 + 5;    // 09:05 AM
  const checkOutStart = 15 * 60 + 40; // 03:40 PM

  const isBeforeWindow = currentTotalMinutes < windowStart;
  const isDuringCheckInWindow = currentTotalMinutes >= windowStart && currentTotalMinutes <= windowEnd;
  const isAfterCheckInWindow = currentTotalMinutes > windowEnd && currentTotalMinutes < checkOutStart;
  const isEveningCheckOut = currentTotalMinutes >= checkOutStart;

  const hasCheckedInToday = Boolean(todayCheckIn);
  const hasCheckedOutToday = Boolean(todayCheckOut);

  let buttonLabel: AttendanceWindowState['buttonLabel'] = 'Check In';
  let buttonSubtext = 'Available 8:50 AM – 9:05 AM';
  let buttonColor: AttendanceWindowState['buttonColor'] = 'blue';
  let canCheckIn = false;
  let canCheckOut = false;

  if (hasCheckedOutToday) {
    buttonLabel = 'Checked Out';
    buttonSubtext = `Departed at ${todayCheckOut}`;
    buttonColor = 'gray';
  } else if (isEveningCheckOut) {
    // 3:40 PM onward: Check Out mode
    buttonLabel = 'Check Out';
    buttonColor = 'red';
    if (hasCheckedInToday) {
      canCheckOut = true;
      buttonSubtext = 'Evening departure available';
    } else {
      canCheckOut = false;
      buttonSubtext = 'Cannot check out: Not checked in today';
      buttonColor = 'gray';
    }
  } else if (hasCheckedInToday) {
    // Already checked in morning, waiting for 3:40 PM
    buttonLabel = 'Check In';
    canCheckIn = false;
    buttonColor = 'gray';
    buttonSubtext = `Checked In at ${todayCheckIn}. Check Out available after 3:40 PM.`;
  } else if (isBeforeWindow) {
    buttonLabel = 'Check In';
    canCheckIn = false;
    buttonColor = 'gray';
    buttonSubtext = 'Opens at 8:50 AM';
  } else if (isDuringCheckInWindow) {
    buttonLabel = 'Check In';
    canCheckIn = true;
    buttonColor = 'blue';
    buttonSubtext = 'Available 8:50 AM – 9:05 AM';
  } else {
    // 9:05 AM - 3:40 PM and not checked in
    buttonLabel = 'Check-in Closed';
    canCheckIn = false;
    buttonColor = 'gray';
    buttonSubtext = 'Check In window closed';
  }

  // Format current effective time string e.g. "08:55 AM"
  const period = currentHours >= 12 ? 'PM' : 'AM';
  const displayH = currentHours % 12 === 0 ? 12 : currentHours % 12;
  const currentTimeStr = `${String(displayH).padStart(2, '0')}:${String(currentMinutes).padStart(2, '0')} ${period}`;

  const resetCheckState = () => {
    localStorage.removeItem('extremis_today_checkin');
    localStorage.removeItem('extremis_today_checkout');
    refreshAttendance();
  };

  return {
    currentTimeStr,
    canCheckIn,
    canCheckOut,
    isBeforeWindow,
    isAfterWindow: isAfterCheckInWindow,
    isEveningCheckOut,
    buttonLabel,
    buttonSubtext,
    buttonColor,
    hasCheckedInToday,
    hasCheckedOutToday,
    todayCheckInTime: todayCheckIn || undefined,
    todayCheckOutTime: todayCheckOut || undefined,
    overrideTime: setDevTimeOverride,
    resetCheckState,
  };
}
