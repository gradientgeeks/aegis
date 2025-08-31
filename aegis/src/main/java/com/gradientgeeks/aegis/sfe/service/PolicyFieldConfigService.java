package com.gradientgeeks.aegis.sfe.service;

import com.gradientgeeks.aegis.sfe.dto.PolicyFieldConfigDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for providing policy field configuration
 */
@Service
public class PolicyFieldConfigService {
    
    /**
     * Gets all available policy fields for rule configuration
     */
    public List<PolicyFieldConfigDto> getAllPolicyFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        // Account Context Fields
        fields.addAll(getAccountContextFields());
        
        // Transaction Context Fields
        fields.addAll(getTransactionContextFields());
        
        // User Limits Fields
        fields.addAll(getUserLimitsFields());
        
        // Account Limits Fields
        fields.addAll(getAccountLimitsFields());
        
        // Risk Assessment Fields
        fields.addAll(getRiskAssessmentFields());
        
        // Device Security Fields
        fields.addAll(getDeviceSecurityFields());
        
        // Authentication Fields
        fields.addAll(getAuthenticationFields());
        
        // Location/Geographic Fields
        fields.addAll(getLocationFields());
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getAccountContextFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto accountTier = new PolicyFieldConfigDto(
            "sessionContext.accountTier", "Account Tier", 
            "User account tier determining transaction limits", 
            "Account", PolicyFieldConfigDto.FieldType.ENUM
        );
        accountTier.setPossibleValues(Arrays.asList("SAVINGS", "CURRENT", "FIXED_DEPOSIT", "RECURRING_DEPOSIT", "CORPORATE", "PREMIUM_SAVINGS", "PREMIUM_CHECKING"));
        accountTier.setSampleValue("SAVINGS");
        fields.add(accountTier);
        
        PolicyFieldConfigDto accountAge = new PolicyFieldConfigDto(
            "sessionContext.accountAge", "Account Age (Months)", 
            "Age of account in months", 
            "Account", PolicyFieldConfigDto.FieldType.NUMBER
        );
        accountAge.setSampleValue("24");
        fields.add(accountAge);
        
        PolicyFieldConfigDto kycLevel = new PolicyFieldConfigDto(
            "sessionContext.kycLevel", "KYC Level", 
            "KYC verification level", 
            "Account", PolicyFieldConfigDto.FieldType.ENUM
        );
        kycLevel.setPossibleValues(Arrays.asList("NONE", "BASIC", "FULL", "ENHANCED"));
        kycLevel.setSampleValue("FULL");
        fields.add(kycLevel);
        
