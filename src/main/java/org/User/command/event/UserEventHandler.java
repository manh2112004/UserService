package org.User.command.event;

import lombok.RequiredArgsConstructor;
import org.User.command.data.UserRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventHandler {
    @Autowired
    private UserRepository userRepository;
    @EventHandler
    public void on(UserStatusUpdatedEvent event) {
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            user.setActive(event.isActive());
            userRepository.save(user);
        });
    }
}
