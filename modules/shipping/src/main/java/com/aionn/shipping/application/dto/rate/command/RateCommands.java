package com.aionn.shipping.application.dto.rate.command;

import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;

public final class RateCommands {

        private RateCommands() {
        }

        public record ConfigureRate(
                        String zoneCode,
                        BigDecimal baseFee,
                        String currency,
                        String condition) implements Command {
        }

        public record UpdateRate(
                        String rateId,
                        BigDecimal baseFee,
                        String condition) implements Command {
        }
}
