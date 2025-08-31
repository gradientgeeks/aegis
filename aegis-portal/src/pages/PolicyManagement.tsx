import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  InputAdornment,
  Tooltip,
  Alert,
  CircularProgress,
  Snackbar,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Badge,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  ExpandMore as ExpandMoreIcon,
  Shield as PolicyIcon,
  Warning as ViolationIcon,
  History as HistoryIcon,
  Rule as RuleIcon,
  Visibility as ViewIcon,
  Security as SecurityIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { policyService } from '../services/api';
import { authService } from '../services/auth';
import { type Policy, type PolicyViolation, type PolicyType, type EnforcementLevel } from '../types';
import { format } from 'date-fns';

const PolicyManagement: React.FC = () => {
  const navigate = useNavigate();
  const user = authService.getCurrentUser().user;
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [filteredPolicies, setFilteredPolicies] = useState<Policy[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState<PolicyType | 'ALL'>('ALL');
  const [filterLevel, setFilterLevel] = useState<EnforcementLevel | 'ALL'>('ALL');
  const [selectedPolicy, setSelectedPolicy] = useState<Policy | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showViewDialog, setShowViewDialog] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  useEffect(() => {
    fetchPolicies();
  }, []);

  useEffect(() => {
    let filtered = policies;

    // Apply search filter
    if (searchTerm) {
      filtered = filtered.filter(policy =>
        policy.policyName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (policy.description && policy.description.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    // Apply type filter
    if (filterType !== 'ALL') {
      filtered = filtered.filter(policy => policy.policyType === filterType);
    }

    // Apply enforcement level filter
    if (filterLevel !== 'ALL') {
      filtered = filtered.filter(policy => policy.enforcementLevel === filterLevel);
    }

    setFilteredPolicies(filtered);
  }, [policies, searchTerm, filterType, filterLevel]);

  const fetchPolicies = async () => {
    try {
      setLoading(true);
      if (user?.organization) {
        // Fetch policies for the user's organization
        const data = await policyService.getPoliciesByOrganization();
        setPolicies(data);
        setFilteredPolicies(data);
      }
    } catch (err) {
      setError('Failed to fetch policies');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeletePolicy = async () => {
    if (!selectedPolicy?.id) return;
    
    try {
      await policyService.deletePolicy(selectedPolicy.id);
      setPolicies(policies.filter(p => p.id !== selectedPolicy.id));
      setSnackbar({ open: true, message: 'Policy deleted successfully', severity: 'success' });
      setShowDeleteDialog(false);
      setSelectedPolicy(null);
    } catch (err) {
      setSnackbar({ open: true, message: 'Failed to delete policy', severity: 'error' });
    }
  };

  const handleTogglePolicy = async (policy: Policy) => {
    try {
      const updatedPolicy = await policyService.updatePolicyStatus(policy.id!, !policy.isActive);
      setPolicies(policies.map(p => p.id === policy.id ? updatedPolicy : p));
      setSnackbar({ 
        open: true, 
        message: `Policy ${updatedPolicy.isActive ? 'activated' : 'deactivated'} successfully`, 
        severity: 'success' 
      });
    } catch (err) {
      console.error('Failed to toggle policy status:', err);
      setSnackbar({ open: true, message: 'Failed to update policy status', severity: 'error' });
    }
  };

  const getPolicyTypeColor = (type: PolicyType) => {
    const colors: Record<PolicyType, string> = {
      'DEVICE_SECURITY': 'error',
      'TRANSACTION_LIMIT': 'warning',
      'GEOGRAPHIC_RESTRICTION': 'info',
      'TIME_RESTRICTION': 'secondary',
      'DEVICE_BINDING': 'primary',
      'RISK_ASSESSMENT': 'error',
      'API_RATE_LIMIT': 'default',
      'AUTHENTICATION_REQUIREMENT': 'success',
    };
    return colors[type] || 'default';
  };

  const getEnforcementLevelColor = (level: EnforcementLevel) => {
    const colors: Record<EnforcementLevel, string> = {
      'BLOCK': 'error',
      'REQUIRE_MFA': 'warning',
      'WARN': 'info',
      'NOTIFY': 'secondary',
      'MONITOR': 'default',
    };
    return colors[level] || 'default';
  };

  const formatPolicyType = (type: PolicyType) => {
    return type.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  const formatEnforcementLevel = (level: EnforcementLevel) => {
    return level.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight="bold">
            <PolicyIcon sx={{ mr: 1, verticalAlign: 'bottom' }} />
            Policy Management
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Configure security policies for {user?.organization || 'your organization'}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/policies/create')}
        >
          Create New Policy
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <SecurityIcon sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} />
              <Typography variant="h6" fontWeight="bold">
                {policies.length}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Total Policies
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <PolicyIcon sx={{ fontSize: 40, color: 'success.main', mb: 1 }} />
              <Typography variant="h6" fontWeight="bold">
                {policies.filter(p => p.isActive).length}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Active Policies
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <ViolationIcon sx={{ fontSize: 40, color: 'error.main', mb: 1 }} />
              <Typography variant="h6" fontWeight="bold">
                {policies.filter(p => p.enforcementLevel === 'BLOCK').length}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Blocking Policies
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <RuleIcon sx={{ fontSize: 40, color: 'info.main', mb: 1 }} />
              <Typography variant="h6" fontWeight="bold">
                {policies.reduce((sum, p) => sum + (p.rules?.length || 0), 0)}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Total Rules
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              placeholder="Search policies..."
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
          <Grid item xs={12} md={4}>
            <FormControl fullWidth>
              <InputLabel>Policy Type</InputLabel>
              <Select
                value={filterType}
                label="Policy Type"
                onChange={(e) => setFilterType(e.target.value as PolicyType | 'ALL')}
              >
                <MenuItem value="ALL">All Types</MenuItem>
                <MenuItem value="DEVICE_SECURITY">Device Security</MenuItem>
                <MenuItem value="TRANSACTION_LIMIT">Transaction Limit</MenuItem>
                <MenuItem value="GEOGRAPHIC_RESTRICTION">Geographic Restriction</MenuItem>
                <MenuItem value="TIME_RESTRICTION">Time Restriction</MenuItem>
                <MenuItem value="DEVICE_BINDING">Device Binding</MenuItem>
                <MenuItem value="RISK_ASSESSMENT">Risk Assessment</MenuItem>
                <MenuItem value="API_RATE_LIMIT">API Rate Limit</MenuItem>
                <MenuItem value="AUTHENTICATION_REQUIREMENT">Authentication Requirement</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={4}>
            <FormControl fullWidth>
              <InputLabel>Enforcement Level</InputLabel>
              <Select
                value={filterLevel}
                label="Enforcement Level"
                onChange={(e) => setFilterLevel(e.target.value as EnforcementLevel | 'ALL')}
              >
                <MenuItem value="ALL">All Levels</MenuItem>
                <MenuItem value="BLOCK">Block</MenuItem>
                <MenuItem value="REQUIRE_MFA">Require MFA</MenuItem>
                <MenuItem value="WARN">Warn</MenuItem>
                <MenuItem value="NOTIFY">Notify</MenuItem>
                <MenuItem value="MONITOR">Monitor</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Policies Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Policy Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Enforcement Level</TableCell>
              <TableCell>Rules</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredPolicies.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="textSecondary">
                    {searchTerm || filterType !== 'ALL' || filterLevel !== 'ALL' 
                      ? 'No policies match your search criteria' 
                      : 'No policies found'
                    }
                  </Typography>
                  <Button
                    variant="contained"
                    sx={{ mt: 2 }}
                    onClick={() => navigate('/policies/create')}
                  >
                    Create Your First Policy
                  </Button>
                </TableCell>
              </TableRow>
            ) : (
              filteredPolicies.map((policy) => (
                <TableRow key={policy.id} hover>
                  <TableCell>
                    <Box>
                      <Typography variant="body1" fontWeight="medium">
                        {policy.policyName}
                      </Typography>
                      {policy.description && (
                        <Typography variant="body2" color="textSecondary">
                          {policy.description}
                        </Typography>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={formatPolicyType(policy.policyType)}
                      color={getPolicyTypeColor(policy.policyType) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={formatEnforcementLevel(policy.enforcementLevel)}
                      color={getEnforcementLevelColor(policy.enforcementLevel) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Badge badgeContent={policy.rules?.length || 0} color="primary">
                      <RuleIcon />
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <FormControlLabel
                      control={
                        <Switch
                          checked={policy.isActive || false}
                          onChange={() => handleTogglePolicy(policy)}
                          size="small"
                        />
                      }
                      label={policy.isActive ? 'Active' : 'Inactive'}
                    />
                  </TableCell>
                  <TableCell>
                    {policy.createdAt ? format(new Date(policy.createdAt), 'MMM dd, yyyy') : '-'}
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="View Details">
                      <IconButton
                        size="small"
                        onClick={() => {
                          setSelectedPolicy(policy);
                          setShowViewDialog(true);
                        }}
                      >
                        <ViewIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Edit Policy">
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/policies/${policy.id}/edit`)}
                      >
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="View Violations">
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/policies/${policy.id}/violations`)}
                      >
                        <HistoryIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete Policy">
                      <IconButton
                        size="small"
                        onClick={() => {
                          setSelectedPolicy(policy);
                          setShowDeleteDialog(true);
                        }}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* View Policy Dialog */}
      <Dialog
        open={showViewDialog}
        onClose={() => setShowViewDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Policy Details</DialogTitle>
        <DialogContent>
          {selectedPolicy && (
            <Box>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Policy Name
                  </Typography>
                  <Typography variant="body1" mb={2}>
                    {selectedPolicy.policyName}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Type
                  </Typography>
                  <Chip
                    label={formatPolicyType(selectedPolicy.policyType)}
                    color={getPolicyTypeColor(selectedPolicy.policyType) as any}
                    size="small"
                    sx={{ mb: 2 }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Enforcement Level
                  </Typography>
                  <Chip
                    label={formatEnforcementLevel(selectedPolicy.enforcementLevel)}
                    color={getEnforcementLevelColor(selectedPolicy.enforcementLevel) as any}
                    size="small"
                    sx={{ mb: 2 }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Status
                  </Typography>
                  <Chip
                    label={selectedPolicy.isActive ? 'Active' : 'Inactive'}
                    color={selectedPolicy.isActive ? 'success' : 'default'}
                    size="small"
                    sx={{ mb: 2 }}
                  />
                </Grid>
                {selectedPolicy.description && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="textSecondary">
                      Description
                    </Typography>
                    <Typography variant="body1" mb={2}>
                      {selectedPolicy.description}
                    </Typography>
                  </Grid>
                )}
              </Grid>

              {selectedPolicy.rules && selectedPolicy.rules.length > 0 && (
                <Box mt={3}>
                  <Typography variant="h6" mb={2}>
                    Policy Rules ({selectedPolicy.rules.length})
                  </Typography>
                  {selectedPolicy.rules.map((rule, index) => (
                    <Accordion key={rule.id || index}>
                      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="body1">
                          {rule.ruleName}
                        </Typography>
                      </AccordionSummary>
                      <AccordionDetails>
                        <Grid container spacing={2}>
                          <Grid item xs={12} sm={4}>
                            <Typography variant="body2" color="textSecondary">
                              Field
                            </Typography>
                            <Typography variant="body1">
                              {rule.conditionField}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={4}>
                            <Typography variant="body2" color="textSecondary">
                              Operator
                            </Typography>
                            <Typography variant="body1">
                              {rule.operator.replace(/_/g, ' ')}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={4}>
                            <Typography variant="body2" color="textSecondary">
                              Value
                            </Typography>
                            <Typography variant="body1">
                              {rule.conditionValue}
                            </Typography>
                          </Grid>
                          {rule.errorMessage && (
                            <Grid item xs={12}>
                              <Typography variant="body2" color="textSecondary">
                                Error Message
                              </Typography>
                              <Typography variant="body1">
                                {rule.errorMessage}
                              </Typography>
                            </Grid>
                          )}
                        </Grid>
                      </AccordionDetails>
                    </Accordion>
                  ))}
                </Box>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowViewDialog(false)}>Close</Button>
          <Button
            variant="contained"
            onClick={() => {
              setShowViewDialog(false);
              navigate(`/policies/${selectedPolicy?.id}/edit`);
            }}
          >
            Edit Policy
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Delete Policy</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete the policy "{selectedPolicy?.policyName}"? 
            This action cannot be undone.
          </Typography>
          {selectedPolicy?.isActive && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              This policy is currently active and will immediately stop being enforced.
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteDialog(false)}>Cancel</Button>
          <Button variant="contained" color="error" onClick={handleDeletePolicy}>
            Delete
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default PolicyManagement;