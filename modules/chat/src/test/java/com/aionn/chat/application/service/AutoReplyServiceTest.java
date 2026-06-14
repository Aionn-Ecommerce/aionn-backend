package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.autoreply.command.AutoReplyCommands;
import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.chat.application.port.out.MerchantAutoReplyPersistencePort;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.model.MerchantAutoReply;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the auto-reply ownership fix: only the merchant's owner can read or
 * write the auto-reply config. Foreign callers must be rejected with
 * AUTO_REPLY_FORBIDDEN (CHT_302).
 */
@ExtendWith(MockitoExtension.class)
class AutoReplyServiceTest {

    @Mock
    MerchantAutoReplyPersistencePort repository;
    @Mock
    ChatResultMapper mapper;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    MerchantQueryPort merchantQueryPort;

    @InjectMocks
    AutoReplyService autoReplyService;

    @Test
    void get_rejectsWhenCallerHasNoMerchant() {
        when(merchantQueryPort.findMerchantIdByOwnerId("U_random")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autoReplyService.get("U_random", "M_seller"))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_302");

        verify(repository, never()).findByMerchantId(any());
    }

    @Test
    void get_rejectsWhenCallerOwnsAnotherMerchant() {
        when(merchantQueryPort.findMerchantIdByOwnerId("U_attacker")).thenReturn(Optional.of("M_attacker"));

        assertThatThrownBy(() -> autoReplyService.get("U_attacker", "M_victim"))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_302");

        verify(repository, never()).findByMerchantId(any());
    }

    @Test
    void get_returnsConfigForOwnerOfMerchant() {
        when(merchantQueryPort.findMerchantIdByOwnerId("U_owner")).thenReturn(Optional.of("M_seller"));
        when(repository.findByMerchantId("M_seller")).thenReturn(Optional.of(MerchantAutoReply.create("M_seller")));
        AutoReplyResult expected = stubResult();
        when(mapper.toResult(any(MerchantAutoReply.class))).thenReturn(expected);

        AutoReplyResult result = autoReplyService.get("U_owner", "M_seller");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void update_rejectsWhenCallerOwnsAnotherMerchant() {
        when(merchantQueryPort.findMerchantIdByOwnerId("U_attacker")).thenReturn(Optional.of("M_attacker"));

        AutoReplyCommands.UpdateAutoReply cmd = new AutoReplyCommands.UpdateAutoReply(
                "U_attacker", "M_victim", true, null, "away",
                LocalTime.of(8, 0), LocalTime.of(22, 0), Set.of(DayOfWeek.MONDAY));

        assertThatThrownBy(() -> autoReplyService.update(cmd))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_302");

        verify(repository, never()).save(any());
    }

    @Test
    void update_persistsForOwnerOfMerchant() {
        when(merchantQueryPort.findMerchantIdByOwnerId("U_owner")).thenReturn(Optional.of("M_seller"));
        when(repository.findByMerchantId("M_seller")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResult(any(MerchantAutoReply.class))).thenReturn(stubResult());

        AutoReplyCommands.UpdateAutoReply cmd = new AutoReplyCommands.UpdateAutoReply(
                "U_owner", "M_seller", true, "hi", "away",
                LocalTime.of(8, 0), LocalTime.of(22, 0), Set.of(DayOfWeek.MONDAY));

        autoReplyService.update(cmd);

        verify(repository).save(any(MerchantAutoReply.class));
    }

    private static AutoReplyResult stubResult() {
        return new AutoReplyResult("M_seller", true, null, null,
                LocalTime.of(8, 0), LocalTime.of(22, 0),
                Set.of(DayOfWeek.MONDAY), "Asia/Ho_Chi_Minh",
                java.time.Instant.now(), java.time.Instant.now());
    }
}
