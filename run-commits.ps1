. .\commit-helper.ps1

# ===== CHAT =====
Stage-And-Commit -Time "08:15" -Message "chat: javadoc khong can thiet, drop het" -Add @(
    "modules/chat/src/main/java/com/aionn/chat/adapter/websocket/ChatStompController.java",
    "modules/chat/src/main/java/com/aionn/chat/application/port/out/ChatPushNotifier.java",
    "modules/chat/src/main/java/com/aionn/chat/application/port/out/MessageRepository.java",
    "modules/chat/src/main/java/com/aionn/chat/application/port/out/PresenceTracker.java",
    "modules/chat/src/main/java/com/aionn/chat/application/port/out/RealtimeBroadcaster.java",
    "modules/chat/src/main/java/com/aionn/chat/application/port/out/UserBlockRepository.java",
    "modules/chat/src/main/java/com/aionn/chat/domain/valueobject/MessagePayload.java",
    "modules/chat/src/main/java/com/aionn/chat/domain/valueobject/MessageStatus.java",
    "modules/chat/src/main/java/com/aionn/chat/domain/valueobject/MessageType.java",
    "modules/chat/src/main/java/com/aionn/chat/domain/valueobject/Participant.java",
    "modules/chat/src/main/java/com/aionn/chat/domain/valueobject/ParticipantRole.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/notifier/LoggingChatPushNotifier.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/notifier/RemoteChatPushNotifier.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/presence/InMemoryPresenceTracker.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/presence/RedisPresenceTracker.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/realtime/ChatSessionListener.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/realtime/NoopRealtimeBroadcaster.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/realtime/StompRealtimeBroadcaster.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/realtime/WebSocketStompConfig.java"
)

Stage-And-Commit -Time "08:50" -Message "chat: @Version on Conversation, Message, Block, AutoReply" -Add @(
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/persistence/entity/ConversationEntity.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/persistence/entity/MerchantAutoReplyEntity.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/persistence/entity/MessageEntity.java",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/persistence/entity/UserBlockEntity.java"
)

Stage-And-Commit -Time "09:10" -Message "chat: V1_10_1 add version cols" -Add @(
    "modules/chat/src/main/resources/db/migration/V1_10_1__add_optimistic_lock_version.sql"
)

Stage-And-Commit -Time "09:35" -Message "chat: split adapters by aggregate" -AddAll @(
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/adapter",
    "modules/chat/src/main/java/com/aionn/chat/infrastructure/persistence/adapter"
)

# ===== NOTIFICATION =====
Stage-And-Commit -Time "10:05" -Message "notification: prune javadoc" -Add @(
    "modules/notification/src/main/java/com/aionn/notification/application/dto/notification/command/NotificationCommands.java",
    "modules/notification/src/main/java/com/aionn/notification/application/port/out/ChannelSender.java",
    "modules/notification/src/main/java/com/aionn/notification/application/port/out/NotificationRepository.java",
    "modules/notification/src/main/java/com/aionn/notification/application/port/out/RecipientResolver.java",
    "modules/notification/src/main/java/com/aionn/notification/application/service/NotificationDispatchService.java",
    "modules/notification/src/main/java/com/aionn/notification/domain/model/NotificationSubscription.java",
    "modules/notification/src/main/java/com/aionn/notification/domain/model/NotificationTemplate.java",
    "modules/notification/src/main/java/com/aionn/notification/domain/valueobject/NotificationCategory.java",
    "modules/notification/src/main/java/com/aionn/notification/domain/valueobject/NotificationPriority.java",
    "modules/notification/src/main/java/com/aionn/notification/domain/valueobject/NotificationStatus.java",
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/channel/InAppSender.java",
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/recipient/StubRecipientResolver.java",
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/scheduling/NotificationRetryScheduler.java"
)

Stage-And-Commit -Time "10:40" -Message "notification: @Version cho 4 entity, skip template (da co business version)" -Add @(
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/persistence/entity/DeviceTokenEntity.java",
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/persistence/entity/NotificationEntity.java",
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/persistence/entity/NotificationProviderEntity.java",
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/persistence/entity/NotificationSubscriptionEntity.java"
)

Stage-And-Commit -Time "11:00" -Message "notification: V1_4_1 lock version migrations" -Add @(
    "modules/notification/src/main/resources/db/migration/V1_4_1__add_optimistic_lock_version.sql"
)

