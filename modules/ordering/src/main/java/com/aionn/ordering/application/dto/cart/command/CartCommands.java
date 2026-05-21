package com.aionn.ordering.application.dto.cart.command;

import com.aionn.sharedkernel.application.command.Command;

public final class CartCommands {

    private CartCommands() {
    }

    public record AddItem(String userId, String skuId, int qty) implements Command {
    }

    public record UpdateItemQty(String userId, String skuId, int newQty) implements Command {
    }

    public record RemoveItem(String userId, String skuId) implements Command {
    }

    public record ClearCart(String userId, String reason) implements Command {
    }

    public record ApplyVoucher(String userId, String voucherCode) implements Command {
    }
}
