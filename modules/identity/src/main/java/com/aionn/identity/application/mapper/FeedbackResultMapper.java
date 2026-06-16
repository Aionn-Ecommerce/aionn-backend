package com.aionn.identity.application.mapper;

import com.aionn.identity.application.dto.feedback.result.FeedbackResult;
import com.aionn.identity.infrastructure.persistence.entity.UserFeedbackEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackResultMapper {

    @Mapping(target = "category", expression = "java(entity.getCategory().name())")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "rating", expression = "java(entity.getRating() == null ? null : entity.getRating().intValue())")
    FeedbackResult toResult(UserFeedbackEntity entity);
}
