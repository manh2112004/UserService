package org.User.query.queries;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.User.constant.UserType;

@Getter
@AllArgsConstructor
public class GetAdminUsersQuery {
    private String email;
    private UserType userType;
    private Boolean isActive;
    private String roleName;
    private int page;
    private int size;
}
