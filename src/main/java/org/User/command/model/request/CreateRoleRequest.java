package org.User.command.model.request;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
// Dòng dưới đây sẽ ép Jackson coi đây là Object thuần túy, bỏ qua Metadata class
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class CreateRoleRequest {
    private String roleName;
    private String description;
    private List<Long> permissionIds;
}
