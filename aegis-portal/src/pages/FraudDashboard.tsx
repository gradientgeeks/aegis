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
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Chip,
  Alert,
  CircularProgress,
  InputAdornment,
  Paper,
} from '@mui/material';
import {
  Warning as AlertTriangleIcon,
  Shield as ShieldIcon,
  TrendingUp,
  TrendingDown,
  CalendarToday as CalendarIcon,
  Download as DownloadIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  AccessTime as ClockIcon,
  Smartphone as SmartphoneIcon,
} from '@mui/icons-material';
import { fraudService, deviceService } from '../services/api';
import { type FraudStatistics } from '../types';

interface FraudReport {
  id: string;
  deviceId: string;
  clientId: string;
  bankTransactionId: string;
  reasonCode: string;
  description: string;
  timestamp: string;
  status: 'PENDING' | 'PROCESSED' | 'ESCALATED';
  actionTaken?: string;
}

interface FraudTrend {
  date: string;
  count: number;
  severity: 'low' | 'medium' | 'high';
}

const FraudDashboard: React.FC = () => {
  const [fraudStats, setFraudStats] = useState<FraudStatistics | null>(null);
  const [recentReports, setRecentReports] = useState<FraudReport[]>([]);
  const [fraudTrends, setFraudTrends] = useState<FraudTrend[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedPeriod, setSelectedPeriod] = useState('7d');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');

  useEffect(() => {
    loadDashboardData();
  }, [selectedPeriod]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      // Load fraud statistics
      const stats = await fraudService.getFraudStatistics(selectedPeriod);
      setFraudStats(stats);
      
      // Load recent fraud reports
      const reports = await fraudService.getRecentFraudReports(20);
      setRecentReports(reports);
      
      // Generate mock fraud trends data
      setFraudTrends(generateMockTrends());
      
    } catch (error) {
      console.error('Error loading fraud dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const generateMockTrends = (): FraudTrend[] => {
    const trends: FraudTrend[] = [];
    const days = selectedPeriod === '7d' ? 7 : selectedPeriod === '30d' ? 30 : 90;
    
    for (let i = days - 1; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      
      trends.push({
        date: date.toISOString().split('T')[0],
        count: Math.floor(Math.random() * 20) + 1,
        severity: ['low', 'medium', 'high'][Math.floor(Math.random() * 3)] as 'low' | 'medium' | 'high'
      });
    }
    
    return trends;
  };

  const getReasonCodeColor = (reasonCode: string) => {
    switch (reasonCode) {
      case 'CONFIRMED_FRAUD': return 'error';
      case 'BANK_ML_HIGH_RISK': return 'warning';
      case 'SUSPICIOUS_ACTIVITY': return 'info';
      default: return 'default';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PROCESSED': return 'success';
      case 'PENDING': return 'warning';
      case 'ESCALATED': return 'error';
      default: return 'default';
    }
  };

  const filteredReports = recentReports.filter(report => {
    const matchesSearch = !searchTerm || 
      report.deviceId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      report.clientId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      report.bankTransactionId.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = !statusFilter || report.status === statusFilter;
    
    return matchesSearch && matchesStatus;
  });

  const exportReports = () => {
    const csvContent = [
      ['Device ID', 'Organization', 'Transaction ID', 'Reason', 'Description', 'Timestamp', 'Status'],
      ...filteredReports.map(report => [
        report.deviceId,
        report.clientId,
        report.bankTransactionId,
        report.reasonCode,
        report.description,
        report.timestamp,
        report.status
      ])
    ].map(row => row.join(',')).join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `fraud-reports-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
        <Box sx={{ textAlign: 'center' }}>
          <CircularProgress size={48} />
          <Typography variant="body1" sx={{ mt: 2 }}>
            Loading fraud dashboard...
          </Typography>
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <div>
          <Typography variant="h4" component="h1" gutterBottom>
            Fraud Dashboard
          </Typography>
          <Typography variant="body1" color="textSecondary">
            Monitor fraud detection and device security across your network
          </Typography>
        </div>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Period</InputLabel>
            <Select
              value={selectedPeriod}
              onChange={(e) => setSelectedPeriod(e.target.value)}
              label="Period"
            >
              <MenuItem value="7d">Last 7 days</MenuItem>
              <MenuItem value="30d">Last 30 days</MenuItem>
              <MenuItem value="90d">Last 90 days</MenuItem>
            </Select>
          </FormControl>
          <Button
            variant="contained"
            onClick={exportReports}
            startIcon={<DownloadIcon />}
          >
            Export
          </Button>
        </Box>
      </Box>

      {/* Key Metrics */}
      {fraudStats && (
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Box sx={{ 
                    p: 1, 
                    backgroundColor: 'error.light', 
                    borderRadius: 1, 
                    mr: 2,
                    display: 'flex',
                    alignItems: 'center'
                  }}>
                    <AlertTriangleIcon sx={{ color: 'error.main' }} />
                  </Box>
                  <div>
                    <Typography variant="body2" color="textSecondary" fontWeight="medium">
                      Total Fraud Reports
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      {fraudStats.recentReports}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 0.5 }}>
                      <TrendingDown sx={{ fontSize: 12, color: 'success.main', mr: 0.5 }} />
                      <Typography variant="caption" color="success.main">
                        -12% from last period
                      </Typography>
                    </Box>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Box sx={{ 
                    p: 1, 
                    backgroundColor: 'warning.light', 
                    borderRadius: 1, 
                    mr: 2,
                    display: 'flex',
                    alignItems: 'center'
                  }}>
                    <ShieldIcon sx={{ color: 'warning.main' }} />
                  </Box>
                  <div>
                    <Typography variant="body2" color="textSecondary" fontWeight="medium">
                      Devices Blocked
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      {fraudStats.blockedDevices}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 0.5 }}>
                      <TrendingUp sx={{ fontSize: 12, color: 'error.main', mr: 0.5 }} />
                      <Typography variant="caption" color="error.main">
                        +8% from last period
                      </Typography>
                    </Box>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Box sx={{ 
                    p: 1, 
                    backgroundColor: 'info.light', 
                    borderRadius: 1, 
                    mr: 2,
                    display: 'flex',
                    alignItems: 'center'
                  }}>
                    <SmartphoneIcon sx={{ color: 'info.main' }} />
                  </Box>
                  <div>
                    <Typography variant="body2" color="textSecondary" fontWeight="medium">
                      Fraudulent Devices
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      {fraudStats.fraudulentDevices}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 0.5 }}>
                      <TrendingUp sx={{ fontSize: 12, color: 'error.main', mr: 0.5 }} />
                      <Typography variant="caption" color="error.main">
                        +5% from last period
                      </Typography>
                    </Box>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Box sx={{ 
                    p: 1, 
                    backgroundColor: 'primary.light', 
                    borderRadius: 1, 
                    mr: 2,
                    display: 'flex',
                    alignItems: 'center'
                  }}>
                    <CalendarIcon sx={{ color: 'primary.main' }} />
                  </Box>
                  <div>
                    <Typography variant="body2" color="textSecondary" fontWeight="medium">
                      Detection Rate
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      94.2%
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mt: 0.5 }}>
                      <TrendingUp sx={{ fontSize: 12, color: 'success.main', mr: 0.5 }} />
                      <Typography variant="caption" color="success.main">
                        +2.1% from last period
                      </Typography>
                    </Box>
                  </div>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Fraud Trends Chart */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" component="h3" gutterBottom>
            Fraud Detection Trends
          </Typography>
          <Box sx={{ height: 200, display: 'flex', alignItems: 'end', justifyContent: 'space-between', gap: 0.5 }}>
            {fraudTrends.map((trend, index) => (
              <Box key={index} sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flex: 1 }}>
                <Box
                  sx={{
                    width: '100%',
                    backgroundColor: trend.severity === 'high' ? 'error.main' :
                                   trend.severity === 'medium' ? 'warning.main' : 'success.main',
                    borderRadius: '4px 4px 0 0',
                    minHeight: 4,
                    height: `${(trend.count / 20) * 160}px`,
                    transition: 'all 0.3s ease',
                    '&:hover': {
                      opacity: 0.8,
                      cursor: 'pointer'
                    }
                  }}
                  title={`${trend.date}: ${trend.count} reports`}
                />
                <Typography 
                  variant="caption" 
                  color="textSecondary" 
                  sx={{ 
                    mt: 1, 
                    transform: 'rotate(45deg)', 
                    transformOrigin: 'left',
                    fontSize: '0.7rem'
                  }}
                >
                  {new Date(trend.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                </Typography>
              </Box>
            ))}
          </Box>
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 3, mt: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Box sx={{ width: 12, height: 12, backgroundColor: 'success.main', borderRadius: 0.5, mr: 1 }} />
              <Typography variant="body2">Low Risk</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Box sx={{ width: 12, height: 12, backgroundColor: 'warning.main', borderRadius: 0.5, mr: 1 }} />
              <Typography variant="body2">Medium Risk</Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Box sx={{ width: 12, height: 12, backgroundColor: 'error.main', borderRadius: 0.5, mr: 1 }} />
              <Typography variant="body2">High Risk</Typography>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Recent Fraud Reports */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6" component="h3">
              Recent Fraud Reports
            </Typography>
            <Typography variant="body2" color="textSecondary">
              {filteredReports.length} reports
            </Typography>
          </Box>
          
          {/* Search and Filter */}
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                size="small"
                placeholder="Search device ID, organization, or transaction..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  label="Status"
                >
                  <MenuItem value="">All Statuses</MenuItem>
                  <MenuItem value="PENDING">Pending</MenuItem>
                  <MenuItem value="PROCESSED">Processed</MenuItem>
                  <MenuItem value="ESCALATED">Escalated</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="outlined"
                onClick={() => {
                  setSearchTerm('');
                  setStatusFilter('');
                }}
                startIcon={<FilterIcon />}
                sx={{ height: 40 }}
              >
                Clear
              </Button>
            </Grid>
          </Grid>
        </CardContent>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Device & Transaction</TableCell>
                <TableCell>Organization</TableCell>
                <TableCell>Reason</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Reported</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredReports.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    <Typography color="textSecondary">
                      No fraud reports found
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                filteredReports.map((report) => (
                  <TableRow key={report.id} hover>
                    <TableCell>
                      <div>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <SmartphoneIcon sx={{ fontSize: 14, mr: 1, color: 'text.secondary' }} />
                          <Typography variant="body2" fontWeight="medium">
                            {report.deviceId}
                          </Typography>
                        </Box>
                        <Typography variant="caption" color="textSecondary" sx={{ mt: 0.5, display: 'block' }}>
                          {report.bankTransactionId}
                        </Typography>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {report.clientId}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={report.reasonCode.replace('_', ' ')}
                        color={getReasonCodeColor(report.reasonCode)}
                        size="small"
                      />
                      {report.description && (
                        <Typography variant="caption" color="textSecondary" sx={{ mt: 0.5, display: 'block', maxWidth: 200 }}>
                          {report.description}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={report.status}
                        color={getStatusColor(report.status)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <ClockIcon sx={{ fontSize: 12, mr: 0.5 }} />
                        <Typography variant="body2">
                          {new Date(report.timestamp).toLocaleString()}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => {
                          window.location.href = `/device-management?deviceId=${report.deviceId}`;
                        }}
                      >
                        View Device
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </Box>
  );
};

export default FraudDashboard;