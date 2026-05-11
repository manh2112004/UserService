package org.User.command.event;

import lombok.RequiredArgsConstructor;
import org.User.command.data.UserRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventsHandler {
    @Autowired
    private UserRepository userRepository;
    @EventHandler
    public void on(UserUpdatedEvent event) {
        userRepository.findById(event.getId()).ifPresent(user -> {
            user.setUsername(event.getUsername());
            user.setPhoneNumber(event.getPhoneNumber());
            user.setEmail(event.getEmail());
            userRepository.save(user);
        });
    }
    @EventHandler
    public void on(UserAvatarUpdatedEvent event){
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            user.setAvatarUrl(event.getAvatarUrl());
            userRepository.save(user);
        });
    }

}
