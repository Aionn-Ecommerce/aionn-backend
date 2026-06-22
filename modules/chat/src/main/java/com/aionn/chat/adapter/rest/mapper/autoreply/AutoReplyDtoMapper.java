package com.aionn.chat.adapter.rest.mapper.autoreply;

import com.aionn.chat.adapter.rest.dto.autoreply.request.UpdateAutoReplyRequest;
import com.aionn.chat.adapter.rest.dto.autoreply.response.AutoReplyResponse;
import com.aionn.chat.application.dto.autoreply.command.AutoReplyCommands;
import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutoReplyDtoMapper {

    @Mapping(target = "ownerId", ignore = true)
    AutoReplyCommands.UpdateAutoReply toUpdateCommand(String merchantId, UpdateAutoReplyRequest request);

    AutoReplyResponse toResponse(AutoReplyResult result);
}
