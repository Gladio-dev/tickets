package com.group.artifName.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PasswordDto {

    @NotNull(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "La contraseña es obligatoria")
    private UUID uuid;
}
