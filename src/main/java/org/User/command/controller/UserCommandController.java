package org.User.command.controller;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import org.User.command.command.CreateUserCommand;
import org.User.command.model.request.LoginRequestModel;
import org.User.command.model.request.RegisterRequestModel;
import org.User.command.model.response.LoginResponseDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
public class UserCommandController {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private Keycloak keycloak;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;

    @PostMapping("/register")
    public CompletableFuture<String> register(@Valid @RequestBody RegisterRequestModel model) {
        // 1. Tạo đối tượng User cơ bản
        UserRepresentation user = new UserRepresentation();
        user.setUsername(model.getEmail());
        user.setEmail(model.getEmail());
        user.setFirstName(model.getFullName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());

        // 2. Gọi API tạo User
        Response response = keycloak.realm("jobhunt").users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Lỗi Keycloak: " + response.getStatus());
        }

        // 3. Lấy ID từ Header Location (Chuẩn hơn)
        String keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // 4. Thiết lập mật khẩu không tạm thời
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(model.getPassword());
        cred.setTemporary(false);

        // Reset password để xác nhận trạng thái "Full set up"
        keycloak.realm("jobhunt").users().get(keycloakUserId).resetPassword(cred);

        // 5. Gửi sang Axon
        CreateUserCommand command = new CreateUserCommand(
                keycloakUserId,
                model.getFullName(),
                model.getEmail(),
                model.getUserType()
        );
        return commandGateway.send(command);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestModel loginRequest) {
        String url = keycloakServerUrl + "/realms/jobhunt/protocol/openid-connect/token";
        // 1. Header chuẩn OAuth2
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 2. Body gửi sang Keycloak
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "jobhunt-res-server");
        body.add("username", loginRequest.getEmail());
        body.add("password", loginRequest.getPassword());

        try {
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(url, entity, LoginResponseDTO.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi chi tiết: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản hoặc mật khẩu không chính xác!");
        }
    }
}
