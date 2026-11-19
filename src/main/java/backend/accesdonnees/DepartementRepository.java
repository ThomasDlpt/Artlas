package backend.accesdonnees;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.geographie.Departement;

/**
 * Accès aux départements enregistrés.
 * La table ne contient que les départements rencontrés à l'import (champs N_Département / Département de Basilic) :
 * c'est la liste proposée par le frontend pour filtrer les lieux (GET /api/departements).
 */
@Repository
public interface DepartementRepository extends JpaRepository<Departement, String> {

    // requête dérivée par Spring Data : tri sur l'attribut "code"
    List<Departement> findAllByOrderByCodeAsc();

    /** Tous les départements connus, triés par code (tri alphabétique : "21" passe avant "2A"). */
    default List<Departement> listerParCode() {
        return findAllByOrderByCodeAsc();
    }

    /** Le département de ce code, ou null s'il est inconnu. */
    default Departement trouverParCode(String code) {
        if (code == null) {
            return null;
        }
        return findById(code).orElse(null);
    }
}
