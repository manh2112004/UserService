package org.User.command.event;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreatedEvent {
    private String userId; // keycloakUserId sẽ truyền vào đây
    private String username;
    private String email;
    private String userType;
}
