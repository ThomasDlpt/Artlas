package backend.consultation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.accesdonnees.DepartementRepository;
import backend.geographie.Departement;
import backend.geographie.Region;

@ExtendWith(MockitoExtension.class)
class DepartementServiceTest {

    @Mock
    private DepartementRepository repo;

    private DepartementService service;

    @BeforeEach
    void setUp() {
        service = new DepartementService(repo);
    }

    //----------------------Constructeur-----------------------------
    @Test
    void constructeur_repoNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new DepartementService(null));
    }

    //----------------------lister-----------------------------
    @Test
    void lister_delegueAuRepository() {
        Region bretagne = new Region("53", "Bretagne");
        List<Departement> attendus = List.of(
            new Departement("22", "Côtes-d’Armor", bretagne),
            new Departement("35", "Ille-et-Vilaine", bretagne));
        when(repo.listerParCode()).thenReturn(attendus);

        List<Departement> resultat = service.lister();

        assertSame(attendus, resultat);
        verify(repo).listerParCode();
    }

    @Test
    void lister_rienImporte_renvoieListeVide() {
        when(repo.listerParCode()).thenReturn(List.of());

        assertTrue(service.lister().isEmpty());
    }
}
