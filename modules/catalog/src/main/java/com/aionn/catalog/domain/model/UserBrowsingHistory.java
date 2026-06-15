package com.aionn.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserBrowsingHistory {
    private final String userId;
    private final List<String> categoryIds;
    private final List<String> brandIds;

    public static UserBrowsingHistory create(String userId) {
        return new UserBrowsingHistory(userId, new ArrayList<>(), new ArrayList<>());
    }

    public void trackView(List<String> productCategoryIds, String brandId) {
        if (productCategoryIds != null) {
            for (String cat : productCategoryIds) {
                categoryIds.remove(cat);
                categoryIds.add(0, cat);
            }
        }
        if (brandId != null) {
            brandIds.remove(brandId);
            brandIds.add(0, brandId);
        }
        // Limit preference list sizes to top 5
        while (categoryIds.size() > 5) {
            categoryIds.remove(categoryIds.size() - 1);
        }
        while (brandIds.size() > 5) {
            brandIds.remove(brandIds.size() - 1);
        }
    }
}
