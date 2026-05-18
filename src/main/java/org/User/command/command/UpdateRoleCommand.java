package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleCommand {
    @TargetAggregateIdentifier
    private String id; // ID này phải khớp với ID của MANAGER hiện tại
    private String roleName;
    private String description;
}