        PolicyFieldConfigDto hasDeviceBinding = new PolicyFieldConfigDto(
            "sessionContext.hasDeviceBinding", "Has Device Binding", 
            "Whether device binding is enabled", 
            "Account", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        hasDeviceBinding.setSampleValue("true");
        fields.add(hasDeviceBinding);
        
        PolicyFieldConfigDto deviceBindingCount = new PolicyFieldConfigDto(
            "sessionContext.deviceBindingCount", "Device Binding Count", 
            "Number of bound devices", 
            "Account", PolicyFieldConfigDto.FieldType.NUMBER
        );
        deviceBindingCount.setSampleValue("2");
        fields.add(deviceBindingCount);
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getTransactionContextFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto transactionType = new PolicyFieldConfigDto(
            "transactionContext.transactionType", "Transaction Type", 
            "Type of transaction being performed", 
            "Transaction", PolicyFieldConfigDto.FieldType.ENUM
        );
        transactionType.setPossibleValues(Arrays.asList("TRANSFER", "PAYMENT", "WITHDRAWAL", "DEPOSIT", "BILL_PAYMENT", "RECHARGE"));
        transactionType.setSampleValue("TRANSFER");
        fields.add(transactionType);
        
        PolicyFieldConfigDto amount = new PolicyFieldConfigDto(
            "transactionContext.amount", "Transaction Amount", 
            "Actual transaction amount in INR", 
            "Transaction", PolicyFieldConfigDto.FieldType.CURRENCY
        );
        amount.setSampleValue("50000");
        fields.add(amount);
        
        PolicyFieldConfigDto amountRange = new PolicyFieldConfigDto(
            "transactionContext.amountRange", "Amount Range", 
            "Categorized amount range", 
            "Transaction", PolicyFieldConfigDto.FieldType.ENUM
        );
        amountRange.setPossibleValues(Arrays.asList("MICRO", "LOW", "MEDIUM", "HIGH", "VERY_HIGH"));
        amountRange.setSampleValue("HIGH");
        fields.add(amountRange);
        
        PolicyFieldConfigDto beneficiaryType = new PolicyFieldConfigDto(
            "transactionContext.beneficiaryType", "Beneficiary Type", 
            "Type of beneficiary", 
            "Transaction", PolicyFieldConfigDto.FieldType.ENUM
        );
        beneficiaryType.setPossibleValues(Arrays.asList("NEW", "EXISTING", "FREQUENT", "SELF"));
        beneficiaryType.setSampleValue("NEW");
        fields.add(beneficiaryType);
        
        PolicyFieldConfigDto timeOfDay = new PolicyFieldConfigDto(
            "transactionContext.timeOfDay", "Time of Day", 
            "Time category when transaction occurs", 
            "Transaction", PolicyFieldConfigDto.FieldType.ENUM
        );
        timeOfDay.setPossibleValues(Arrays.asList("BUSINESS_HOURS", "AFTER_HOURS", "NIGHT"));
        timeOfDay.setSampleValue("NIGHT");
        fields.add(timeOfDay);
        
        PolicyFieldConfigDto dayOfWeek = new PolicyFieldConfigDto(
            "transactionContext.dayOfWeek", "Day of Week", 
            "Day of the week", 
            "Transaction", PolicyFieldConfigDto.FieldType.ENUM
        );
        dayOfWeek.setPossibleValues(Arrays.asList("WEEKDAY", "WEEKEND"));
        dayOfWeek.setSampleValue("WEEKEND");
        fields.add(dayOfWeek);
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getUserLimitsFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto dailyCount = new PolicyFieldConfigDto(
            "userLimits.dailyTransactionCount", "Daily Transaction Count", 
            "Number of transactions today", 
            "Daily Limits", PolicyFieldConfigDto.FieldType.NUMBER
        );
        dailyCount.setSampleValue("5");
        fields.add(dailyCount);
        
        PolicyFieldConfigDto dailyAmount = new PolicyFieldConfigDto(
            "userLimits.dailyTransactionAmount", "Daily Transaction Amount", 
            "Total amount transacted today", 
            "Daily Limits", PolicyFieldConfigDto.FieldType.CURRENCY
        );
        dailyAmount.setSampleValue("150000");
        fields.add(dailyAmount);
        
        PolicyFieldConfigDto weeklyCount = new PolicyFieldConfigDto(
            "userLimits.weeklyTransactionCount", "Weekly Transaction Count", 
            "Number of transactions this week", 
            "Weekly Limits", PolicyFieldConfigDto.FieldType.NUMBER
        );
        weeklyCount.setSampleValue("20");
        fields.add(weeklyCount);
        
        PolicyFieldConfigDto weeklyAmount = new PolicyFieldConfigDto(
            "userLimits.weeklyTransactionAmount", "Weekly Transaction Amount", 
            "Total amount transacted this week", 
            "Weekly Limits", PolicyFieldConfigDto.FieldType.CURRENCY
        );
        weeklyAmount.setSampleValue("500000");
        fields.add(weeklyAmount);
        
        PolicyFieldConfigDto monthlyCount = new PolicyFieldConfigDto(
            "userLimits.monthlyTransactionCount", "Monthly Transaction Count", 
            "Number of transactions this month", 
            "Monthly Limits", PolicyFieldConfigDto.FieldType.NUMBER
        );
        monthlyCount.setSampleValue("50");
        fields.add(monthlyCount);
        
        PolicyFieldConfigDto monthlyAmount = new PolicyFieldConfigDto(
            "userLimits.monthlyTransactionAmount", "Monthly Transaction Amount", 
            "Total amount transacted this month", 
            "Monthly Limits", PolicyFieldConfigDto.FieldType.CURRENCY
        );
        monthlyAmount.setSampleValue("2000000");
        fields.add(monthlyAmount);
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getAccountLimitsFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto maxDailyAmount = new PolicyFieldConfigDto(
            "accountLimits.maxDailyAmount", "Max Daily Amount Limit", 
            "Maximum allowed daily transaction amount for account type", 
            "Account Limits", PolicyFieldConfigDto.FieldType.CURRENCY
        );
        maxDailyAmount.setSampleValue("100000");
        fields.add(maxDailyAmount);
        
        PolicyFieldConfigDto maxSingleTransaction = new PolicyFieldConfigDto(
            "accountLimits.maxSingleTransactionAmount", "Max Single Transaction", 
            "Maximum allowed single transaction amount", 
            "Account Limits", PolicyFieldConfigDto.FieldType.CURRENCY
        );
        maxSingleTransaction.setSampleValue("50000");
        fields.add(maxSingleTransaction);
        
        PolicyFieldConfigDto maxMonthlyAmount = new PolicyFieldConfigDto(
            "accountLimits.maxMonthlyAmount", "Max Monthly Amount Limit", 
            "Maximum allowed monthly transaction amount", 
            "Account Limits", PolicyFieldConfigDto.FieldType.CURRENCY
        );
        maxMonthlyAmount.setSampleValue("1000000");
        fields.add(maxMonthlyAmount);
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getRiskAssessmentFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto locationChanged = new PolicyFieldConfigDto(
            "riskFactors.isLocationChanged", "Location Changed", 
            "Whether user location has changed significantly", 
            "Risk Assessment", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        locationChanged.setSampleValue("true");
        fields.add(locationChanged);
        
        PolicyFieldConfigDto deviceChanged = new PolicyFieldConfigDto(
            "riskFactors.isDeviceChanged", "Device Changed", 
            "Whether user is on a different device", 
            "Risk Assessment", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        deviceChanged.setSampleValue("false");
        fields.add(deviceChanged);
        
        PolicyFieldConfigDto dormantAccount = new PolicyFieldConfigDto(
            "riskFactors.isDormantAccount", "Dormant Account", 
            "Whether account was dormant and recently activated", 
            "Risk Assessment", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        dormantAccount.setSampleValue("false");
        fields.add(dormantAccount);
        
        PolicyFieldConfigDto velocityRisk = new PolicyFieldConfigDto(
            "riskFactors.velocityRiskScore", "Velocity Risk Score", 
            "Risk score based on transaction velocity (0-100)", 
            "Risk Assessment", PolicyFieldConfigDto.FieldType.NUMBER
        );
        velocityRisk.setSampleValue("75");
        fields.add(velocityRisk);
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getDeviceSecurityFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto isRooted = new PolicyFieldConfigDto(
            "deviceInfo.isRooted", "Device Rooted/Jailbroken", 
            "Whether device is rooted or jailbroken", 
            "Device Security", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        isRooted.setSampleValue("false");
        fields.add(isRooted);
        
        PolicyFieldConfigDto isEmulator = new PolicyFieldConfigDto(
            "deviceInfo.isEmulator", "Is Emulator", 
            "Whether device is an emulator", 
            "Device Security", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        isEmulator.setSampleValue("false");
        fields.add(isEmulator);
        
        PolicyFieldConfigDto appVersion = new PolicyFieldConfigDto(
            "deviceInfo.appVersion", "App Version", 
            "Version of the banking app", 
            "Device Security", PolicyFieldConfigDto.FieldType.STRING
        );
        appVersion.setSampleValue("2.1.0");
        fields.add(appVersion);
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getAuthenticationFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto mfaCompleted = new PolicyFieldConfigDto(
            "authContext.mfaCompleted", "MFA Completed", 
            "Whether multi-factor authentication was completed", 
            "Authentication", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        mfaCompleted.setSampleValue("true");
        fields.add(mfaCompleted);
        
        PolicyFieldConfigDto biometricUsed = new PolicyFieldConfigDto(
            "authContext.biometricUsed", "Biometric Authentication", 
            "Whether biometric authentication was used", 
            "Authentication", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        biometricUsed.setSampleValue("false");
        fields.add(biometricUsed);
        
        PolicyFieldConfigDto loginAttempts = new PolicyFieldConfigDto(
            "authContext.failedLoginAttempts", "Failed Login Attempts", 
            "Number of failed login attempts in last hour", 
            "Authentication", PolicyFieldConfigDto.FieldType.NUMBER
        );
        loginAttempts.setSampleValue("0");
        fields.add(loginAttempts);
        
        return fields;
    }
    
    private List<PolicyFieldConfigDto> getLocationFields() {
        List<PolicyFieldConfigDto> fields = new ArrayList<>();
        
        PolicyFieldConfigDto country = new PolicyFieldConfigDto(
            "locationInfo.country", "Country", 
            "User's current country", 
            "Location", PolicyFieldConfigDto.FieldType.STRING
        );
        country.setSampleValue("IN");
        fields.add(country);
        
        PolicyFieldConfigDto state = new PolicyFieldConfigDto(
            "locationInfo.state", "State/Province", 
            "User's current state or province", 
            "Location", PolicyFieldConfigDto.FieldType.STRING
        );
        state.setSampleValue("Maharashtra");
        fields.add(state);
        
        PolicyFieldConfigDto isHighRiskLocation = new PolicyFieldConfigDto(
            "locationInfo.isHighRiskLocation", "High Risk Location", 
            "Whether current location is considered high risk", 
            "Location", PolicyFieldConfigDto.FieldType.BOOLEAN
        );
        isHighRiskLocation.setSampleValue("false");
        fields.add(isHighRiskLocation);
        
        return fields;
    }
}