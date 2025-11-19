import com.licencia.licencia.dto.LicenciaDtos.AssignLicenciaRequest;
import com.licencia.licencia.dto.LicenciaDtos.CreateLicenciaRequest;
import com.licencia.licencia.dto.LicenciaDtos.LicenciaResponse;
import com.licencia.licencia.dto.LicenciaDtos.UpdateLicenciaRequest;
import com.licencia.licencia.model.Licencia;
import com.licencia.licencia.service.LicenciaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Licencias", description = "Gestión de licencias")
@RestController
@RequestMapping("/licencias")
public class LicenciaController {

  private final LicenciaService service;
  private final LicenciaAssembler assembler;

  public LicenciaController(LicenciaService service) {
    this.service = service;
    this.assembler = new LicenciaAssembler();
  }

  @Operation(summary = "Listar licencias (paginado y filtros)")
  @GetMapping
  public CollectionModel<EntityModel<LicenciaResponse>> list(
      @RequestParam(required = false) String juegoId,
      @RequestParam(required = false) String estadoId,
      @PageableDefault(size = 20, sort = "fechaVencimiento") Pageable pageable) {

    Page<Licencia> page = service.listar(juegoId, estadoId, pageable);
    var models = page.getContent().stream().map(assembler::toModel).toList();

    return CollectionModel.of(models,
      linkTo(methodOn(LicenciaController.class).list(juegoId, estadoId, pageable)).withSelfRel(),
      linkTo(methodOn(LicenciaController.class).create(null)).withRel("create"));
  }

  @Operation(summary = "Listar licencias disponibles por juego")
  @GetMapping("/disponibles")
  public ResponseEntity<List<LicenciaResponse>> listDisponibles(
      @RequestParam String juegoId,
      @PageableDefault(size = 20) Pageable pageable) {
    var disponibles = service.listarDisponibles(juegoId, pageable);
    return ResponseEntity.ok(disponibles.stream().map(assembler::mapToResponse).toList());
  }

  @Operation(summary = "Obtener licencia por id")
  @GetMapping("/{id}")
  public EntityModel<LicenciaResponse> get(@PathVariable String id) {
    return assembler.toModel(service.obtener(id));
  }

  @Operation(summary = "Buscar licencia por clave")
  @GetMapping("/buscar")
  public ResponseEntity<EntityModel<LicenciaResponse>> buscarPorClave(@RequestParam String clave) {
    return service.buscarPorClave(clave)
      .map(l -> ResponseEntity.ok(assembler.toModel(l)))
      .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Crear licencia")
  @PostMapping
  public ResponseEntity<EntityModel<LicenciaResponse>> create(@Valid @RequestBody CreateLicenciaRequest body) {
    var entity = new Licencia();
    entity.setId(body.id());
    entity.setClave(body.clave());
    entity.setFechaVencimiento(body.fechaVencimiento());
    entity.setEstadoId(body.estadoId());
    entity.setJuegoId(body.juegoId());
    var saved = service.crear(entity);
    return ResponseEntity
      .created(linkTo(methodOn(LicenciaController.class).get(saved.getId())).toUri())
      .body(assembler.toModel(saved));
  }

  @Operation(summary = "Actualizar licencia")
  @PutMapping("/{id}")
  public EntityModel<LicenciaResponse> update(@PathVariable String id, @Valid @RequestBody UpdateLicenciaRequest body) {
    var entity = service.obtener(id);
    entity.setClave(body.clave());
    if (body.fechaVencimiento() != null) {
      entity.setFechaVencimiento(body.fechaVencimiento());
    }
    entity.setEstadoId(body.estadoId());
    entity.setJuegoId(body.juegoId());
    return assembler.toModel(service.actualizar(id, entity));
  }

  @Operation(summary = "Asignar licencia a un usuario")
  @PostMapping("/{id}/asignar")
  public EntityModel<LicenciaResponse> assign(
      @PathVariable String id,
      @Valid @RequestBody AssignLicenciaRequest request) {
    return assembler.toModel(service.asignar(id, request.usuarioId()));
  }

  @Operation(summary = "Liberar licencia")
  @PostMapping("/{id}/liberar")
  public EntityModel<LicenciaResponse> release(@PathVariable String id) {
    return assembler.toModel(service.liberar(id));
  }

  @Operation(summary = "Eliminar licencia")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  static class LicenciaAssembler implements RepresentationModelAssembler<Licencia, EntityModel<LicenciaResponse>> {
    @Override public EntityModel<LicenciaResponse> toModel(Licencia l) {
      return EntityModel.of(mapToResponse(l),
        linkTo(methodOn(LicenciaController.class).get(l.getId())).withSelfRel(),
        linkTo(methodOn(LicenciaController.class).list(null,null,PageRequest.of(0,20))).withRel("collection"));
    }

    LicenciaResponse mapToResponse(Licencia licencia) {
      return new LicenciaResponse(
          licencia.getId(),
          licencia.getClave(),
          licencia.getFechaVencimiento(),
          licencia.getEstadoId(),
          licencia.getJuegoId(),
          licencia.getUsuarioId(),
          licencia.getAsignadaEn()
      );
    }
  }
}



