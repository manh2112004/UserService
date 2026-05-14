package org.User.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@Builder
public class UpdateRoleCommand {
    @TargetAggregateIdentifier
    private String id; // ID này phải khớp với ID của MANAGER hiện tại
    private String roleName;
    private String description;
    private List<String> permissionNames;
}
