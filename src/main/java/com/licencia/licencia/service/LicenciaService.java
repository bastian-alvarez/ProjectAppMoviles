import com.licencia.licencia.model.Licencia;
import com.licencia.licencia.repository.LicenciaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class LicenciaService {

  public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
  public static final String ESTADO_ASIGNADA = "ASIGNADA";

  private final LicenciaRepository repo;

  public LicenciaService(LicenciaRepository repo) { this.repo = repo; }

  public Page<Licencia> listar(String juegoId, String estadoId, Pageable p) {
    if (juegoId != null && !juegoId.isBlank() && estadoId != null && !estadoId.isBlank()) {
      return repo.findByJuegoIdAndEstadoId(juegoId, estadoId, p);
    }
    if (juegoId != null && !juegoId.isBlank()) return repo.findByJuegoId(juegoId, p);
    if (estadoId != null && !estadoId.isBlank()) return repo.findByEstadoId(estadoId, p);
    return repo.findAll(p);
  }

  public List<Licencia> listarDisponibles(String juegoId, Pageable pageable) {
    return repo.findByJuegoIdAndEstadoId(juegoId, ESTADO_DISPONIBLE, pageable).getContent();
  }

  public Optional<Licencia> obtenerDisponible(String juegoId) {
    return repo.findFirstByJuegoIdAndEstadoIdOrderByFechaVencimientoAsc(juegoId, ESTADO_DISPONIBLE);
  }

  public Licencia obtener(String id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Licencia no encontrada: " + id));
  }

  public Licencia crear(Licencia l) {
    if (repo.existsById(l.getId())) throw new IllegalArgumentException("Ya existe licencia con id: " + l.getId());
    if (l.getEstadoId() == null || l.getEstadoId().isBlank()) {
      l.setEstadoId(ESTADO_DISPONIBLE);
    }
    return repo.save(l);
  }

  public Licencia actualizar(String id, Licencia data) {
    var l = obtener(id);
    l.setClave(data.getClave());
    l.setFechaVencimiento(data.getFechaVencimiento());
    l.setEstadoId(data.getEstadoId());
    l.setJuegoId(data.getJuegoId());
    l.setUsuarioId(data.getUsuarioId());
    l.setAsignadaEn(data.getAsignadaEn());
    return repo.save(l);
  }

  public void eliminar(String id) {
    if (!repo.existsById(id)) throw new IllegalArgumentException("Licencia no encontrada: " + id);
    repo.deleteById(id);
  }

  public Optional<Licencia> buscarPorClave(String clave) {
    return repo.findByClave(clave);
  }

  public Licencia asignar(String id, String usuarioId) {
    var licencia = obtener(id);
    if (!ESTADO_DISPONIBLE.equalsIgnoreCase(licencia.getEstadoId())) {
      throw new IllegalStateException("La licencia " + id + " no está disponible");
    }
    licencia.setEstadoId(ESTADO_ASIGNADA);
    licencia.setUsuarioId(usuarioId);
    licencia.setAsignadaEn(Instant.now());
    return repo.save(licencia);
  }

  public Licencia liberar(String id) {
    var licencia = obtener(id);
    licencia.setEstadoId(ESTADO_DISPONIBLE);
    licencia.setUsuarioId(null);
    licencia.setAsignadaEn(null);
    return repo.save(licencia);
  }
}


