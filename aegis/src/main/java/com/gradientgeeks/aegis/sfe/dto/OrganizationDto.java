package com.gradientgeeks.aegis.sfe.dto;

import com.gradientgeeks.aegis.sfe.entity.User;
import java.time.LocalDateTime;

/**
 * DTO for organization information without sensitive data like registration keys
 */
public class OrganizationDto {
    private Long id;
    private String email;
    private String name;
    private String organization;
    private String contactPerson;
    private String phone;
    private String address;
    private User.UserRole role;
    private User.ApprovalStatus approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private Boolean active;
    
    // Constructors
    public OrganizationDto() {}
    
    public OrganizationDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.organization = user.getOrganization();
        this.contactPerson = user.getContactPerson();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.role = user.getRole();
        this.approvalStatus = user.getApprovalStatus();
        this.createdAt = user.getCreatedAt();
        this.approvedAt = user.getApprovedAt();
        this.approvedBy = user.getApprovedBy();
        this.active = user.getActive();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public User.UserRole getRole() {
        return role;
    }
    
    public void setRole(User.UserRole role) {
        this.role = role;
    }
    
    public User.ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(User.ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
}