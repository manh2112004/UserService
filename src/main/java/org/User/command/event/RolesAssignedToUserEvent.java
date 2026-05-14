package org.User.command.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolesAssignedToUserEvent {
    private String userId;
    private List<String> roleNames;
}
