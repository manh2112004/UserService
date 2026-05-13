package org.User.command.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
public class Permission {
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String permissionName; // Ví dụ: "CREATE_JOB", "VIEW_CV"

    private String description;
}