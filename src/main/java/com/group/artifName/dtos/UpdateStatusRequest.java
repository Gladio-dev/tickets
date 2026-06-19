package com.group.artifName.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String status; // Aquí mandarán "EN_PROCESO" o "RESUELTO"
}