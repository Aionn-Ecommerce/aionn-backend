package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.ecommerce.identity.application.dto.auth.command.LinkSocialCommand;
import com.ecommerce.identity.application.dto.auth.view.SocialLinkView;
import com.ecommerce.identity.application.port.in.auth.LinkSocialInputPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkSocialUseCase implements LinkSocialInputPort {

    private final AuthService authService;
    private final AuthDtoMapper authDtoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SocialLinkView linkSocial(LinkSocialCommand command) {
        var socialLink = authService.linkSocial(command);
        return authDtoMapper.toSocialLinkView(socialLink);
    }
}
