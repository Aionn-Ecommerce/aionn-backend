package com.aionn.promotion.infrastructure.adapter;

import com.aionn.promotion.application.port.out.VoucherRepository;
import com.aionn.promotion.domain.model.Voucher;
import com.aionn.promotion.infrastructure.persistence.entity.VoucherEntity;
import com.aionn.promotion.infrastructure.persistence.mapper.VoucherDomainMapper;
import com.aionn.promotion.infrastructure.persistence.repository.VoucherJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VoucherRepositoryAdapter implements VoucherRepository {

    private final VoucherJpaRepository jpa;
    private final VoucherDomainMapper mapper;

    @Override
    public Voucher save(Voucher voucher) {
        VoucherEntity existing = jpa.findById(voucher.getVoucherCode()).orElse(null);
        return mapper.toDomain(jpa.save(mapper.toEntity(voucher, existing)));
    }

    @Override
    public Optional<Voucher> findByCode(String voucherCode) {
        return jpa.findById(voucherCode).map(mapper::toDomain);
    }

    @Override
    public Optional<Voucher> lockByCode(String voucherCode) {
        return jpa.findForUpdate(voucherCode).map(mapper::toDomain);
    }
}

