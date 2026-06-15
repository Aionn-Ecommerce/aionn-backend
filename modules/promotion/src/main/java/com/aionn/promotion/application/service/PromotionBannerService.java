package com.aionn.promotion.application.service;

import com.aionn.promotion.application.dto.banner.result.PromotionBannerResult;
import com.aionn.promotion.application.mapper.PromotionResultMapper;
import com.aionn.promotion.application.port.out.PromotionBannerPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionBannerService {

    private final PromotionBannerPersistencePort bannerRepository;
    private final PromotionResultMapper mapper;

    public List<PromotionBannerResult> listActive() {
        return bannerRepository.findAllActive().stream()
                .map(mapper::toResult)
                .toList();
    }
}
