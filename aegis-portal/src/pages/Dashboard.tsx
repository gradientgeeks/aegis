import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  Paper,
  Chip,
  CircularProgress,
  Alert,
  Button,
} from '@mui/material';
import {
  Key as KeyIcon,
  CheckCircle as ActiveIcon,
  Cancel as InactiveIcon,
  Schedule as ExpiringIcon,
  TrendingUp as TrendingUpIcon,
  Business as BusinessIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { registrationKeyService } from '../services/api';
import { authService } from '../services/auth';
import { type RegistrationKey } from '../types';
import { format } from 'date-fns';

interface DashboardStats {
  total: number;
  active: number;
  inactive: number;
  expiringSoon: number;
}

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [keys, setKeys] = useState<RegistrationKey[]>([]);
  const [stats, setStats] = useState<DashboardStats>({
    total: 0,
    active: 0,
    inactive: 0,
    expiringSoon: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const user = authService.getCurrentUser();

  useEffect(() => {
    fetchRegistrationKeys();
  }, []);

  const fetchRegistrationKeys = async () => {
    try {
      setLoading(true);
      const data = await registrationKeyService.getAllRegistrationKeys();
      setKeys(data);
      
      // Calculate stats
      const now = new Date();
      const sevenDaysFromNow = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
      
      const stats: DashboardStats = {
        total: data.length,
        active: data.filter(key => key.isActive).length,
        inactive: data.filter(key => !key.isActive).length,
        expiringSoon: data.filter(key => {
          if (!key.expiresAt || !key.isActive) return false;
          const expiryDate = new Date(key.expiresAt);
          return expiryDate <= sevenDaysFromNow && expiryDate > now;
        }).length,
      };
      
      setStats(stats);
    } catch (err) {
      setError('Failed to fetch registration keys');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const StatCard: React.FC<{
    title: string;
    value: number;
    icon: React.ReactNode;
    color: string;
  }> = ({ title, value, icon, color }) => (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box>
            <Typography color="textSecondary" gutterBottom variant="body2">
              {title}
            </Typography>
            <Typography variant="h4" component="div" fontWeight="bold">
              {value}
            </Typography>
          </Box>
          <Box sx={{ color }}>
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <BusinessIcon sx={{ fontSize: 32, mr: 1, color: 'primary.main' }} />
        <Typography variant="h4" fontWeight="bold">
          {user.user?.organization || 'Dashboard'}
        </Typography>
      </Box>
      
      <Typography variant="body1" color="textSecondary" mb={3}>
        Welcome to Aegis Security Portal. Manage your organization's registration keys and API access.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Keys"
            value={stats.total}
            icon={<KeyIcon sx={{ fontSize: 40 }} />}
            color="#1976d2"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Keys"
            value={stats.active}
            icon={<ActiveIcon sx={{ fontSize: 40 }} />}
            color="#4caf50"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Inactive Keys"
            value={stats.inactive}
            icon={<InactiveIcon sx={{ fontSize: 40 }} />}
            color="#f44336"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Expiring Soon"
            value={stats.expiringSoon}
            icon={<ExpiringIcon sx={{ fontSize: 40 }} />}
            color="#ff9800"
          />
        </Grid>
      </Grid>

      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h6" fontWeight="bold">
            Recent Registration Keys
          </Typography>
          <Button
            variant="contained"
            startIcon={<KeyIcon />}
            onClick={() => navigate('/registration-keys')}
          >
            View All Keys
          </Button>
        </Box>

        {keys.length === 0 ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="body1" color="textSecondary" mb={2}>
              No registration keys found
            </Typography>
            <Button
              variant="contained"
              onClick={() => navigate('/registration-keys/create')}
            >
              Create Your First Key
            </Button>
          </Box>
        ) : (
          <Box>
            {keys.slice(0, 5).map((key) => (
              <Box
                key={key.id}
                sx={{
                  p: 2,
                  mb: 2,
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 1,
                  '&:hover': {
                    bgcolor: 'action.hover',
                  },
                }}
              >
                <Grid container alignItems="center" spacing={2}>
                  <Grid item xs={12} md={4}>
                    <Typography variant="subtitle1" fontWeight="bold">
                      {key.clientId}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      {key.description || 'No description'}
                    </Typography>
                  </Grid>
                  
                  <Grid item xs={12} md={3}>
                    <Typography variant="body2" color="textSecondary">
                      Created: {format(new Date(key.createdAt), 'MMM dd, yyyy')}
                    </Typography>
                    {key.expiresAt && (
                      <Typography variant="body2" color="textSecondary">
                        Expires: {format(new Date(key.expiresAt), 'MMM dd, yyyy')}
                      </Typography>
                    )}
                  </Grid>
                  
                  <Grid item xs={12} md={3}>
                    <Chip
                      label={key.isActive ? 'Active' : 'Inactive'}
                      color={key.isActive ? 'success' : 'default'}
                      size="small"
                    />
                  </Grid>
                  
                  <Grid item xs={12} md={2} sx={{ textAlign: 'right' }}>
                    <Button
                      size="small"
                      onClick={() => navigate('/registration-keys')}
                    >
                      View Details
                    </Button>
                  </Grid>
                </Grid>
              </Box>
            ))}
          </Box>
        )}
      </Paper>

      <Box sx={{ mt: 4 }}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" fontWeight="bold" mb={2}>
            Quick Actions
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="outlined"
                size="large"
                startIcon={<KeyIcon />}
                onClick={() => navigate('/registration-keys/create')}
              >
                Create New Key
              </Button>
            </Grid>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="outlined"
                size="large"
                startIcon={<TrendingUpIcon />}
                onClick={() => navigate('/registration-keys')}
              >
                View Activity
              </Button>
            </Grid>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="outlined"
                size="large"
                onClick={() => navigate('/profile')}
              >
                Organization Settings
              </Button>
            </Grid>
          </Grid>
        </Paper>
      </Box>
    </Box>
  );
};

export default Dashboard;