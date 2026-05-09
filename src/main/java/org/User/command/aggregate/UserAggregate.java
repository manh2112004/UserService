package org.User.command.aggregate;

import lombok.NoArgsConstructor;
import org.User.command.command.CreateUserCommand;
import org.User.command.event.UserCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@NoArgsConstructor
public class UserAggregate {
    @AggregateIdentifier
    private String userId; // Đây chính là keycloakUserId từ Command
    private String fullName;
    private String email;
    private String userType;
    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        AggregateLifecycle.apply(UserCreatedEvent.builder()
                .userId(command.getUserId())
                .username(command.getFullName())
                .email(command.getEmail())
                .userType(command.getUserType())
                .build());
    }
    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        // Cập nhật trạng thái của Aggregate từ Event
        this.userId = event.getUserId();
        this.fullName = event.getUsername();
        this.email = event.getEmail();
        this.userType = event.getUserType();
    }
}
