package org.User.command.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdatedEvent {
    private String id;
    private String username;
    private String phoneNumber;
    private String email;
}
