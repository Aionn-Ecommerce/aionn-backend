package com.aionn.identity.infrastructure.persistence.repository.account;

import com.aionn.identity.infrastructure.persistence.entity.DataExportRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataExportRequestRepository extends JpaRepository<DataExportRequestEntity, String> {

    boolean existsByUser_UserIdAndStatusIn(String userId, java.util.Collection<String> statuses);
}