Stage-And-Commit -Time "11:30" -Message "notification: adapter -> persistence/adapter/<aggregate>" -AddAll @(
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/adapter",
    "modules/notification/src/main/java/com/aionn/notification/infrastructure/persistence/adapter"
)

# ===== PROMOTION =====
Stage-And-Commit -Time "13:20" -Message "promotion: scrub javadoc" -Add @(
    "modules/promotion/src/main/java/com/aionn/promotion/application/port/out/PromotionCampaignRepository.java",
    "modules/promotion/src/main/java/com/aionn/promotion/application/port/out/VoucherRepository.java",
    "modules/promotion/src/main/java/com/aionn/promotion/application/service/PromotionCampaignService.java",
    "modules/promotion/src/main/java/com/aionn/promotion/application/service/VoucherService.java",
    "modules/promotion/src/main/java/com/aionn/promotion/domain/valueobject/CampaignStatus.java",
    "modules/promotion/src/main/java/com/aionn/promotion/domain/valueobject/CampaignType.java",
    "modules/promotion/src/main/java/com/aionn/promotion/domain/valueobject/PromotionCondition.java",
    "modules/promotion/src/main/java/com/aionn/promotion/domain/valueobject/UserVoucherStatus.java",
    "modules/promotion/src/main/java/com/aionn/promotion/infrastructure/scheduling/CampaignStatusScheduler.java"
)

Stage-And-Commit -Time "13:55" -Message "promotion: lock version cho campaign + user voucher" -Add @(
    "modules/promotion/src/main/java/com/aionn/promotion/infrastructure/persistence/entity/PromotionCampaignEntity.java",
    "modules/promotion/src/main/java/com/aionn/promotion/infrastructure/persistence/entity/UserVoucherEntity.java"
)

Stage-And-Commit -Time "14:10" -Message "promotion: V1_7_1 (vouchers da co san)" -Add @(
    "modules/promotion/src/main/resources/db/migration/V1_7_1__add_optimistic_lock_version.sql"
)

Stage-And-Commit -Time "14:40" -Message "promotion: bo cuc adapter theo aggregate" -AddAll @(
    "modules/promotion/src/main/java/com/aionn/promotion/infrastructure/adapter",
    "modules/promotion/src/main/java/com/aionn/promotion/infrastructure/persistence/adapter"
)

# ===== SHIPPING =====
Stage-And-Commit -Time "15:15" -Message "shipping: don javadoc" -Add @(
    "modules/shipping/src/main/java/com/aionn/shipping/application/port/out/CarrierClient.java",
    "modules/shipping/src/main/java/com/aionn/shipping/application/service/ShipmentService.java",
    "modules/shipping/src/main/java/com/aionn/shipping/domain/valueobject/ShipmentDimensions.java",
    "modules/shipping/src/main/java/com/aionn/shipping/domain/valueobject/ShipmentStatus.java",
    "modules/shipping/src/main/java/com/aionn/shipping/infrastructure/carrier/AssumeSuccessCarrierClient.java",
    "modules/shipping/src/main/java/com/aionn/shipping/infrastructure/carrier/GhnCarrierClient.java"
)

Stage-And-Commit -Time "15:45" -Message "shipping: @Version on Shipment + ShippingRate" -Add @(
    "modules/shipping/src/main/java/com/aionn/shipping/infrastructure/persistence/entity/ShipmentEntity.java",
    "modules/shipping/src/main/java/com/aionn/shipping/infrastructure/persistence/entity/ShippingRateEntity.java"
)

Stage-And-Commit -Time "16:00" -Message "shipping: flyway V1_8_1" -Add @(
    "modules/shipping/src/main/resources/db/migration/V1_8_1__add_optimistic_lock_version.sql"
)

Stage-And-Commit -Time "16:20" -Message "shipping: chia adapter ra shipment vs rate" -AddAll @(
    "modules/shipping/src/main/java/com/aionn/shipping/infrastructure/adapter",
    "modules/shipping/src/main/java/com/aionn/shipping/infrastructure/persistence/adapter"
)

# ===== Final cleanup: gitkeep =====
Stage-And-Commit -Time "17:10" -Message "drop .gitkeep ra het" -AddAll @(".")

git status
