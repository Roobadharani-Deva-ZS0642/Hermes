package org.intics.hermes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseStatusPayload<T> {

    private Object payload;
    private String status;
    private Integer errorCode;
    private String errorMsg;
    private LocalDateTime responseTimeStamp;


    public ResponseStatusPayload(final T stackTrace, final Integer errorCode, final String errorMsg) {
        this.payload = stackTrace;
        this.errorCode = errorCode;
        this.status = "Fail";
        this.errorMsg = errorMsg;
        this.responseTimeStamp = LocalDateTime.now(Clock.systemDefaultZone());
    }

    public ResponseStatusPayload(final T payload) {
        this.payload = payload;
        this.status = "Success";
        this.responseTimeStamp = LocalDateTime.now(Clock.systemDefaultZone());

    }
    public String getResponseTimeStamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return responseTimeStamp.format(formatter);
    }

    @JsonProperty("payload")
    public Object getPayload() {
        if (payload == null) {
            return new ObjectMapper().createObjectNode();
        }
        return payload;
    }
}
