package com.vuthevy1209.springmail.event;

import com.vuthevy1209.springmail.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserLoggedInEvent extends ApplicationEvent {
    private final User user;
    private final String accessToken;

    public UserLoggedInEvent(Object source, User user, String accessToken) {
        super(source);
        this.user = user;
        this.accessToken = accessToken;
    }
}
