package com.licencia.licencia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "licencia")
public class Licencia {

  @Id
  @Column(name = "id_licencia", length = 20, nullable = false)
  private String id;

  @NotBlank
  @Column(name = "clave", nullable = false, unique = true)
  private String clave;

  @NotNull
  @Column(name = "fecha_vencimiento", nullable = false)
  private LocalDate fechaVencimiento;

  @Column(name = "id_estado", length = 20)
  private String estadoId;

  @Column(name = "id_juego", length = 20)
  private String juegoId;

  @Column(name = "id_usuario", length = 20)
  private String usuarioId;

  @Column(name = "asignada_en")
  private Instant asignadaEn;

  public String getId() { return id; }

  public void setId(String id) { this.id = id; }

  public String getClave() { return clave; }

  public void setClave(String clave) { this.clave = clave; }

  public LocalDate getFechaVencimiento() { return fechaVencimiento; }

  public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

  public String getEstadoId() { return estadoId; }

  public void setEstadoId(String estadoId) { this.estadoId = estadoId; }

  public String getJuegoId() { return juegoId; }

  public void setJuegoId(String juegoId) { this.juegoId = juegoId; }

  public String getUsuarioId() { return usuarioId; }

  public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

  public Instant getAsignadaEn() { return asignadaEn; }

  public void setAsignadaEn(Instant asignadaEn) { this.asignadaEn = asignadaEn; }
}


