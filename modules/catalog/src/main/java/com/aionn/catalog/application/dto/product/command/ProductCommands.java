package com.aionn.catalog.application.dto.product.command;

import com.aionn.sharedkernel.application.command.Command;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * All command records for the Product aggregate, kept in one file because
 * they share semantics and reading them next to each other is more useful
 * than spreading across 20 tiny files.
 */
public final class ProductCommands {

        private ProductCommands() {
        }

        public record CreateProduct(String merchantId, String name) implements Command {
        }

        public record DefineVariant(
                        String productId,
                        String merchantId,
                        Map<String, String> attributeValues,
                        BigDecimal price,
                        String currency) implements Command {
        }

        public record RemoveVariant(String productId, String merchantId, String skuId) implements Command {
        }

        public record UpdateMedia(String productId, String merchantId, List<String> imageList) implements Command {
        }

        public record AssignBrand(String productId, String merchantId, String brandId) implements Command {
        }

        public record AssignCategories(String productId, String merchantId, List<String> categoryIds)
                        implements Command {
        }

        public record SubmitForReview(String productId, String merchantId) implements Command {
        }

        public record Publish(String productId, String adminId) implements Command {
        }

        public record Reject(String productId, String adminId, String reasonCode, String feedback) implements Command {
        }

        public record Deactivate(String productId, String merchantId, String reason) implements Command {
        }

        public record Restore(String productId, String merchantId) implements Command {
        }

        public record Clone(String sourceId, String merchantId) implements Command {
        }

        public record ChangeVariantPrice(
                        String productId,
                        String merchantId,
                        String skuId,
                        BigDecimal newPrice,
                        String currency) implements Command {
        }

        public record UpdateAiMetadata(
                        String productId,
                        String merchantId,
                        List<String> tags,
                        String aiDescription) implements Command {
        }

        public record AssignCollections(String productId, String merchantId, List<String> collectionIds)
                        implements Command {
        }

        public record DefineAttributes(String productId, String merchantId, Map<String, String> attributes)
                        implements Command {
        }

        public record BulkPriceUpdate(
                        String merchantId,
                        List<String> skuIds,
                        ChangeType changeType,
                        BigDecimal value,
                        String currency) implements Command {

                public enum ChangeType {
                        SET, INCREASE_AMOUNT, DECREASE_AMOUNT, INCREASE_PERCENT, DECREASE_PERCENT
                }
        }

        public record EmergencyTakedown(String productId, String adminId, String reason) implements Command {
        }
}
