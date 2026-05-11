package org.User.command.model.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String phoneNumber;
    private String email;
}
