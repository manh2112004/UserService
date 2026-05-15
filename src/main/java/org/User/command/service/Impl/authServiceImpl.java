package org.User.command.service.Impl;
import jakarta.ws.rs.core.Response;
import org.User.command.command.CreateUserCommand;
import org.User.command.model.request.LoginRequestModel;
import org.User.command.model.request.RegisterRequestModel;
import org.User.command.model.response.LoginResponseDTO;
import org.User.command.service.authService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
@Service
public class authServiceImpl implements authService {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;
    @Override
    public CompletableFuture<String> registerUser(RegisterRequestModel model) {
        // 1. Tạo đối tượng User cơ bản trên Keycloak
        UserRepresentation user = new UserRepresentation();
        user.setUsername(model.getEmail());
        user.setEmail(model.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Lỗi tạo user trên Keycloak, status: " + response.getStatus());
        }

        // 2. Lấy Keycloak User ID
        String keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // 3. Thiết lập mật khẩu
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(model.getPassword());
        cred.setTemporary(false);
        keycloak.realm(realm).users().get(keycloakUserId).resetPassword(cred);

        // 4. Gửi Command sang Axon
        CreateUserCommand command = new CreateUserCommand(
                keycloakUserId,
                model.getEmail(),
                model.getUserType()
        );
        return commandGateway.send(command);
    }

    @Override
    public LoginResponseDTO login(LoginRequestModel model) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", model.getEmail());
        body.add("password", model.getPassword());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(url, entity, LoginResponseDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();

            // In ra để kiểm chứng (bạn đã thấy dòng này trong log)
            System.err.println("Chi tiết lỗi Keycloak: " + errorBody);

            // Kiểm tra nếu error_description chứa cụm từ khóa về tài khoản bị khóa
            if (errorBody.contains("Account disabled") || errorBody.contains("user_disabled")) {
                throw new RuntimeException("Tài khoản đang bị khóa. Vui lòng liên hệ Admin!");
            }

            // Kiểm tra lỗi khóa tạm thời do nhập sai pass quá nhiều (Brute Force)
            if (errorBody.contains("user_temporarily_disabled")) {
                throw new RuntimeException("Tài khoản tạm thời bị khóa. Hãy thử lại sau ít phút!");
            }

            // Các trường hợp invalid_grant khác (thường là sai pass hoặc sai user)
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu!");
        }
    }

    @Override
        public Map<String, Object> refreshToken(String refreshToken) {
        String url = "http://localhost:8080/realms/jobhunt/protocol/openid-connect/token";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret); // Nếu client là access type: confidential
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Keycloak error: " + e.getMessage());
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
    }

    @Override
    public void logout(String refreshToken) {
        // 1. Tạo URL logout của Keycloak
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        // 2. Thiết lập Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3. Thiết lập Body (Chứa client_id, client_secret và refresh_token)
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // 4. Gọi đến Keycloak để hủy Session
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("Logout thành công trên Keycloak");
        } catch (Exception e) {
            System.err.println("Lỗi Logout Keycloak: " + e.getMessage());
            throw new RuntimeException("Không thể đăng xuất, vui lòng thử lại!");
        }
    }

    @Override
    public void verifyEmailInKeycloak(String userId) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            user.setEmailVerified(true);
            userResource.update(user);
        } catch (Exception e) {
            throw new RuntimeException("Keycloak error: " + e.getMessage());
        }
    }

    @Override
    public String findUserIdByEmail(String email) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(null, null, null, email, 0, 1);
        return users.isEmpty() ? null : users.get(0).getId();
    }

    @Override
    public void updatePasswordInKeycloak(String userId, String newPassword) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(newPassword);
        cred.setTemporary(false); // Người dùng không cần đổi lại lần nữa khi đăng nhập

        userResource.resetPassword(cred);
    }
}
