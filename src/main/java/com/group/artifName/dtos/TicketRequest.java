package com.group.artifName.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRequest {

    @NotBlank(message = "El título del ticket es obligatorio")
    private String title;

    @NotBlank(message = "La descripción del ticket es obligatoria")
    private String description;
}