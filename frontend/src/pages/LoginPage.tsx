import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserCheck, ShieldAlert, Lock, ArrowRight, User, GraduationCap, Shield } from 'lucide-react';
import { authService } from '../services/api';
import { Role } from '../types';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [selectedRole, setSelectedRole] = useState<Role>('student');
  const [username, setUsername] = useState('STU2024001');
  const [password, setPassword] = useState('StudentPassword123!');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleRoleChange = (role: Role) => {
    setSelectedRole(role);
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const res = await authService.login(username, password, selectedRole);
      if (!res.success) {
        setError(res.error || 'Login failed. Please check credentials.');
        setIsLoading(false);
        return;
      }

      // Check if student has already granted permissions
      if (res.user && res.user.hasAcceptedPermissions) {
        navigate('/dashboard');
      } else {
        navigate('/permissions');
      }
    } catch {
      setError('An error occurred during authentication.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex-1 flex flex-col px-5 py-8 justify-center max-w-sm mx-auto w-full">
      {/* Brand & App Title */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-tr from-[#1D4ED8] to-[#2563EB] text-white shadow-lg shadow-blue-500/25 mb-3">
          <GraduationCap className="w-9 h-9" />
        </div>
        <h1 className="text-2xl font-black text-slate-900 tracking-tight">Extremis</h1>
        <p className="text-xs text-slate-500 font-medium mt-0.5">Role-Based Student Attendance Portal</p>
      </div>

      {/* Role Selector */}
      <div className="bg-slate-200/70 p-1 rounded-xl flex gap-1 mb-6">
        <button
          type="button"
          onClick={() => handleRoleChange('student')}
          className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1.5 ${
            selectedRole === 'student'
              ? 'bg-white text-[#1D4ED8] shadow-xs'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          <User className="w-3.5 h-3.5" />
          <span>Student</span>
        </button>

        <button
          type="button"
          onClick={() => handleRoleChange('faculty')}
          className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1.5 ${
            selectedRole === 'faculty'
              ? 'bg-white text-[#1D4ED8] shadow-xs'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          <GraduationCap className="w-3.5 h-3.5" />
          <span>Faculty</span>
        </button>

        <button
          type="button"
          onClick={() => handleRoleChange('admin')}
          className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1.5 ${
            selectedRole === 'admin'
              ? 'bg-white text-[#1D4ED8] shadow-xs'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          <Shield className="w-3.5 h-3.5" />
          <span>Admin</span>
        </button>
      </div>

      {/* Non-student notification */}
      {selectedRole !== 'student' ? (
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-slate-100 text-center py-10">
          <div className="w-12 h-12 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center mx-auto mb-3">
            <Lock className="w-6 h-6" />
          </div>
          <h3 className="font-bold text-slate-800 text-base capitalize">{selectedRole} Portal</h3>
          <p className="text-xs text-slate-500 mt-1 mb-4">
            The {selectedRole} management interface is coming soon. Please sign in using the Student portal.
          </p>
          <button
            type="button"
            onClick={() => handleRoleChange('student')}
            className="text-xs font-bold text-[#1D4ED8] hover:underline"
          >
            ← Back to Student Login
          </button>
        </div>
      ) : (
        /* Login Card */
        <div className="bg-white rounded-2xl p-5 shadow-sm border border-slate-100">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2 font-medium">
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1" htmlFor="username">
                Student ID / Roll Number
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. STU2024001"
                required
                className="w-full h-11 px-3.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-[#1D4ED8] focus:bg-white focus:ring-2 focus:ring-blue-500/15 transition-all text-slate-800"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1" htmlFor="password">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
                className="w-full h-11 px-3.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-[#1D4ED8] focus:bg-white focus:ring-2 focus:ring-blue-500/15 transition-all text-slate-800"
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full h-12 bg-gradient-to-r from-[#1D4ED8] to-[#2563EB] hover:from-blue-700 hover:to-blue-600 text-white font-bold rounded-xl text-sm shadow-md shadow-blue-500/25 flex items-center justify-center gap-2 transition-all active:scale-[0.98] disabled:opacity-60 cursor-pointer"
            >
              <span>{isLoading ? 'Signing In...' : 'Sign In as Student'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Single active session note */}
          <div className="mt-4 pt-3 border-t border-slate-100 flex items-center gap-2 text-[11px] text-slate-500">
            <UserCheck className="w-3.5 h-3.5 text-blue-600 shrink-0" />
            <span>Single active device session enforced.</span>
          </div>
        </div>
      )}

      {/* Demo Credentials Helper */}
      <div className="mt-4 text-center">
        <p className="text-[11px] text-slate-400">
          Demo: <code className="text-[#1D4ED8] font-bold">STU2024001</code> / <code className="text-slate-600">StudentPassword123!</code>
        </p>
      </div>
    </div>
  );
};
