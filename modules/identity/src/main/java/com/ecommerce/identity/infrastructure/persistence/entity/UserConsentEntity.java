package com.ecommerce.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents")
public class UserConsentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consent_id", nullable = false)
    private Long consentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "consent_type", length = 50)
    private String consentType;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    protected UserConsentEntity() {
    }

    public Long getConsentId() {
        return consentId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getAgreedAt() {
        return agreedAt;
    }

    public void setAgreedAt(LocalDateTime agreedAt) {
        this.agreedAt = agreedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}