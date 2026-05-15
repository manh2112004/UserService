package org.User.command.aggregate;

import lombok.NoArgsConstructor;
import org.User.command.command.*;
import org.User.command.event.*;
import org.User.command.service.RoleService;
import org.User.command.service.UserService;
import org.User.command.service.authService;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Aggregate
@NoArgsConstructor
public class authAggregate {
    @AggregateIdentifier
    private String userId; // Đây chính là keycloakUserId từ Command
    private String email;
    private String userType;
    private boolean emailVerified;
    private boolean isActive;
    private Set<String>userRoles=new HashSet<>();
    @CommandHandler
    public authAggregate(CreateUserCommand command) {
        AggregateLifecycle.apply(UserCreatedEvent.builder()
                .userId(command.getUserId())
                .email(command.getEmail())
                .userType(command.getUserType())
                .build());
    }
    @CommandHandler
    public void handle(VerifyEmailCommand command, authService authService) {
        if (this.emailVerified) {
            throw new IllegalStateException("Email này đã được xác thực trước đó!");
        }
        authService.verifyEmailInKeycloak(command.getUserId());
        // 3. Nếu thành công, áp dụng Event để lưu vào Event Store
        AggregateLifecycle.apply(new UserEmailVerifiedEvent(command.getUserId()));
    }
    // 1. Xử lý Forgot Password
    @CommandHandler
    public void handle(ForgotPasswordCommand command, authService authService) {
        String foundUserId = authService.findUserIdByEmail(command.getEmail());

        if (foundUserId == null) {
            throw new IllegalArgumentException("Email không tồn tại trong hệ thống!");
        }

        // Tạo mã reset ngẫu nhiên
        String resetCode = java.util.UUID.randomUUID().toString();

        // Phát event để các service khác (như Mail Service) xử lý
        AggregateLifecycle.apply(new PasswordResetRequestedEvent(
                foundUserId,
                command.getEmail(),
                resetCode
        ));
    }
    // Xử lý Đặt lại mật khẩu mới
    @CommandHandler
    public void handle(ResetPasswordCommand command, authService authService) {
        // 1. Cập nhật trực tiếp lên Keycloak
        authService.updatePasswordInKeycloak(command.getUserId(), command.getNewPassword());

        // 2. Lưu sự kiện xác nhận đổi mật khẩu vào Event Store
        AggregateLifecycle.apply(new PasswordResetConfirmedEvent(command.getUserId()));
    }
    @CommandHandler
    public void handle(ResendVerificationCommand command) {
        // Kiểm tra trạng thái hiện tại của User trong Aggregate
        // Nếu isActive đã là true (đã xác thực rồi) thì không cho gửi lại nữa
        if (this.isActive) {
            throw new IllegalStateException("Tài khoản này đã được xác thực trước đó rồi!");
        }
        // Tạo mã xác thực mới (ví dụ 6 số hoặc UUID)
        String newCode = java.util.UUID.randomUUID().toString();
        // Bắn sự kiện để Notification Service xử lý việc gửi Mail thực tế
        AggregateLifecycle.apply(new VerificationEmailResentEvent(
                command.getUserId(),
                command.getEmail(),
                newCode
        ));
    }
    @CommandHandler
    public void handle(UpdateUserStatusCommand command, UserService userService) {
        // 2. Gọi Keycloak để disable/enable user trên server bảo mật
        userService.updateUserStatusInKeycloak(command.getUserId(), command.isActive());
        // 3. Phát Event cập nhật DB
        AggregateLifecycle.apply(new UserStatusUpdatedEvent(
                command.getUserId(),
                command.isActive()
        ));
    }
    @CommandHandler
    public void handle(AssignRoleToUserCommand command, UserService userService) {
        // Gọi RoleService (hoặc KeycloakService) để thực hiện gán Role thực tế trên Keycloak
        // Lưu ý: Bạn có thể inject RoleService vào Handler này
        userService.assignRolesToUserInKeycloak(command.getUserId(), command.getRoleNames());
        // Phát event sau khi gán thành công
        AggregateLifecycle.apply(new RolesAssignedToUserEvent(
                command.getUserId(),
                command.getRoleNames()
        ));
    }
    @EventSourcingHandler
    public void on(PasswordResetRequestedEvent event) {
        this.userId = event.getUserId();
        this.email = event.getEmail();
    }
    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        // Cập nhật trạng thái của Aggregate từ Event
        this.userId = event.getUserId();
        this.email = event.getEmail();
        this.userType = event.getUserType();
    }
    @EventSourcingHandler
    public void on(UserEmailVerifiedEvent event) {
        this.userId = event.getUserId();
        this.emailVerified = true; // Cập nhật trạng thái Aggregate
    }
    @EventSourcingHandler
    public void on(RolesAssignedToUserEvent event) {
        // 1. Cập nhật ID (Phòng trường hợp đây là sự kiện đầu tiên, dù thường là UserCreatedEvent)
        this.userId = event.getUserId();
        // 2. Cập nhật danh sách Role vào trạng thái nội tại của Aggregate
        if (event.getRoleNames() != null) {
            this.userRoles.addAll(event.getRoleNames());
        }
    }
    @EventSourcingHandler
    public void on(UserStatusUpdatedEvent event) {
        this.userId = event.getUserId();
        this.isActive = event.isActive();
    }
}
