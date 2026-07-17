package com.group.artifName.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetDto {
    @NotNull(message = "Es obligatorio el ID")
    private Long id;
}