package org.User.command.aggregate;

import lombok.NoArgsConstructor;
import org.User.command.command.CreateUserCommand;
import org.User.command.command.ForgotPasswordCommand;
import org.User.command.command.ResetPasswordCommand;
import org.User.command.command.VerifyEmailCommand;
import org.User.command.event.PasswordResetConfirmedEvent;
import org.User.command.event.PasswordResetRequestedEvent;
import org.User.command.event.UserCreatedEvent;
import org.User.command.event.UserEmailVerifiedEvent;
import org.User.command.service.authService;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

@Aggregate
@NoArgsConstructor
public class authAggregate {
    @AggregateIdentifier
    private String userId; // Đây chính là keycloakUserId từ Command
    private String fullName;
    private String email;
    private String userType;
    private boolean emailVerified;
    @CommandHandler
    public authAggregate(CreateUserCommand command) {
        AggregateLifecycle.apply(UserCreatedEvent.builder()
                .userId(command.getUserId())
                .username(command.getFullName())
                .email(command.getEmail())
                .userType(command.getUserType())
                .build());
    }
    @CommandHandler
    public void handle(VerifyEmailCommand command, authService authService) {
        // 1. Kiểm tra nghiệp vụ (Invariants)
        if (this.emailVerified) {
            throw new IllegalStateException("Email này đã được xác thực trước đó!");
        }
        // 2. Gọi Service để thực thi với Keycloak
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

    @EventSourcingHandler
    public void on(PasswordResetRequestedEvent event) {
        this.userId = event.getUserId();
        this.email = event.getEmail();
    }
    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        // Cập nhật trạng thái của Aggregate từ Event
        this.userId = event.getUserId();
        this.fullName = event.getUsername();
        this.email = event.getEmail();
        this.userType = event.getUserType();
    }
    @EventSourcingHandler
    public void on(UserEmailVerifiedEvent event) {
        this.userId = event.getUserId();
        this.emailVerified = true; // Cập nhật trạng thái Aggregate
    }
}
