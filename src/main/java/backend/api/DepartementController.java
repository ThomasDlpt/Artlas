package backend.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.consultation.DepartementService;

/**
 * API REST des départements (consommée par LieuApi.listerDepartements() du frontend).
 */
@RestController
@RequestMapping("/api")
public class DepartementController {
    private final DepartementService service;

    public DepartementController(DepartementService service) {
        this.service = service;
    }

    /** GET /api/departements : [{ "code": "35", "nom": "Ille-et-Vilaine" }, ...] triés par code, [] si aucun. */
    @GetMapping("/departements")
    public List<DepartementDto> lister() {
        return service.lister().stream().map(DepartementDto::de).toList();
    }
}
