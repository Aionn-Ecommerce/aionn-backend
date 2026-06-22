package com.aionn.chat.application.usecase.block;

import com.aionn.chat.application.dto.block.command.BlockCommands;
import com.aionn.chat.application.dto.block.result.BlockResult;
import com.aionn.chat.application.port.in.block.UnblockUserInputPort;
import com.aionn.chat.application.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnblockUserUseCase implements UnblockUserInputPort {

    private final UserBlockService userBlockService;

    @Override
    public BlockResult execute(BlockCommands.UnblockUser command) {
        return userBlockService.unblock(command);
    }
}
