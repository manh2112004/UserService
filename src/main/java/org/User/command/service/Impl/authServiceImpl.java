package org.User.command.service.Impl;
import jakarta.ws.rs.core.Response;
import org.User.command.command.CreateUserCommand;
import org.User.command.model.request.LoginRequestModel;
import org.User.command.model.request.RegisterRequestModel;
import org.User.command.model.response.LoginResponseDTO;
import org.User.command.service.authService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
@Service
public class authServiceImpl implements authService {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;

    private final String REALM = "jobhunt";
    @Override
    public CompletableFuture<String> registerUser(RegisterRequestModel model) {
        // 1. Tạo đối tượng User cơ bản trên Keycloak
        UserRepresentation user = new UserRepresentation();
        user.setUsername(model.getEmail());
        user.setEmail(model.getEmail());
        user.setFirstName(model.getFullName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());

        Response response = keycloak.realm(REALM).users().create(user);

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
        keycloak.realm(REALM).users().get(keycloakUserId).resetPassword(cred);

        // 4. Gửi Command sang Axon
        CreateUserCommand command = new CreateUserCommand(
                keycloakUserId,
                model.getFullName(),
                model.getEmail(),
                model.getUserType()
        );
        return commandGateway.send(command);
    }

    @Override
    public LoginResponseDTO login(LoginRequestModel model) {
        String url = keycloakServerUrl + "/realms/" + REALM + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "jobhunt-res-server");
        body.add("username", model.getEmail());
        body.add("password", model.getPassword());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity(url, entity, LoginResponseDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu!");
        }
    }
}
