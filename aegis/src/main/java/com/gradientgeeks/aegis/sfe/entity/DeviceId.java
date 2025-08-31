package com.gradientgeeks.aegis.sfe.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeviceId implements Serializable {
    
    private String deviceId;
    private String clientId;
    
    public DeviceId() {}
    
    public DeviceId(String deviceId, String clientId) {
        this.deviceId = deviceId;
        this.clientId = clientId;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceId deviceId1 = (DeviceId) o;
        return Objects.equals(deviceId, deviceId1.deviceId) && 
               Objects.equals(clientId, deviceId1.clientId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(deviceId, clientId);
    }
    
    @Override
    public String toString() {
        return "DeviceId{" +
                "deviceId='" + deviceId + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}