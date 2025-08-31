import React, { useEffect, useState } from 'react';
import {type  Device } from '../types';
import { deviceService } from '../services/api';
import { Shield, ShieldOff, Smartphone, Clock, User } from 'lucide-react';
import { format } from 'date-fns';

export const Devices: React.FC = () => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<'all' | 'active' | 'blocked'>('all');
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchDevices();
  }, []);

  const fetchDevices = async () => {
    try {
      const data = await deviceService.getAllDevices();
      setDevices(data);
    } catch (error) {
      console.error('Failed to fetch devices:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleBlockDevice = async (deviceId: string) => {
    if (!confirm(`Are you sure you want to block device ${deviceId}?`)) return;

    try {
      await deviceService.blockDevice({
        deviceId,
        reason: 'Blocked by bank admin',
        blockedBy: 'Bank Admin',
      });
      await fetchDevices();
      alert('Device blocked successfully');
    } catch (error) {
      console.error('Failed to block device:', error);
      alert('Failed to block device');
    }
  };

  const handleUnblockDevice = async (deviceId: string) => {
    if (!confirm(`Are you sure you want to unblock device ${deviceId}?`)) return;

    try {
      await deviceService.unblockDevice(deviceId);
      await fetchDevices();
      alert('Device unblocked successfully');
    } catch (error) {
      console.error('Failed to unblock device:', error);
      alert('Failed to unblock device');
    }
  };

  const filteredDevices = devices.filter((device) => {
    const matchesStatus =
      filterStatus === 'all' ||
      (filterStatus === 'active' && device.status === 'active') ||
      (filterStatus === 'blocked' && device.status === 'blocked');

    const matchesSearch =
      !searchTerm ||
      device.deviceId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      device.username.toLowerCase().includes(searchTerm.toLowerCase());

    return matchesStatus && matchesSearch;
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-gray-600">Loading devices...</div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Device Management</h1>

      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex gap-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Search by device ID or username..."
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setFilterStatus('all')}
              className={`px-4 py-2 rounded-lg font-medium ${
                filterStatus === 'all'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              All ({devices.length})
            </button>
            <button
              onClick={() => setFilterStatus('active')}
              className={`px-4 py-2 rounded-lg font-medium ${
                filterStatus === 'active'
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              Active ({devices.filter((d) => d.status === 'active').length})
            </button>
            <button
              onClick={() => setFilterStatus('blocked')}
              className={`px-4 py-2 rounded-lg font-medium ${
                filterStatus === 'blocked'
                  ? 'bg-red-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              Blocked ({devices.filter((d) => d.status === 'blocked').length})
            </button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredDevices.map((device) => (
          <div key={device.deviceId} className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow">
            <div className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg ${
                    device.status === 'active' ? 'bg-green-100' : 'bg-red-100'
                  }`}>
                    <Smartphone className={`w-6 h-6 ${
                      device.status === 'active' ? 'text-green-600' : 'text-red-600'
                    }`} />
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900">Device</p>
                    <p className={`text-sm ${
                      device.status === 'active' ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {device.status === 'active' ? 'Active' : 'Blocked'}
                    </p>
                  </div>
                </div>
                {device.status === 'active' ? (
                  <button
                    onClick={() => handleBlockDevice(device.deviceId)}
                    className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Block device"
                  >
                    <ShieldOff className="w-5 h-5" />
                  </button>
                ) : (
                  <button
                    onClick={() => handleUnblockDevice(device.deviceId)}
                    className="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                    title="Unblock device"
                  >
                    <Shield className="w-5 h-5" />
                  </button>
                )}
              </div>

              <div className="space-y-3">
                <div>
                  <p className="text-xs text-gray-500">Device ID</p>
                  <p className="font-mono text-sm break-all">{device.deviceId}</p>
                </div>

                <div className="flex items-center gap-2">
                  <User className="w-4 h-4 text-gray-400" />
                  <div>
                    <p className="text-xs text-gray-500">User</p>
                    <p className="text-sm font-medium">{device.username}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-gray-400" />
                  <div>
                    <p className="text-xs text-gray-500">Last Used</p>
                    <p className="text-sm">{format(new Date(device.lastUsed), 'MMM dd, yyyy HH:mm')}</p>
                  </div>
                </div>

                {device.deviceInfo && (
                  <div className="pt-3 border-t">
                    <p className="text-xs text-gray-500 mb-1">Device Info</p>
                    <p className="text-sm text-gray-700">
                      {device.deviceInfo.model} • {device.deviceInfo.os} {device.deviceInfo.version}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};