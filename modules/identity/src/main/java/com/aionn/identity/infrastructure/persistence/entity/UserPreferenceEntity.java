package com.aionn.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPreferenceEntity {

    @Id
    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "theme", length = 20)
    private String theme;

    @Column(name = "notification_settings", columnDefinition = "jsonb")
    private String notificationSettings;

    @Column(name = "ai_privacy_settings", columnDefinition = "jsonb")
    private String aiPrivacySettings;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}



