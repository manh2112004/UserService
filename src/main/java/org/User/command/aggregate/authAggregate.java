package org.User.command.aggregate;

import lombok.NoArgsConstructor;
import org.User.command.command.*;
import org.User.command.event.*;
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
    private String phone_number;
    private String avatarUrl;
    private String userType;
    private boolean emailVerified;
    private boolean isActive;
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
    public void handle(UpdateUserCommand command) {
        if (command.getEmail() == null || !command.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }

        if (command.getPhoneNumber() == null ||
                !command.getPhoneNumber().matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại phải bao gồm 10 chữ số!");
        }

        AggregateLifecycle.apply(
                new UserUpdatedEvent(
                        command.getUserId(),
                        command.getUsername(),
                        command.getPhoneNumber(),
                        command.getEmail()
                )
        );
    }
    @CommandHandler
    public void handle(UpdateUserAvatarCommand command) {
        // Bắn sự kiện ra hệ thống
        AggregateLifecycle.apply(new UserAvatarUpdatedEvent(
                command.getUserId(),
                command.getAvatarUrl()
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
        this.fullName = event.getUsername();
        this.email = event.getEmail();
        this.userType = event.getUserType();
    }
    @EventSourcingHandler
    public void on(UserEmailVerifiedEvent event) {
        this.userId = event.getUserId();
        this.emailVerified = true; // Cập nhật trạng thái Aggregate
    }
    @EventSourcingHandler
    public void on(UserUpdatedEvent event) {
        this.userId = event.getId();
        this.fullName = event.getUsername();
        this.phone_number = event.getPhoneNumber();
        this.email = event.getEmail();
    }
    @EventSourcingHandler
    public void on(UserAvatarUpdatedEvent event) {
        this.userId = event.getUserId();
        this.avatarUrl = event.getAvatarUrl();
    }
}
