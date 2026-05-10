package org.User.command.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationEmailResentEvent {
    private String userId;
    private String email;
    private String verificationCode; // Mã mới để gửi trong mail
}
