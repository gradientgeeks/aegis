package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "registration_keys", indexes = {
    @Index(name = "idx_client_id", columnList = "clientId", unique = true),
    @Index(name = "idx_registration_key", columnList = "registrationKey", unique = true)
})
public class RegistrationKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;
    
    @NotBlank
    @Size(max = 512)
    @Column(name = "registration_key", unique = true, nullable = false)
    private String registrationKey;
    
    @Size(max = 255)
    @Column(name = "description")
    private String description;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "organization", nullable = false)
    private String organization;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public RegistrationKey() {}
    
    public RegistrationKey(String clientId, String registrationKey, String description) {
        this.clientId = clientId;
        this.registrationKey = registrationKey;
        this.description = description;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getRegistrationKey() {
        return registrationKey;
    }
    
    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = registrationKey;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationKey that = (RegistrationKey) o;
        return Objects.equals(clientId, that.clientId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
    
    @Override
    public String toString() {
        return "RegistrationKey{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}