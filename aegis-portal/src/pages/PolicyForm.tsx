import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Grid,
  Paper,
  Divider,
  Alert,
  CircularProgress,
  Snackbar,
  IconButton,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  AccordionActions,
} from '@mui/material';
import {
  Save as SaveIcon,
  Cancel as CancelIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  ExpandMore as ExpandMoreIcon,
  Rule as RuleIcon,
  Policy as PolicyIcon,
} from '@mui/icons-material';
import { policyService } from '../services/api';
import { authService } from '../services/auth';
import { 
  type Policy, 
  type PolicyRule, 
  type PolicyType, 
  type EnforcementLevel, 
  type RuleOperator 
} from '../types';

const PolicyForm: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const user = authService.getCurrentUser().user;
  const isEditing = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  const [policy, setPolicy] = useState<Policy>({
    clientId: user?.organization ? `${user.organization}_PROD_ANDROID` : '',
    policyName: '',
    policyType: 'DEVICE_SECURITY',
    enforcementLevel: 'WARN',
    description: '',
    isActive: true,
    rules: [],
  });

  const [newRule, setNewRule] = useState<PolicyRule>({
    ruleName: '',
    conditionField: '',
    operator: 'EQUALS',
    conditionValue: '',
    errorMessage: '',
    priority: 1,
    isActive: true,
  });

  const [showRuleForm, setShowRuleForm] = useState(false);
  const [editingRuleIndex, setEditingRuleIndex] = useState<number | null>(null);
  const [policyFields, setPolicyFields] = useState<any[]>([]);
  const [loadingFields, setLoadingFields] = useState(false);

  const policyTypes: { value: PolicyType; label: string; description: string }[] = [
    { value: 'DEVICE_SECURITY', label: 'Device Security', description: 'Policies related to device security status and requirements' },
    { value: 'TRANSACTION_LIMIT', label: 'Transaction Limit', description: 'Policies for transaction amount and frequency limits' },
    { value: 'GEOGRAPHIC_RESTRICTION', label: 'Geographic Restriction', description: 'Policies based on user location and geographic boundaries' },
    { value: 'TIME_RESTRICTION', label: 'Time Restriction', description: 'Policies for time-based access controls' },
    { value: 'DEVICE_BINDING', label: 'Device Binding', description: 'Policies for device binding and multi-device usage' },
    { value: 'RISK_ASSESSMENT', label: 'Risk Assessment', description: 'Policies based on user risk factors and behavior' },
    { value: 'API_RATE_LIMIT', label: 'API Rate Limit', description: 'Policies for API request rate limiting' },
    { value: 'AUTHENTICATION_REQUIREMENT', label: 'Authentication Requirement', description: 'Policies for authentication and MFA requirements' },
  ];

  const enforcementLevels: { value: EnforcementLevel; label: string; description: string }[] = [
    { value: 'BLOCK', label: 'Block', description: 'Completely block the request' },
    { value: 'REQUIRE_MFA', label: 'Require MFA', description: 'Require multi-factor authentication' },
    { value: 'WARN', label: 'Warn', description: 'Allow request but send warning' },
    { value: 'NOTIFY', label: 'Notify', description: 'Allow request and send notification' },
    { value: 'MONITOR', label: 'Monitor', description: 'Allow request and log for monitoring' },
  ];

  const operators: { value: RuleOperator; label: string }[] = [
    { value: 'EQUALS', label: 'Equals' },
    { value: 'NOT_EQUALS', label: 'Not Equals' },
    { value: 'GREATER_THAN', label: 'Greater Than' },
    { value: 'LESS_THAN', label: 'Less Than' },
    { value: 'GREATER_THAN_OR_EQUALS', label: 'Greater Than or Equals' },
    { value: 'LESS_THAN_OR_EQUALS', label: 'Less Than or Equals' },
    { value: 'CONTAINS', label: 'Contains' },
    { value: 'NOT_CONTAINS', label: 'Not Contains' },
    { value: 'STARTS_WITH', label: 'Starts With' },
    { value: 'ENDS_WITH', label: 'Ends With' },
    { value: 'IN', label: 'In (comma-separated)' },
    { value: 'NOT_IN', label: 'Not In (comma-separated)' },
    { value: 'REGEX_MATCH', label: 'Regex Match' },
    { value: 'BETWEEN', label: 'Between (comma-separated min,max)' },
    { value: 'IS_NULL', label: 'Is Null/Empty' },
    { value: 'IS_NOT_NULL', label: 'Is Not Null/Empty' },
  ];

  const commonFields: { value: string; label: string; description: string }[] = [
    { value: 'sessionContext.accountTier', label: 'Account Tier', description: 'User account tier (BASIC, PREMIUM, CORPORATE)' },
    { value: 'sessionContext.accountAge', label: 'Account Age', description: 'Age of account in months' },
    { value: 'sessionContext.kycLevel', label: 'KYC Level', description: 'KYC verification level (NONE, BASIC, FULL)' },
    { value: 'sessionContext.hasDeviceBinding', label: 'Has Device Binding', description: 'Whether device binding is enabled' },
    { value: 'sessionContext.deviceBindingCount', label: 'Device Binding Count', description: 'Number of bound devices' },
    { value: 'transactionContext.transactionType', label: 'Transaction Type', description: 'Type of transaction being performed' },
    { value: 'transactionContext.amountRange', label: 'Amount Range', description: 'Categorized amount range (MICRO, LOW, MEDIUM, HIGH, VERY_HIGH)' },
    { value: 'transactionContext.beneficiaryType', label: 'Beneficiary Type', description: 'Type of beneficiary (NEW, EXISTING, FREQUENT)' },
    { value: 'transactionContext.timeOfDay', label: 'Time of Day', description: 'Time category (BUSINESS_HOURS, AFTER_HOURS, NIGHT)' },
    { value: 'riskFactors.isLocationChanged', label: 'Location Changed', description: 'Whether user location has changed' },
    { value: 'riskFactors.isDeviceChanged', label: 'Device Changed', description: 'Whether user is on a different device' },
    { value: 'riskFactors.isDormantAccount', label: 'Dormant Account', description: 'Whether account was dormant' },
    { value: 'riskFactors.requiresDeviceRebinding', label: 'Requires Device Rebinding', description: 'Whether device rebinding is required' },
  ];

  useEffect(() => {
    if (isEditing && id) {
      fetchPolicy();
    }
    fetchPolicyFields();
  }, [id, isEditing]);

  const fetchPolicyFields = async () => {
    try {
      setLoadingFields(true);
      const fields = await policyService.getPolicyFields();
      setPolicyFields(fields);
    } catch (err) {
      console.error('Failed to fetch policy fields:', err);
    } finally {
      setLoadingFields(false);
    }
  };

  const fetchPolicy = async () => {
    try {
      setLoading(true);
      const data = await policyService.getPolicyById(Number(id));
      setPolicy({
        ...data,
        rules: data.rules || []
      });
    } catch (err) {
      console.error('Failed to fetch policy:', err);
      setError('Failed to fetch policy');
    } finally {
      setLoading(false);
    }
  };

  const handleSavePolicy = async () => {
    try {
      setSaving(true);

      // Validate required fields
      if (!policy.policyName.trim()) {
        setSnackbar({ open: true, message: 'Policy name is required', severity: 'error' });
        setSaving(false);
        return;
      }

      if (!policy.rules || policy.rules.length === 0) {
        setSnackbar({ open: true, message: 'At least one rule is required', severity: 'error' });
        setSaving(false);
        return;
      }

      if (isEditing) {
        await policyService.updatePolicy(Number(id), policy);
        setSnackbar({ open: true, message: 'Policy updated successfully', severity: 'success' });
      } else {
        await policyService.createPolicy(policy);
        setSnackbar({ open: true, message: 'Policy created successfully', severity: 'success' });
      }

      setTimeout(() => navigate('/policies'), 1500);
    } catch (err) {
      setSnackbar({ open: true, message: 'Failed to save policy', severity: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handleAddRule = () => {
    if (!newRule.ruleName.trim() || !newRule.conditionField.trim()) {
      setSnackbar({ open: true, message: 'Rule name and condition field are required', severity: 'error' });
      return;
    }

    const rules = [...(policy.rules || [])];
    
    if (editingRuleIndex !== null) {
      rules[editingRuleIndex] = { ...newRule, id: rules[editingRuleIndex].id };
      setEditingRuleIndex(null);
    } else {
      rules.push({ ...newRule, id: Date.now() }); // Temporary ID for UI
    }

    setPolicy({ ...policy, rules });
    setNewRule({
      ruleName: '',
      conditionField: '',
      operator: 'EQUALS',
      conditionValue: '',
      errorMessage: '',
      priority: rules.length + 1,
      isActive: true,
    });
    setShowRuleForm(false);
  };

  const handleEditRule = (index: number) => {
    const rule = policy.rules![index];
    setNewRule({ ...rule });
    setEditingRuleIndex(index);
    setShowRuleForm(true);
  };

  const handleDeleteRule = (index: number) => {
    const rules = [...(policy.rules || [])];
    rules.splice(index, 1);
    setPolicy({ ...policy, rules });
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
            {isEditing ? 'Edit Policy' : 'Create New Policy'}
          </Typography>
          <Typography variant="body2" color="textSecondary">
            {isEditing ? 'Modify existing security policy' : 'Define a new security policy for your organization'}
          </Typography>
        </Box>
        <Box>
          <Button
            variant="outlined"
            startIcon={<CancelIcon />}
            onClick={() => navigate('/policies')}
            sx={{ mr: 2 }}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            startIcon={<SaveIcon />}
            onClick={handleSavePolicy}
            disabled={saving}
          >
            {saving ? <CircularProgress size={24} /> : (isEditing ? 'Update Policy' : 'Create Policy')}
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Policy Details */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={3}>
                Policy Details
              </Typography>

              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Policy Name"
                    value={policy.policyName}
                    onChange={(e) => setPolicy({ ...policy, policyName: e.target.value })}
                    placeholder="e.g., High Risk Transaction Monitoring"
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Policy Type</InputLabel>
                    <Select
                      value={policy.policyType}
                      label="Policy Type"
                      onChange={(e) => setPolicy({ ...policy, policyType: e.target.value as PolicyType })}
                    >
                      {policyTypes.map((type) => (
                        <MenuItem key={type.value} value={type.value}>
                          {type.label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Enforcement Level</InputLabel>
                    <Select
                      value={policy.enforcementLevel}
                      label="Enforcement Level"
                      onChange={(e) => setPolicy({ ...policy, enforcementLevel: e.target.value as EnforcementLevel })}
                    >
                      {enforcementLevels.map((level) => (
                        <MenuItem key={level.value} value={level.value}>
                          {level.label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    multiline
                    rows={3}
                    label="Description"
                    value={policy.description}
                    onChange={(e) => setPolicy({ ...policy, description: e.target.value })}
                    placeholder="Describe when and how this policy should be applied..."
                  />
                </Grid>

                <Grid item xs={12}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={policy.isActive || false}
                        onChange={(e) => setPolicy({ ...policy, isActive: e.target.checked })}
                      />
                    }
                    label="Active Policy"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Policy Type Description */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Policy Type Info
              </Typography>
              {policyTypes.find(t => t.value === policy.policyType) && (
                <Box>
                  <Typography variant="body1" fontWeight="medium" mb={1}>
                    {policyTypes.find(t => t.value === policy.policyType)!.label}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" mb={2}>
                    {policyTypes.find(t => t.value === policy.policyType)!.description}
                  </Typography>
                </Box>
              )}
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="h6" mb={2}>
                Enforcement Level Info
              </Typography>
              {enforcementLevels.find(l => l.value === policy.enforcementLevel) && (
                <Box>
                  <Typography variant="body1" fontWeight="medium" mb={1}>
                    {enforcementLevels.find(l => l.value === policy.enforcementLevel)!.label}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    {enforcementLevels.find(l => l.value === policy.enforcementLevel)!.description}
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Policy Rules */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h6">
                  <RuleIcon sx={{ mr: 1, verticalAlign: 'bottom' }} />
                  Policy Rules ({policy.rules?.length || 0})
                </Typography>
                <Button
                  variant="outlined"
                  startIcon={<AddIcon />}
                  onClick={() => {
                    setNewRule({
                      ruleName: '',
                      conditionField: '',
                      operator: 'EQUALS',
                      conditionValue: '',
                      errorMessage: '',
                      priority: (policy.rules?.length || 0) + 1,
                      isActive: true,
                    });
                    setEditingRuleIndex(null);
                    setShowRuleForm(true);
                  }}
                >
                  Add Rule
                </Button>
              </Box>

              {/* Add/Edit Rule Form */}
              {showRuleForm && (
                <Paper sx={{ p: 3, mb: 3, bgcolor: 'grey.50' }}>
                  <Typography variant="h6" mb={2}>
                    {editingRuleIndex !== null ? 'Edit Rule' : 'Add New Rule'}
                  </Typography>
                  
                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Rule Name"
                        value={newRule.ruleName}
                        onChange={(e) => setNewRule({ ...newRule, ruleName: e.target.value })}
                        placeholder="e.g., Account Age Check"
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <FormControl fullWidth>
                        <InputLabel>Condition Field</InputLabel>
                        <Select
                          value={newRule.conditionField}
                          label="Condition Field"
                          onChange={(e) => setNewRule({ ...newRule, conditionField: e.target.value })}
                          disabled={loadingFields}
                        >
                          {policyFields.length > 0 ? (
                            policyFields.reduce((acc, field) => {
                              const category = field.category;
                              if (!acc.find(item => item.category === category)) {
                                acc.push({ category, fields: [] });
                              }
                              acc.find(item => item.category === category).fields.push(field);
                              return acc;
                            }, []).map((group) => [
                              <MenuItem key={group.category} disabled sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                                {group.category}
                              </MenuItem>,
                              ...group.fields.map((field) => (
                                <MenuItem key={field.fieldKey} value={field.fieldKey} sx={{ pl: 3 }}>
                                  {field.fieldName}
                                </MenuItem>
                              ))
                            ]).flat()
                          ) : (
                            <MenuItem disabled>
                              {loadingFields ? 'Loading fields...' : 'No fields available'}
                            </MenuItem>
                          )}
                        </Select>
                        {newRule.conditionField && policyFields.find(f => f.fieldKey === newRule.conditionField) && (
                          <Typography variant="caption" sx={{ mt: 1, display: 'block' }} color="textSecondary">
                            {policyFields.find(f => f.fieldKey === newRule.conditionField)?.description}
                          </Typography>
                        )}
                      </FormControl>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <FormControl fullWidth>
                        <InputLabel>Operator</InputLabel>
                        <Select
                          value={newRule.operator}
                          label="Operator"
                          onChange={(e) => setNewRule({ ...newRule, operator: e.target.value as RuleOperator })}
                        >
                          {operators.map((op) => (
                            <MenuItem key={op.value} value={op.value}>
                              {op.label}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Condition Value"
                        value={newRule.conditionValue}
                        onChange={(e) => setNewRule({ ...newRule, conditionValue: e.target.value })}
                        placeholder={
                          newRule.conditionField && policyFields.find(f => f.fieldKey === newRule.conditionField)?.sampleValue
                            ? `e.g., ${policyFields.find(f => f.fieldKey === newRule.conditionField)?.sampleValue}`
                            : "Value to compare against"
                        }
                        helperText={
                          newRule.conditionField && policyFields.find(f => f.fieldKey === newRule.conditionField)?.possibleValues?.length > 0
                            ? `Possible values: ${policyFields.find(f => f.fieldKey === newRule.conditionField)?.possibleValues?.join(', ')}`
                            : undefined
                        }
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Error Message"
                        value={newRule.errorMessage}
                        onChange={(e) => setNewRule({ ...newRule, errorMessage: e.target.value })}
                        placeholder="Message to show when rule is violated"
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        type="number"
                        label="Priority"
                        value={newRule.priority}
                        onChange={(e) => setNewRule({ ...newRule, priority: Number(e.target.value) })}
                        inputProps={{ min: 1 }}
                      />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={newRule.isActive || false}
                            onChange={(e) => setNewRule({ ...newRule, isActive: e.target.checked })}
                          />
                        }
                        label="Active Rule"
                      />
                    </Grid>
                  </Grid>

                  <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
                    <Button variant="contained" onClick={handleAddRule}>
                      {editingRuleIndex !== null ? 'Update Rule' : 'Add Rule'}
                    </Button>
                    <Button 
                      variant="outlined" 
                      onClick={() => {
                        setShowRuleForm(false);
                        setEditingRuleIndex(null);
                      }}
                    >
                      Cancel
                    </Button>
                  </Box>
                </Paper>
              )}

              {/* Existing Rules */}
              {policy.rules && policy.rules.length > 0 ? (
                policy.rules.map((rule, index) => (
                  <Accordion key={rule.id || index}>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                        <Typography variant="body1" sx={{ flexGrow: 1 }}>
                          {rule.ruleName}
                        </Typography>
                        <Typography variant="body2" color="textSecondary" sx={{ mr: 2 }}>
                          {rule.conditionField} {rule.operator.toLowerCase().replace(/_/g, ' ')} {rule.conditionValue}
                        </Typography>
                      </Box>
                    </AccordionSummary>
                    <AccordionDetails>
                      <Grid container spacing={2}>
                        <Grid item xs={12} sm={4}>
                          <Typography variant="body2" color="textSecondary">
                            Field
                          </Typography>
                          <Typography variant="body1">
                            {policyFields.find(f => f.fieldKey === rule.conditionField)?.fieldName || rule.conditionField}
                          </Typography>
                        </Grid>
                        <Grid item xs={12} sm={4}>
                          <Typography variant="body2" color="textSecondary">
                            Operator
                          </Typography>
                          <Typography variant="body1">
                            {operators.find(o => o.value === rule.operator)?.label || rule.operator}
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
                    <AccordionActions>
                      <Button size="small" onClick={() => handleEditRule(index)}>
                        Edit
                      </Button>
                      <IconButton 
                        size="small" 
                        color="error"
                        onClick={() => handleDeleteRule(index)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </AccordionActions>
                  </Accordion>
                ))
              ) : (
                <Alert severity="info">
                  No rules defined yet. Add at least one rule to complete the policy.
                </Alert>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

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

export default PolicyForm;