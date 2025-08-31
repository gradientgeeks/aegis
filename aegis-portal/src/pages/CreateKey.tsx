import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  TextField,
  Typography,
  Alert,
  Paper,
  Stepper,
  Step,
  StepLabel,
  Grid,
  FormControlLabel,
  Switch,
  IconButton,
  InputAdornment,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  ContentCopy as CopyIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { useNavigate } from 'react-router-dom';
import { registrationKeyService } from '../services/api';
import { authService } from '../services/auth';
import { type RegistrationKeyRequest } from '../types';

const steps = ['Enter Details', 'Review & Create', 'Get Your Key'];

const CreateKey: React.FC = () => {
  const navigate = useNavigate();
  const user = authService.getCurrentUser().user;
  
  // Redirect admin users immediately
  useEffect(() => {
    if (user?.role === 'ADMIN') {
      navigate('/registration-keys');
    }
  }, [user, navigate]);
  const [activeStep, setActiveStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [copiedToClipboard, setCopiedToClipboard] = useState(false);
  
  // Form data
  const [formData, setFormData] = useState<RegistrationKeyRequest>({
    clientId: '',
    description: '',
  });
  const [hasExpiry, setHasExpiry] = useState(false);
  const [expiryDate, setExpiryDate] = useState<Date | null>(null);
  const [createdKey, setCreatedKey] = useState<{ clientId: string; registrationKey: string } | null>(null);

  const handleNext = async () => {
    if (activeStep === 0) {
      // Validate form
      if (!formData.clientId.trim()) {
        setError('Client ID is required');
        return;
      }
      setError('');
      setActiveStep(1);
    } else if (activeStep === 1) {
      // Create the key
      await handleCreateKey();
    } else {
      // Navigate back to list
      navigate('/registration-keys');
    }
  };

  const handleBack = () => {
    if (activeStep === 0) {
      navigate('/registration-keys');
    } else {
      setActiveStep(activeStep - 1);
    }
  };

  const handleCreateKey = async () => {
    try {
      setLoading(true);
      setError('');
      
      const requestData: RegistrationKeyRequest = {
        ...formData,
        expiresAt: hasExpiry && expiryDate ? expiryDate.toISOString() : undefined,
      };
      
      const response = await registrationKeyService.createRegistrationKey(requestData);
      setCreatedKey({
        clientId: response.clientId,
        registrationKey: response.registrationKey,
      });
      setActiveStep(2);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create registration key');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyKey = () => {
    if (createdKey?.registrationKey) {
      navigator.clipboard.writeText(createdKey.registrationKey);
      setCopiedToClipboard(true);
      setTimeout(() => setCopiedToClipboard(false), 2000);
    }
  };

  const renderStepContent = () => {
    switch (activeStep) {
      case 0:
        return (
          <Box>
            <Typography variant="h6" mb={1}>
              Enter Registration Key Details
            </Typography>
            <Typography variant="body2" color="textSecondary" mb={3}>
              Creating key for {user?.organization || 'your organization'}
            </Typography>
            
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Client ID"
                  value={formData.clientId}
                  onChange={(e) => setFormData({ ...formData, clientId: e.target.value })}
                  placeholder="e.g., org-name-prod"
                  required
                  helperText="A unique identifier for your organization or application"
                />
              </Grid>
              
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="e.g., Production API key for mobile app"
                  multiline
                  rows={3}
                  helperText="Optional description to help identify this key"
                />
              </Grid>
              
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={hasExpiry}
                      onChange={(e) => setHasExpiry(e.target.checked)}
                    />
                  }
                  label="Set expiration date"
                />
              </Grid>
              
              {hasExpiry && (
                <Grid item xs={12}>
                  <LocalizationProvider dateAdapter={AdapterDateFns}>
                    <DateTimePicker
                      label="Expiration Date"
                      value={expiryDate}
                      onChange={setExpiryDate}
                      minDateTime={new Date()}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          helperText: 'The key will become inactive after this date',
                        },
                      }}
                    />
                  </LocalizationProvider>
                </Grid>
              )}
            </Grid>
          </Box>
        );
        
      case 1:
        return (
          <Box>
            <Typography variant="h6" mb={3}>
              Review Your Registration Key Details
            </Typography>
            
            <Paper sx={{ p: 3, bgcolor: 'background.default' }}>
              <Typography variant="body2" color="textSecondary" mb={1}>
                Client ID
              </Typography>
              <Typography variant="body1" mb={2} fontWeight="medium">
                {formData.clientId}
              </Typography>
              
              {formData.description && (
                <>
                  <Typography variant="body2" color="textSecondary" mb={1}>
                    Description
                  </Typography>
                  <Typography variant="body1" mb={2}>
                    {formData.description}
                  </Typography>
                </>
              )}
              
              <Typography variant="body2" color="textSecondary" mb={1}>
                Expiration
              </Typography>
              <Typography variant="body1">
                {hasExpiry && expiryDate
                  ? new Date(expiryDate).toLocaleString()
                  : 'Never expires'}
              </Typography>
            </Paper>
            
            <Alert severity="info" sx={{ mt: 3 }}>
              Once created, the registration key will be displayed only once. Make sure to copy and store it securely.
            </Alert>
          </Box>
        );
        
      case 2:
        return (
          <Box>
            <Box sx={{ textAlign: 'center', mb: 3 }}>
              <CheckCircleIcon sx={{ fontSize: 64, color: 'success.main' }} />
              <Typography variant="h6" mt={2}>
                Registration Key Created Successfully!
              </Typography>
            </Box>
            
            <Paper sx={{ p: 3, bgcolor: 'background.default' }}>
              <Typography variant="body2" color="textSecondary" mb={1}>
                Client ID
              </Typography>
              <Typography variant="body1" mb={3} fontWeight="medium">
                {createdKey?.clientId}
              </Typography>
              
              <Typography variant="body2" color="textSecondary" mb={1}>
                Registration Key
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TextField
                  fullWidth
                  value={createdKey?.registrationKey || ''}
                  InputProps={{
                    readOnly: true,
                    sx: { fontFamily: 'monospace' },
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton onClick={handleCopyKey}>
                          <CopyIcon />
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />
              </Box>
              
              {copiedToClipboard && (
                <Typography variant="body2" color="success.main" mt={1}>
                  Copied to clipboard!
                </Typography>
              )}
            </Paper>
            
            <Alert severity="warning" sx={{ mt: 3 }}>
              <strong>Important:</strong> This is the only time you'll see this registration key. 
              Please copy it now and store it securely. You cannot retrieve it later.
            </Alert>
          </Box>
        );
        
      default:
        return null;
    }
  };

  // Don't render anything if admin user (they'll be redirected)
  if (user?.role === 'ADMIN') {
    return null;
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate('/registration-keys')} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" fontWeight="bold">
          Create Registration Key
        </Typography>
      </Box>

      <Card>
        <CardContent sx={{ p: 4 }}>
          <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          {renderStepContent()}

          <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
            <Button
              onClick={handleBack}
              disabled={loading}
            >
              {activeStep === 0 ? 'Cancel' : 'Back'}
            </Button>
            <Button
              variant="contained"
              onClick={handleNext}
              disabled={loading}
            >
              {loading ? 'Creating...' : activeStep === 2 ? 'Done' : activeStep === 1 ? 'Create Key' : 'Next'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default CreateKey;