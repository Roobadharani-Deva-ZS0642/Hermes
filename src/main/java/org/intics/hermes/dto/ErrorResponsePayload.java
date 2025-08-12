package org.intics.hermes.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponsePayload {

    private Object message;
    private String error;
    private Integer status;
    private String path;
    private LocalDateTime timeStamp;
    private String trace;

}
