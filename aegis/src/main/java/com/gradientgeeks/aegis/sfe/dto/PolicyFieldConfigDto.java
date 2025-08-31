package com.gradientgeeks.aegis.sfe.dto;

import java.util.List;

/**
 * DTO for policy field configuration
 */
public class PolicyFieldConfigDto {
    
    private String fieldKey;
    private String fieldName;
    private String description;
    private String category;
    private FieldType fieldType;
    private List<String> possibleValues;
    private String sampleValue;
    private boolean required;
    
    public PolicyFieldConfigDto() {}
    
    public PolicyFieldConfigDto(String fieldKey, String fieldName, String description, 
                              String category, FieldType fieldType) {
        this.fieldKey = fieldKey;
        this.fieldName = fieldName;
        this.description = description;
        this.category = category;
        this.fieldType = fieldType;
        this.required = false;
    }
    
    // Getters and setters
    public String getFieldKey() {
        return fieldKey;
    }
    
    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public FieldType getFieldType() {
        return fieldType;
    }
    
    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }
    
    public List<String> getPossibleValues() {
        return possibleValues;
    }
    
    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }
    
    public String getSampleValue() {
        return sampleValue;
    }
    
    public void setSampleValue(String sampleValue) {
        this.sampleValue = sampleValue;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    /**
     * Field data types for policy conditions
     */
    public enum FieldType {
        STRING,
        NUMBER,
        BOOLEAN,
        ENUM,
        CURRENCY,
        DATE,
        TIME
    }
}