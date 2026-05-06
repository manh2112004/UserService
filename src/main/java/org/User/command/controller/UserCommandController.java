package org.User.command.controller;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import org.User.command.command.CreateUserCommand;
import org.User.command.model.RegisterRequestModel;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users/register")
public class UserCommandController {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private Keycloak keycloak;

@   PostMapping
    public CompletableFuture<String> register(@Valid @RequestBody RegisterRequestModel model){
        // --- BƯỚC 1: GỌI KEYCLOAK API TẠO USER ---
        UserRepresentation user = new UserRepresentation();
        user.setUsername(model.getEmail());
        user.setEmail(model.getEmail());
        user.setFirstName(model.getFullName());
        user.setEnabled(true);
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(model.getPassword());
        cred.setTemporary(false);
        user.setCredentials(Collections.singletonList(cred));
        Response response = keycloak.realm("jobhunt").users().create(user);
        if (response.getStatus() != 201) {
            throw new RuntimeException("Không thể tạo tài khoản trên Keycloak");
        }
        // --- BƯỚC 2: LẤY ID (SUB) TỪ KEYCLOAK ---
        // Keycloak trả về ID trong Header Location của Response
        String path = response.getLocation().getPath();
        String keycloakUserId = path.substring(path.lastIndexOf('/') + 1);
        CreateUserCommand command = new CreateUserCommand(
                keycloakUserId, // Đây chính là @TargetAggregateIdentifier
                model.getFullName(),
                model.getEmail(),
                model.getUserType()
        );

        return commandGateway.send(command);
    }
}
