package dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private Integer status;
    private String errorCode;
    private String message;
}
