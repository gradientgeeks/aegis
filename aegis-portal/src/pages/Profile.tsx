import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  TextField,
  Button,
  Avatar,
  Divider,
  Alert,
  Paper,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Chip,
} from '@mui/material';
import {
  Business as BusinessIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
  Person as PersonIcon,
  Security as SecurityIcon,
  CalendarToday as CalendarIcon,
  Save as SaveIcon,
} from '@mui/icons-material';
import { authService } from '../services/auth';

const Profile: React.FC = () => {
  const user = authService.getCurrentUser().user;
  const [isEditing, setIsEditing] = useState(false);
  const [success, setSuccess] = useState(false);
  
  // Form data - in a real app, this would come from an API
  const [formData, setFormData] = useState({
    organizationName: user?.organization || 'Organization',
    contactPerson: user?.name || 'Contact Person',
    email: user?.email || 'email@example.com',
    phone: '+1 (555) 123-4567',
    address: '123 Security Street, Tech City, TC 12345',
    registeredDate: '2024-01-15',
    plan: user?.email?.includes('admin') ? 'Admin' : 'Enterprise',
  });

  const handleSave = () => {
    // TODO: Implement API call to save profile
    setSuccess(true);
    setIsEditing(false);
    setTimeout(() => setSuccess(false), 3000);
  };

  const handleCancel = () => {
    setIsEditing(false);
    // Reset form data
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" mb={3}>
        Organization Profile
      </Typography>

      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          Profile updated successfully!
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Avatar
                sx={{
                  width: 120,
                  height: 120,
                  mx: 'auto',
                  mb: 2,
                  bgcolor: 'primary.main',
                  fontSize: 48,
                }}
              >
                {formData.organizationName.charAt(0)}
              </Avatar>
              
              <Typography variant="h5" fontWeight="bold" mb={1}>
                {formData.organizationName}
              </Typography>
              
              <Chip
                label={formData.plan}
                color="primary"
                size="small"
                sx={{ mb: 2 }}
              />
              
              <Divider sx={{ my: 2 }} />
              
              <List dense>
                <ListItem>
                  <ListItemIcon>
                    <CalendarIcon />
                  </ListItemIcon>
                  <ListItemText
                    primary="Member Since"
                    secondary={new Date(formData.registeredDate).toLocaleDateString()}
                  />
                </ListItem>
                
                <ListItem>
                  <ListItemIcon>
                    <SecurityIcon />
                  </ListItemIcon>
                  <ListItemText
                    primary="Security Status"
                    secondary={
                      <Chip
                        label="Verified"
                        color="success"
                        size="small"
                      />
                    }
                  />
                </ListItem>
              </List>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h6" fontWeight="bold">
                  Organization Details
                </Typography>
                {!isEditing ? (
                  <Button
                    variant="outlined"
                    onClick={() => setIsEditing(true)}
                  >
                    Edit Profile
                  </Button>
                ) : (
                  <Box>
                    <Button
                      variant="text"
                      onClick={handleCancel}
                      sx={{ mr: 1 }}
                    >
                      Cancel
                    </Button>
                    <Button
                      variant="contained"
                      startIcon={<SaveIcon />}
                      onClick={handleSave}
                    >
                      Save Changes
                    </Button>
                  </Box>
                )}
              </Box>

              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Organization Name"
                    value={formData.organizationName}
                    onChange={(e) => setFormData({ ...formData, organizationName: e.target.value })}
                    disabled={!isEditing}
                    InputProps={{
                      startAdornment: <BusinessIcon sx={{ mr: 1, color: 'action.active' }} />,
                    }}
                  />
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Contact Person"
                    value={formData.contactPerson}
                    onChange={(e) => setFormData({ ...formData, contactPerson: e.target.value })}
                    disabled={!isEditing}
                    InputProps={{
                      startAdornment: <PersonIcon sx={{ mr: 1, color: 'action.active' }} />,
                    }}
                  />
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Email"
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    disabled={!isEditing}
                    InputProps={{
                      startAdornment: <EmailIcon sx={{ mr: 1, color: 'action.active' }} />,
                    }}
                  />
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Phone"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    disabled={!isEditing}
                    InputProps={{
                      startAdornment: <PhoneIcon sx={{ mr: 1, color: 'action.active' }} />,
                    }}
                  />
                </Grid>
                
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Address"
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    disabled={!isEditing}
                    multiline
                    rows={2}
                    InputProps={{
                      startAdornment: <LocationIcon sx={{ mr: 1, color: 'action.active', alignSelf: 'flex-start', mt: 1 }} />,
                    }}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          <Card sx={{ mt: 3 }}>
            <CardContent>
              <Typography variant="h6" fontWeight="bold" mb={3}>
                API Access Information
              </Typography>
              
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                    <Typography variant="body2" color="textSecondary" mb={1}>
                      API Endpoint
                    </Typography>
                    <Typography variant="body1" fontFamily="monospace">
                      https://api.aegis-security.com/v1
                    </Typography>
                  </Paper>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                    <Typography variant="body2" color="textSecondary" mb={1}>
                      Rate Limit
                    </Typography>
                    <Typography variant="body1">
                      10,000 requests/hour
                    </Typography>
                  </Paper>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                    <Typography variant="body2" color="textSecondary" mb={1}>
                      Current Usage
                    </Typography>
                    <Typography variant="body1">
                      2,456 requests (24.56%)
                    </Typography>
                  </Paper>
                </Grid>
              </Grid>
              
              <Alert severity="info" sx={{ mt: 3 }}>
                For API documentation and integration guides, visit our{' '}
                <a href="#" style={{ color: 'inherit' }}>
                  developer portal
                </a>
                .
              </Alert>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Profile;