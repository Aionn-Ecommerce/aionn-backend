package com.ecommerce.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreferenceEntity {

    @Id
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "theme", length = 20)
    private String theme;

    @Column(name = "notification_settings", columnDefinition = "json")
    private String notificationSettings;

    @Column(name = "ai_privacy_settings", columnDefinition = "json")
    private String aiPrivacySettings;

    protected UserPreferenceEntity() {
    }

    public String getUserId() {
        return userId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(String notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public String getAiPrivacySettings() {
        return aiPrivacySettings;
    }

    public void setAiPrivacySettings(String aiPrivacySettings) {
        this.aiPrivacySettings = aiPrivacySettings;
    }
}