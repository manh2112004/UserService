package org.User.command.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.User.command.command.UpdateUserAvatarCommand;
import org.User.command.command.UpdateUserCommand;
import org.User.command.data.UserRepository;
import org.User.command.model.request.UserUpdateRequest;
import org.User.command.service.userService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements userService {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Cloudinary cloudinary;
    public CompletableFuture<String> updateUser(String userId, UserUpdateRequest request) {
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new RuntimeException("Email đã được sử dụng bởi người dùng khác!");
        }
        if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
            throw new RuntimeException("Số điện thoại đã được sử dụng!");
        }
        UpdateUserCommand command = UpdateUserCommand.builder()
                .userId(userId)
                .username(request.getUsername())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .build();
        return commandGateway.send(command);
    }

    @Override
    public CompletableFuture<String> uploadImage(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                return uploadResult.get("url").toString();
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteAvatar(String userId) {
        // 1. Tìm user để lấy URL avatar hiện tại
        return CompletableFuture.runAsync(() -> {
            userRepository.findById(userId).ifPresent(user -> {
                String url = user.getAvatarUrl();
                if (url != null && !url.isEmpty()) {
                    try {
                        // 2. Trích xuất Public ID từ URL Cloudinary để xóa
                        // Ví dụ: http://res.cloudinary.com/demo/image/upload/v1234/sample.jpg -> sample
                        String publicId = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
                        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    } catch (IOException e) {
                        throw new RuntimeException("Không thể xóa ảnh trên Cloudinary");
                    }
                }
            });
        }).thenCompose(result -> {
            // 3. Gửi Command để set avatarUrl về null trong DB và Aggregate
            return commandGateway.send(new UpdateUserAvatarCommand(userId, null));
        }).thenAccept(result -> {});
    }
}
