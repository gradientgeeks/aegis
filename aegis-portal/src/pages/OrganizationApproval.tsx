import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Alert,
  CircularProgress,
  Snackbar,
  Tabs,
  Tab,
  TextField,
} from '@mui/material';
import {
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  Refresh as RefreshIcon,
  Business as BusinessIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { adminService } from '../services/api';
import { authService } from '../services/auth';

interface Organization {
  id: number;
  email: string;
  name: string;
  organization: string;
  contactPerson: string;
  phone?: string;
  address?: string;
  role: string;
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  approvedAt?: string;
  approvedBy?: string;
  active: boolean;
}

const OrganizationApproval: React.FC = () => {
  const currentUser = authService.getCurrentUser().user;
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedOrg, setSelectedOrg] = useState<Organization | null>(null);
  const [actionDialog, setActionDialog] = useState<{ open: boolean; type: 'approve' | 'reject' | null }>({ open: false, type: null });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });
  const [tabValue, setTabValue] = useState(0);
  const [reason, setReason] = useState('');

  useEffect(() => {
    fetchOrganizations();
  }, []);

  const fetchOrganizations = async () => {
    try {
      setLoading(true);
      const data = await adminService.getAllOrganizations();
      setOrganizations(data);
    } catch (err) {
      setError('Failed to fetch organizations');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!selectedOrg) return;

    try {
      await adminService.approveOrganization(selectedOrg.id, currentUser?.email || 'Admin');
      setSnackbar({ open: true, message: 'Organization approved successfully', severity: 'success' });
      setActionDialog({ open: false, type: null });
      fetchOrganizations();
    } catch (err) {
      setSnackbar({ open: true, message: 'Failed to approve organization', severity: 'error' });
    }
  };

  const handleReject = async () => {
    if (!selectedOrg) return;

    try {
      await adminService.rejectOrganization(selectedOrg.id, currentUser?.email || 'Admin', reason);
      setSnackbar({ open: true, message: 'Organization rejected successfully', severity: 'success' });
      setActionDialog({ open: false, type: null });
      setReason('');
      fetchOrganizations();
    } catch (err) {
      setSnackbar({ open: true, message: 'Failed to reject organization', severity: 'error' });
    }
  };

  const openActionDialog = (org: Organization, type: 'approve' | 'reject') => {
    setSelectedOrg(org);
    setActionDialog({ open: true, type });
  };

  const closeActionDialog = () => {
    setActionDialog({ open: false, type: null });
    setSelectedOrg(null);
    setReason('');
  };

  const getFilteredOrganizations = () => {
    switch (tabValue) {
      case 0: // Pending
        return organizations.filter(org => org.approvalStatus === 'PENDING');
      case 1: // Approved
        return organizations.filter(org => org.approvalStatus === 'APPROVED');
      case 2: // Rejected
        return organizations.filter(org => org.approvalStatus === 'REJECTED');
      default: // All
        return organizations;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'APPROVED':
        return 'success';
      case 'REJECTED':
        return 'error';
      default:
        return 'default';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  const filteredOrgs = getFilteredOrganizations();

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight="bold">
            Organization Approval
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Manage organization registration requests
          </Typography>
        </Box>
        <IconButton onClick={fetchOrganizations} color="primary">
          <RefreshIcon />
        </IconButton>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabValue} onChange={(_, value) => setTabValue(value)}>
          <Tab label={`Pending (${organizations.filter(org => org.approvalStatus === 'PENDING').length})`} />
          <Tab label={`Approved (${organizations.filter(org => org.approvalStatus === 'APPROVED').length})`} />
          <Tab label={`Rejected (${organizations.filter(org => org.approvalStatus === 'REJECTED').length})`} />
          <Tab label={`All (${organizations.length})`} />
        </Tabs>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Organization</TableCell>
              <TableCell>Contact Person</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Registered</TableCell>
              <TableCell>Approved/Rejected</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredOrgs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="textSecondary">
                    No organizations found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredOrgs.map((org) => (
                <TableRow key={org.id} hover>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <BusinessIcon fontSize="small" color="action" />
                      <Typography variant="body1" fontWeight="medium">
                        {org.organization}
                      </Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <PersonIcon fontSize="small" color="action" />
                      {org.contactPerson}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <EmailIcon fontSize="small" color="action" />
                      {org.email}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {org.phone && (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <PhoneIcon fontSize="small" color="action" />
                        {org.phone}
                      </Box>
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={org.approvalStatus}
                      color={getStatusColor(org.approvalStatus)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{format(new Date(org.createdAt), 'MMM dd, yyyy')}</TableCell>
                  <TableCell>
                    {org.approvedAt ? (
                      <Box>
                        <Typography variant="caption" display="block">
                          {format(new Date(org.approvedAt), 'MMM dd, yyyy')}
                        </Typography>
                        <Typography variant="caption" color="textSecondary">
                          by {org.approvedBy}
                        </Typography>
                      </Box>
                    ) : (
                      '-'
                    )}
                  </TableCell>
                  <TableCell align="right">
                    {org.approvalStatus === 'PENDING' && (
                      <>
                        <IconButton
                          color="success"
                          onClick={() => openActionDialog(org, 'approve')}
                          size="small"
                        >
                          <ApproveIcon />
                        </IconButton>
                        <IconButton
                          color="error"
                          onClick={() => openActionDialog(org, 'reject')}
                          size="small"
                        >
                          <RejectIcon />
                        </IconButton>
                      </>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog
        open={actionDialog.open}
        onClose={closeActionDialog}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {actionDialog.type === 'approve' ? 'Approve Organization' : 'Reject Organization'}
        </DialogTitle>
        <DialogContent>
          {selectedOrg && (
            <>
              <DialogContentText sx={{ mb: 2 }}>
                Are you sure you want to {actionDialog.type} <strong>{selectedOrg.organization}</strong>?
              </DialogContentText>
              
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="textSecondary">Contact Person:</Typography>
                <Typography>{selectedOrg.contactPerson}</Typography>
              </Box>
              
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="textSecondary">Email:</Typography>
                <Typography>{selectedOrg.email}</Typography>
              </Box>
              
              {selectedOrg.address && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="textSecondary">Address:</Typography>
                  <Typography>{selectedOrg.address}</Typography>
                </Box>
              )}
              
              {actionDialog.type === 'reject' && (
                <TextField
                  fullWidth
                  label="Reason for rejection (optional)"
                  multiline
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  sx={{ mt: 2 }}
                />
              )}
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={closeActionDialog}>Cancel</Button>
          <Button
            onClick={actionDialog.type === 'approve' ? handleApprove : handleReject}
            color={actionDialog.type === 'approve' ? 'success' : 'error'}
            variant="contained"
          >
            {actionDialog.type === 'approve' ? 'Approve' : 'Reject'}
          </Button>
        </DialogActions>
      </Dialog>

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

export default OrganizationApproval;