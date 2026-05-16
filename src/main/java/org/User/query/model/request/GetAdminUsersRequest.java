package org.User.query.model.request;

import lombok.Data;
import org.User.constant.UserType;

@Data
public class GetAdminUsersRequest {
    private String email;
    private UserType userType;
    private Boolean isActive;
    private String roleName;
    private int page = 0;
    private int size = 10;
}
