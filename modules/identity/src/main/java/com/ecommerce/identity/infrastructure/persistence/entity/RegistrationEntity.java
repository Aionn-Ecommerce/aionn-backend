package com.ecommerce.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
public class RegistrationEntity {

    @Id
    @Column(name = "reg_id", nullable = false, length = 50)
    private String regId;

    @Column(name = "identity", nullable = false, length = 100)
    private String identity;

    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "otp_id", length = 50)
    private String otpId;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    protected RegistrationEntity() {
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOtpId() {
        return otpId;
    }

    public void setOtpId(String otpId) {
        this.otpId = otpId;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}