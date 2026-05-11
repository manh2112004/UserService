package org.User.command.event;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAvatarUpdatedEvent {
    private String userId;
    private String avatarUrl;
}
