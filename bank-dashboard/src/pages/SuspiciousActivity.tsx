import React, { useEffect, useState } from 'react';
import { dashboardService, deviceService } from '../services/api';
import { AlertTriangle, ShieldOff, TrendingUp } from 'lucide-react';
import { format } from 'date-fns';

interface SuspiciousActivity {
  id: string;
  type: 'multiple_failed_transactions' | 'unusual_location' | 'rapid_transactions' | 'large_amount';
  userId: number;
  username: string;
  deviceId: string;
  description: string;
  timestamp: string;
  severity: 'low' | 'medium' | 'high';
  details: any;
}

export const SuspiciousActivity: React.FC = () => {
  const [activities, setActivities] = useState<SuspiciousActivity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSuspiciousActivities();
  }, []);

  const fetchSuspiciousActivities = async () => {
    try {
      const data = await dashboardService.getSuspiciousActivities();
      setActivities(data);
    } catch (error) {
      console.error('Failed to fetch suspicious activities:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleBlockDevice = async (deviceId: string) => {
    if (!confirm(`Are you sure you want to block device ${deviceId}?`)) return;

    try {
      await deviceService.blockDevice({
        deviceId,
        reason: 'Suspicious activity detected',
        blockedBy: 'Bank Security System',
      });
      alert('Device blocked successfully');
    } catch (error) {
      console.error('Failed to block device:', error);
      alert('Failed to block device');
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'high':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'medium':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'low':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'multiple_failed_transactions':
        return <AlertTriangle className="w-5 h-5" />;
      case 'rapid_transactions':
        return <TrendingUp className="w-5 h-5" />;
      default:
        return <AlertTriangle className="w-5 h-5" />;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-gray-600">Loading suspicious activities...</div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Suspicious Activities</h1>

      {activities.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <AlertTriangle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No Suspicious Activities</h3>
          <p className="text-gray-600">All transactions appear to be normal at this time.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {activities.map((activity) => (
            <div
              key={activity.id}
              className={`bg-white rounded-lg shadow border-l-4 ${
                activity.severity === 'high'
                  ? 'border-red-500'
                  : activity.severity === 'medium'
                  ? 'border-yellow-500'
                  : 'border-blue-500'
              }`}
            >
              <div className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-3">
                      <div className={`p-2 rounded-lg ${getSeverityColor(activity.severity)}`}>
                        {getActivityIcon(activity.type)}
                      </div>
                      <div>
                        <h3 className="font-semibold text-gray-900">
                          {activity.type.replace(/_/g, ' ').replace(/\b\w/g, (l) => l.toUpperCase())}
                        </h3>
                        <p className="text-sm text-gray-600">
                          {format(new Date(activity.timestamp), 'MMM dd, yyyy HH:mm:ss')}
                        </p>
                      </div>
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-semibold ${getSeverityColor(
                          activity.severity
                        )}`}
                      >
                        {activity.severity.toUpperCase()}
                      </span>
                    </div>

                    <p className="text-gray-700 mb-4">{activity.description}</p>

                    <div className="grid grid-cols-2 gap-4 mb-4">
                      <div>
                        <p className="text-sm text-gray-600">User</p>
                        <p className="font-medium">{activity.username} (ID: {activity.userId})</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Device ID</p>
                        <p className="font-mono text-sm">{activity.deviceId}</p>
                      </div>
                    </div>

                    {activity.details && (
                      <div className="bg-gray-50 rounded-lg p-4 mb-4">
                        <p className="text-sm font-medium text-gray-700 mb-2">Additional Details</p>
                        <pre className="text-xs text-gray-600 overflow-auto">
                          {JSON.stringify(activity.details, null, 2)}
                        </pre>
                      </div>
                    )}
                  </div>

                  <button
                    onClick={() => handleBlockDevice(activity.deviceId)}
                    className="ml-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 flex items-center gap-2"
                  >
                    <ShieldOff className="w-4 h-4" />
                    Block Device
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};