import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  Pagination,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Block as BlockIcon,
  CheckCircle as CheckCircleIcon,
  Warning as WarningIcon,
  Smartphone as SmartphoneIcon,
  Visibility as VisibilityIcon,
  Info as InfoIcon,
} from '@mui/icons-material';
import { deviceService, fraudService } from '../services/api';
import type { 
  Device, 
  DeviceSearchParams, 
  DeviceStatus, 
  FraudStatistics 
} from '../types';

const DeviceManagement: React.FC = () => {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState<DeviceSearchParams>({
    page: 0,
    size: 20
  });
  const [pagination, setPagination] = useState<any>({});
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);
  const [showBlockModal, setShowBlockModal] = useState(false);
  const [fraudStats, setFraudStats] = useState<FraudStatistics | null>(null);
  const [blockReason, setBlockReason] = useState('');
  const [blockType, setBlockType] = useState<'TEMPORARILY_BLOCKED' | 'PERMANENTLY_BLOCKED'>('TEMPORARILY_BLOCKED');
  const [alertMessage, setAlertMessage] = useState<{ type: 'success' | 'error', message: string } | null>(null);

  useEffect(() => {
    loadDevices();
    loadFraudStatistics();
  }, [searchParams]);

  const loadDevices = async () => {
    try {
      setLoading(true);
      const response = await deviceService.searchDevices(searchParams);
      setDevices(response.devices);
      setPagination(response.pagination);
    } catch (error) {
      console.error('Error loading devices:', error);
      setAlertMessage({ type: 'error', message: 'Failed to load devices' });
    } finally {
      setLoading(false);
    }
  };

  const loadFraudStatistics = async () => {
    try {
      const stats = await fraudService.getFraudStatistics();
      setFraudStats(stats);
    } catch (error) {
      console.error('Error loading fraud statistics:', error);
    }
  };

  const handleSearch = (field: keyof DeviceSearchParams, value: string) => {
    setSearchParams(prev => ({
      ...prev,
      [field]: value || undefined,
      page: 0
    }));
  };

  const handleBlockDevice = async () => {
    if (!selectedDevice || !blockReason.trim()) return;
    
    try {
      await deviceService.blockDevice(selectedDevice.deviceId, blockReason, blockType);
      setShowBlockModal(false);
      setBlockReason('');
      loadDevices();
      setAlertMessage({ type: 'success', message: 'Device blocked successfully' });
    } catch (error) {
      console.error('Error blocking device:', error);
      setAlertMessage({ type: 'error', message: 'Failed to block device' });
    }
  };

  const handleUnblockDevice = async (device: Device) => {
    const reason = prompt('Enter reason for unblocking:');
    if (!reason) return;

    try {
      await deviceService.unblockDevice(device.deviceId, reason);
      loadDevices();
      setAlertMessage({ type: 'success', message: 'Device unblocked successfully' });
    } catch (error) {
      console.error('Error unblocking device:', error);
      setAlertMessage({ type: 'error', message: 'Failed to unblock device' });
    }
  };

  const handleMarkAsFraudulent = async (device: Device) => {
    const reason = prompt('Enter reason for marking as fraudulent:');
    if (!reason) return;

    try {
      await deviceService.markDeviceAsFraudulent(device.deviceId, reason);
      loadDevices();
      setAlertMessage({ type: 'success', message: 'Device marked as fraudulent' });
    } catch (error) {
      console.error('Error marking device as fraudulent:', error);
      setAlertMessage({ type: 'error', message: 'Failed to mark device as fraudulent' });
    }
  };

  const getStatusColor = (status: DeviceStatus) => {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'TEMPORARILY_BLOCKED': return 'warning';
      case 'PERMANENTLY_BLOCKED': return 'error';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: DeviceStatus) => {
    switch (status) {
      case 'ACTIVE': return <CheckCircleIcon />;
      case 'TEMPORARILY_BLOCKED': return <WarningIcon />;
      case 'PERMANENTLY_BLOCKED': return <BlockIcon />;
      default: return <SmartphoneIcon />;
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Alert Messages */}
      {alertMessage && (
        <Alert 
          severity={alertMessage.type} 
          onClose={() => setAlertMessage(null)}
          sx={{ mb: 2 }}
        >
          {alertMessage.message}
        </Alert>
      )}

      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <div>
          <Typography variant="h4" component="h1" gutterBottom>
            Device Management
          </Typography>
          <Typography variant="body1" color="textSecondary">
            Monitor and manage device security across your organization
          </Typography>
        </div>
        <Button
          variant="contained"
          onClick={loadDevices}
          startIcon={<RefreshIcon />}
        >
          Refresh
        </Button>
      </Box>

      {/* Fraud Statistics Cards */}
      {fraudStats && (
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <SmartphoneIcon sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
                  <div>
                    <Typography variant="body2" color="textSecondary">
                      Total Devices
                    </Typography>
                    <Typography variant="h4">
                      {fraudStats.totalDevices}
                    </Typography>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <BlockIcon sx={{ fontSize: 40, color: 'warning.main', mr: 2 }} />
                  <div>
                    <Typography variant="body2" color="textSecondary">
                      Blocked Devices
                    </Typography>
                    <Typography variant="h4">
                      {fraudStats.blockedDevices}
                    </Typography>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <WarningIcon sx={{ fontSize: 40, color: 'error.main', mr: 2 }} />
                  <div>
                    <Typography variant="body2" color="textSecondary">
                      Fraudulent Devices
                    </Typography>
                    <Typography variant="h4">
                      {fraudStats.fraudulentDevices}
                    </Typography>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <CheckCircleIcon sx={{ fontSize: 40, color: 'success.main', mr: 2 }} />
                  <div>
                    <Typography variant="body2" color="textSecondary">
                      Recent Reports
                    </Typography>
                    <Typography variant="h4">
                      {fraudStats.recentReports}
                    </Typography>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Search and Filter */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                label="Device ID"
                placeholder="Search device ID..."
                value={searchParams.deviceId || ''}
                onChange={(e) => handleSearch('deviceId', e.target.value)}
                InputProps={{
                  endAdornment: <SearchIcon color="action" />
                }}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                label="Organization"
                placeholder="Client ID..."
                value={searchParams.clientId || ''}
                onChange={(e) => handleSearch('clientId', e.target.value)}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={searchParams.status || ''}
                  onChange={(e) => handleSearch('status', e.target.value)}
                  label="Status"
                >
                  <MenuItem value="">All Statuses</MenuItem>
                  <MenuItem value="ACTIVE">Active</MenuItem>
                  <MenuItem value="TEMPORARILY_BLOCKED">Temporarily Blocked</MenuItem>
                  <MenuItem value="PERMANENTLY_BLOCKED">Permanently Blocked</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <Button
                fullWidth
                variant="outlined"
                onClick={() => setSearchParams({ page: 0, size: 20 })}
                sx={{ height: 56 }}
              >
                Clear Filters
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Device List */}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>
                  Device
                  <Tooltip title="Device IDs may include bank suffix (e.g. _UCOBANK_PROD_ANDROID) for multi-bank support">
                    <InfoIcon fontSize="small" sx={{ ml: 0.5, color: 'text.secondary' }} />
                  </Tooltip>
                </TableCell>
                <TableCell>Organization</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Registration</TableCell>
                <TableCell>Last Activity</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 3 }}>
                      <CircularProgress sx={{ mr: 2 }} />
                      Loading devices...
                    </Box>
                  </TableCell>
                </TableRow>
              ) : devices.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    No devices found
                  </TableCell>
                </TableRow>
              ) : (
                devices.map((device) => (
                  <TableRow key={device.deviceId}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <SmartphoneIcon sx={{ mr: 1, color: 'text.secondary' }} />
                        <div>
                          <Typography variant="body2" fontWeight="medium">
                            {device.deviceId}
                          </Typography>
                          {device.hardwareInfo && (
                            <Typography variant="caption" color="textSecondary">
                              {device.hardwareInfo.manufacturer} {device.hardwareInfo.model}
                            </Typography>
                          )}
                          {device.isMultiBankDevice && (
                            <Chip
                              label="Multi-Bank"
                              size="small"
                              color="info"
                              sx={{ mt: 0.5 }}
                            />
                          )}
                        </div>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Box>
                        <Typography variant="body2">{device.clientId}</Typography>
                        {device.relatedBanks && device.relatedBanks.length > 1 && (
                          <Typography variant="caption" color="textSecondary">
                            Also used by: {device.relatedBanks.filter(b => b !== device.clientId).join(', ')}
                          </Typography>
                        )}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Chip
                          icon={getStatusIcon(device.status)}
                          label={device.status.replace('_', ' ')}
                          color={getStatusColor(device.status)}
                          size="small"
                        />
                        {device.isFraudulent && (
                          <Chip
                            icon={<WarningIcon />}
                            label="Fraudulent"
                            color="error"
                            size="small"
                          />
                        )}
                      </Box>
                    </TableCell>
                    <TableCell>
                      {new Date(device.registrationDate).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      {new Date(device.lastActivity).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Tooltip title="View Details">
                          <IconButton
                            size="small"
                            onClick={() => setSelectedDevice(device)}
                          >
                            <VisibilityIcon />
                          </IconButton>
                        </Tooltip>
                        {device.status === 'ACTIVE' && (
                          <Tooltip title="Block Device">
                            <IconButton
                              size="small"
                              color="warning"
                              onClick={() => {
                                setSelectedDevice(device);
                                setShowBlockModal(true);
                              }}
                            >
                              <BlockIcon />
                            </IconButton>
                          </Tooltip>
                        )}
                        {device.status !== 'ACTIVE' && (
                          <Tooltip title="Unblock Device">
                            <IconButton
                              size="small"
                              color="success"
                              onClick={() => handleUnblockDevice(device)}
                            >
                              <CheckCircleIcon />
                            </IconButton>
                          </Tooltip>
                        )}
                        {!device.isFraudulent && (
                          <Tooltip title="Mark as Fraudulent">
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => handleMarkAsFraudulent(device)}
                            >
                              <WarningIcon />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <Pagination
              count={pagination.totalPages}
              page={pagination.page + 1}
              onChange={(_, page) => setSearchParams(prev => ({ ...prev, page: page - 1 }))}
              color="primary"
            />
          </Box>
        )}
      </Card>

      {/* Block Device Modal */}
      <Dialog open={showBlockModal} onClose={() => setShowBlockModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          Block Device: {selectedDevice?.deviceId}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            <Alert severity="info" sx={{ mb: 2 }}>
              Blocking this device will prevent it from accessing ALL banking apps registered with this device. 
              This ensures comprehensive security across all financial institutions.
            </Alert>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Block Type</InputLabel>
              <Select
                value={blockType}
                onChange={(e) => setBlockType(e.target.value as 'TEMPORARILY_BLOCKED' | 'PERMANENTLY_BLOCKED')}
                label="Block Type"
              >
                <MenuItem value="TEMPORARILY_BLOCKED">Temporary Block</MenuItem>
                <MenuItem value="PERMANENTLY_BLOCKED">Permanent Block</MenuItem>
              </Select>
            </FormControl>
            <TextField
              fullWidth
              multiline
              rows={4}
              label="Reason"
              placeholder="Enter reason for blocking this device..."
              value={blockReason}
              onChange={(e) => setBlockReason(e.target.value)}
              required
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setShowBlockModal(false);
            setBlockReason('');
          }}>
            Cancel
          </Button>
          <Button
            onClick={handleBlockDevice}
            disabled={!blockReason.trim()}
            variant="contained"
            color="error"
          >
            Block Device
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DeviceManagement;