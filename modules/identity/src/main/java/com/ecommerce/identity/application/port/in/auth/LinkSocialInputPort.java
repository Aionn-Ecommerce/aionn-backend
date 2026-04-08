package com.ecommerce.identity.application.port.in.auth;

import com.ecommerce.identity.application.dto.auth.command.LinkSocialCommand;
import com.ecommerce.identity.application.dto.auth.view.SocialLinkView;

public interface LinkSocialInputPort {

    SocialLinkView linkSocial(LinkSocialCommand command);

    default SocialLinkView execute(LinkSocialCommand command) {
        return linkSocial(command);
    }
}


