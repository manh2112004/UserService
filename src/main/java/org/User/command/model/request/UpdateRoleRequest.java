package org.User.command.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {
    private String roleName;
    private String description;
}
