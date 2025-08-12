package org.intics.hermes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CurrentUser {

    private String username;
    private Long tenantId;
    private String role;
    private String token;
    private String tokenExpireDatetime;

}

