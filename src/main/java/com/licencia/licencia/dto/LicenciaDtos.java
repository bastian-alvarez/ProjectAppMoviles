package com.licencia.licencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalDate;

public final class LicenciaDtos {

  private LicenciaDtos() {}

  public record LicenciaResponse(
      String id,
      String clave,
      LocalDate fechaVencimiento,
      String estadoId,
      String juegoId,
      String usuarioId,
      Instant asignadaEn
  ) {}

  public record CreateLicenciaRequest(
      @NotBlank String id,
      @NotBlank String clave,
      @Schema(example = "2026-12-31")
      LocalDate fechaVencimiento,
      String estadoId,
      String juegoId
  ) {}

  public record UpdateLicenciaRequest(
      @NotBlank String clave,
      LocalDate fechaVencimiento,
      String estadoId,
      String juegoId
  ) {}

  public record AssignLicenciaRequest(
      @NotBlank String usuarioId
  ) {}
}




