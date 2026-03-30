package com.ecommerce.identity.application.usecase.agent;

import com.ecommerce.identity.application.dto.agent.AgentIdentityResult;
import com.ecommerce.identity.application.dto.agent.UpdateAgentPermissionsCommand;
import com.ecommerce.identity.application.port.in.agent.UpdateAgentPermissionsInputPort;
import com.ecommerce.identity.application.service.AgentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateAgentPermissionsUseCase implements UpdateAgentPermissionsInputPort {

    private final AgentService agentService;

    @Override
    public AgentIdentityResult execute(UpdateAgentPermissionsCommand command) {
        var result = agentService.updatePermissions(command.ownerUserId(), command.agentId(), command.permissionsJson());
        return AgentResultMapper.toIdentityResult(result);
    }
}
