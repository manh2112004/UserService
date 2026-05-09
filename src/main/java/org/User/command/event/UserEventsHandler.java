package org.User.command.event;

import lombok.RequiredArgsConstructor;
import org.User.command.data.User;
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
    public void on(UserCreatedEvent event) {
        User userEntity = new User();
        userEntity.setId(event.getUserId()); // Dùng ID từ Keycloak
        userEntity.setUsername(event.getUsername());
        userEntity.setEmail(event.getEmail());
        userEntity.setUserType(event.getUserType());
        userEntity.setKeycloakUid(event.getUserId());
        userRepository.save(userEntity);
    }
}
