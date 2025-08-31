import React, { useEffect, useState } from 'react';
import type { User, Device } from '../types';
import { userService, deviceService } from '../services/api';
import { Shield, ShieldOff, ChevronRight, Smartphone } from 'lucide-react';
import { format } from 'date-fns';

export const Users: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [userDevices, setUserDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [blockingDevice, setBlockingDevice] = useState<string | null>(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  useEffect(() => {
    if (selectedUser) {
      fetchUserDevices(selectedUser.id);
    }
  }, [selectedUser]);

  const fetchUsers = async () => {
    try {
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (error) {
      console.error('Failed to fetch users:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUserDevices = async (userId: number) => {
    try {
      const devices = await userService.getUserDevices(userId);
      setUserDevices(devices);
    } catch (error) {
      console.error('Failed to fetch user devices:', error);
    }
  };

  const handleBlockDevice = async (deviceId: string) => {
    if (!selectedUser) return;
    
    setBlockingDevice(deviceId);
    try {
      await deviceService.blockDevice({
        deviceId,
        reason: 'Blocked by admin from bank dashboard',
        blockedBy: 'Bank Admin',
      });
      
      await fetchUserDevices(selectedUser.id);
      alert('Device blocked successfully in Aegis portal');
    } catch (error) {
      console.error('Failed to block device:', error);
      alert('Failed to block device');
    } finally {
      setBlockingDevice(null);
    }
  };

  const handleUnblockDevice = async (deviceId: string) => {
    if (!selectedUser) return;
    
    setBlockingDevice(deviceId);
    try {
      await deviceService.unblockDevice(deviceId);
      await fetchUserDevices(selectedUser.id);
      alert('Device unblocked successfully');
    } catch (error) {
      console.error('Failed to unblock device:', error);
      alert('Failed to unblock device');
    } finally {
      setBlockingDevice(null);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-gray-600">Loading users...</div>
      </div>
    );
  }

  return (
    <div className="flex h-full">
      <div className="w-1/3 border-r bg-white">
        <div className="p-4 border-b">
          <h2 className="text-lg font-semibold text-gray-900">Users</h2>
          <p className="text-sm text-gray-600">{users.length} total users</p>
        </div>
        <div className="overflow-y-auto">
          {users.map((user) => (
            <div
              key={user.id}
              onClick={() => setSelectedUser(user)}
              className={`p-4 border-b cursor-pointer hover:bg-gray-50 ${
                selectedUser?.id === user.id ? 'bg-blue-50' : ''
              }`}
            >
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="font-medium text-gray-900">{user.name}</h3>
                  <p className="text-sm text-gray-600">{user.username}</p>
                  <p className="text-xs text-gray-500">
                    {user.deviceIds.length} device{user.deviceIds.length !== 1 ? 's' : ''}
                  </p>
                </div>
                <ChevronRight className="w-5 h-5 text-gray-400" />
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="flex-1 bg-gray-50">
        {selectedUser ? (
          <div className="p-6">
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4">User Details</h2>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-600">Name</p>
                  <p className="font-medium">{selectedUser.name}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Username</p>
                  <p className="font-medium">{selectedUser.username}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Email</p>
                  <p className="font-medium">{selectedUser.email}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Status</p>
                  <span
                    className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      selectedUser.status === 'active'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-red-100 text-red-800'
                    }`}
                  >
                    {selectedUser.status}
                  </span>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Created</p>
                  <p className="font-medium">
                    {format(new Date(selectedUser.createdAt), 'MMM dd, yyyy')}
                  </p>
                </div>
                {selectedUser.lastLogin && (
                  <div>
                    <p className="text-sm text-gray-600">Last Login</p>
                    <p className="font-medium">
                      {format(new Date(selectedUser.lastLogin), 'MMM dd, yyyy HH:mm')}
                    </p>
                  </div>
                )}
              </div>
            </div>

            <div className="bg-white rounded-lg shadow">
              <div className="px-6 py-4 border-b flex justify-between items-center">
                <h3 className="text-lg font-semibold text-gray-900">Associated Devices</h3>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Smartphone className="w-4 h-4" />
                  {userDevices.length} device{userDevices.length !== 1 ? 's' : ''}
                </div>
              </div>
              <div className="divide-y">
                {userDevices.map((device) => (
                  <div key={device.deviceId} className="p-6">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <p className="font-mono text-sm font-medium text-gray-900">
                          {device.deviceId}
                        </p>
                        <div className="mt-2 space-y-1">
                          <p className="text-sm text-gray-600">
                            Status:{' '}
                            <span
                              className={`font-medium ${
                                device.status === 'active' ? 'text-green-600' : 'text-red-600'
                              }`}
                            >
                              {device.status}
                            </span>
                          </p>
                          <p className="text-sm text-gray-600">
                            Registered: {format(new Date(device.registeredAt), 'MMM dd, yyyy HH:mm')}
                          </p>
                          <p className="text-sm text-gray-600">
                            Last Used: {format(new Date(device.lastUsed), 'MMM dd, yyyy HH:mm')}
                          </p>
                          {device.deviceInfo && (
                            <p className="text-sm text-gray-600">
                              Device: {device.deviceInfo.model} ({device.deviceInfo.os} {device.deviceInfo.version})
                            </p>
                          )}
                        </div>
                      </div>
                      <div className="ml-4">
                        {device.status === 'active' ? (
                          <button
                            onClick={() => handleBlockDevice(device.deviceId)}
                            disabled={blockingDevice === device.deviceId}
                            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50 flex items-center gap-2"
                          >
                            <ShieldOff className="w-4 h-4" />
                            {blockingDevice === device.deviceId ? 'Blocking...' : 'Block Device'}
                          </button>
                        ) : (
                          <button
                            onClick={() => handleUnblockDevice(device.deviceId)}
                            disabled={blockingDevice === device.deviceId}
                            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 flex items-center gap-2"
                          >
                            <Shield className="w-4 h-4" />
                            {blockingDevice === device.deviceId ? 'Unblocking...' : 'Unblock Device'}
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="flex items-center justify-center h-full text-gray-500">
            Select a user to view details
          </div>
        )}
      </div>
    </div>
  );
};