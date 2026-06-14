package com.aionn.promotion.application.port.out;

import com.aionn.promotion.domain.model.Voucher;

import java.util.Optional;

public interface VoucherPersistencePort {

    Voucher save(Voucher voucher);

    Optional<Voucher> findByCode(String voucherCode);

    Optional<Voucher> lockByCode(String voucherCode);
}
