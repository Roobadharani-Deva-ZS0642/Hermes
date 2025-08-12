package org.intics.hermes.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}
