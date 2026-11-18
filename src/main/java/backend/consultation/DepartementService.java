package backend.consultation;

import java.util.List;

import org.springframework.stereotype.Service;

import backend.accesdonnees.DepartementRepository;
import backend.geographie.Departement;

/**
 * Consultation des départements : ceux des lieux importés (Basilic), proposés par le frontend pour filtrer les lieux.
 */
@Service
public class DepartementService {
    private final DepartementRepository repo;

    public DepartementService(DepartementRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("repository null");
        }
        this.repo = repo;
    }

    /** Tous les départements connus, triés par code. Liste vide si rien n'a encore été importé. */
    public List<Departement> lister() {
        return repo.listerParCode();
    }
}
