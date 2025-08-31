import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
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
} from '@mui/material';
import {
  Add as AddIcon,
  ContentCopy as CopyIcon,
  Refresh as RefreshIcon,
  Block as BlockIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { registrationKeyService } from '../services/api';
import { authService } from '../services/auth';
import { type RegistrationKey } from '../types';
import { format } from 'date-fns';

const RegistrationKeys: React.FC = () => {
  const navigate = useNavigate();
  const user = authService.getCurrentUser().user;
  const [keys, setKeys] = useState<RegistrationKey[]>([]);
  const [filteredKeys, setFilteredKeys] = useState<RegistrationKey[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedKey, setSelectedKey] = useState<RegistrationKey | null>(null);
  const [showKeyDialog, setShowKeyDialog] = useState(false);
  const [visibleKeys, setVisibleKeys] = useState<Set<number>>(new Set());
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  useEffect(() => {
    fetchRegistrationKeys();
  }, []);

  useEffect(() => {
    const filtered = keys.filter(key =>
      key.clientId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (key.description && key.description.toLowerCase().includes(searchTerm.toLowerCase()))
    );
    setFilteredKeys(filtered);
  }, [keys, searchTerm]);

  const fetchRegistrationKeys = async () => {
    try {
      setLoading(true);
      const data = await registrationKeyService.getAllRegistrationKeys();
      setKeys(data);
      setFilteredKeys(data);
    } catch (err) {
      setError('Failed to fetch registration keys');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRegenerateKey = async (clientId: string) => {
    try {
      const updatedKey = await registrationKeyService.regenerateRegistrationKey(clientId);
      setKeys(keys.map(key => key.clientId === clientId ? updatedKey : key));
      setSnackbar({ open: true, message: 'Registration key regenerated successfully', severity: 'success' });
    } catch (err) {
      setSnackbar({ open: true, message: 'Failed to regenerate key', severity: 'error' });
    }
  };

  const handleRevokeKey = async (clientId: string) => {
    try {
      const updatedKey = await registrationKeyService.revokeRegistrationKey(clientId);
      setKeys(keys.map(key => key.clientId === clientId ? updatedKey : key));
      setSnackbar({ open: true, message: 'Registration key revoked successfully', severity: 'success' });
    } catch (err) {
      setSnackbar({ open: true, message: 'Failed to revoke key', severity: 'error' });
    }
  };

  const handleCopyKey = (key: string) => {
    navigator.clipboard.writeText(key);
    setSnackbar({ open: true, message: 'Registration key copied to clipboard', severity: 'success' });
  };

  const toggleKeyVisibility = (keyId: number) => {
    const newVisibleKeys = new Set(visibleKeys);
    if (newVisibleKeys.has(keyId)) {
      newVisibleKeys.delete(keyId);
    } else {
      newVisibleKeys.add(keyId);
    }
    setVisibleKeys(newVisibleKeys);
  };

  const maskKey = (key: string) => {
    return key.substring(0, 8) + '••••••••••••••••' + key.substring(key.length - 4);
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
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight="bold">
            Registration Keys
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Managing keys for {user?.organization || 'your organization'}
          </Typography>
        </Box>
        {user?.role !== 'ADMIN' && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/registration-keys/create')}
          >
            Create New Key
          </Button>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {user?.role === 'ADMIN' && (
        <Alert severity="info" sx={{ mb: 3 }}>
          As an admin user, you can view registration keys but cannot create, modify, or revoke them. This is a security measure to ensure proper separation of duties.
        </Alert>
      )}

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Search by Client ID or Description..."
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
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Client ID</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Registration Key</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
              <TableCell>Expires</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredKeys.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography variant="body1" color="textSecondary">
                    No registration keys found
                  </Typography>
                  {user?.role !== 'ADMIN' && (
                    <Button
                      variant="contained"
                      sx={{ mt: 2 }}
                      onClick={() => navigate('/registration-keys/create')}
                    >
                      Create Your First Key
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ) : (
              filteredKeys.map((key) => (
                <TableRow key={key.id} hover>
                  <TableCell>
                    <Typography variant="body1" fontWeight="medium">
                      {key.clientId}
                    </Typography>
                  </TableCell>
                  <TableCell>{key.description || '-'}</TableCell>
                  <TableCell>
                    {user?.role === 'ADMIN' ? (
                      <Typography variant="body2" color="textSecondary">
                        (Hidden from admin)
                      </Typography>
                    ) : (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                          {visibleKeys.has(key.id) ? key.registrationKey : maskKey(key.registrationKey)}
                        </Typography>
                        <IconButton
                          size="small"
                          onClick={() => toggleKeyVisibility(key.id)}
                        >
                          {visibleKeys.has(key.id) ? <VisibilityOffIcon /> : <VisibilityIcon />}
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={() => handleCopyKey(key.registrationKey)}
                        >
                          <CopyIcon />
                        </IconButton>
                      </Box>
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={key.isActive ? 'Active' : 'Inactive'}
                      color={key.isActive ? 'success' : 'default'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{format(new Date(key.createdAt), 'MMM dd, yyyy')}</TableCell>
                  <TableCell>
                    {key.expiresAt ? format(new Date(key.expiresAt), 'MMM dd, yyyy') : 'Never'}
                  </TableCell>
                  <TableCell align="right">
                    {user?.role !== 'ADMIN' ? (
                      <>
                        <Tooltip title="Regenerate Key">
                          <IconButton
                            size="small"
                            onClick={() => handleRegenerateKey(key.clientId)}
                            disabled={!key.isActive}
                          >
                            <RefreshIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={key.isActive ? 'Revoke Key' : 'Key Already Revoked'}>
                          <IconButton
                            size="small"
                            onClick={() => handleRevokeKey(key.clientId)}
                            disabled={!key.isActive}
                          >
                            <BlockIcon />
                          </IconButton>
                        </Tooltip>
                      </>
                    ) : (
                      <Typography variant="caption" color="textSecondary">
                        No actions available
                      </Typography>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog
        open={showKeyDialog}
        onClose={() => setShowKeyDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Registration Key Details</DialogTitle>
        <DialogContent>
          {selectedKey && (
            <Box>
              <Typography variant="body2" color="textSecondary" mb={1}>
                Client ID
              </Typography>
              <Typography variant="body1" mb={2}>
                {selectedKey.clientId}
              </Typography>

              <Typography variant="body2" color="textSecondary" mb={1}>
                Registration Key
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <Typography
                  variant="body1"
                  sx={{ fontFamily: 'monospace', wordBreak: 'break-all' }}
                >
                  {selectedKey.registrationKey}
                </Typography>
                <IconButton
                  size="small"
                  onClick={() => handleCopyKey(selectedKey.registrationKey)}
                >
                  <CopyIcon />
                </IconButton>
              </Box>

              {selectedKey.description && (
                <>
                  <Typography variant="body2" color="textSecondary" mb={1}>
                    Description
                  </Typography>
                  <Typography variant="body1" mb={2}>
                    {selectedKey.description}
                  </Typography>
                </>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowKeyDialog(false)}>Close</Button>
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

export default RegistrationKeys;