package org.intics.hermes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundRequest {
    private String documentId;
    private String requestTxnId;
    private List<FileId> inputDocuments;
    private String source;
    private String documentSource;
    private String healthPlan;
    private String routePath;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileId {
        private String id;
    }
}
