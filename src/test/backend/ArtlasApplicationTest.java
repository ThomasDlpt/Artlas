package backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import backend.accesdonnees.CircuitRepository;
import backend.accesdonnees.CommuneRepository;
import backend.accesdonnees.DepartementRepository;
import backend.accesdonnees.LieuRepository;
import backend.circuits.Circuit;
import backend.circuits.EtapeCircuit;
import backend.geographie.Adresse;
import backend.geographie.Commune;
import backend.geographie.Departement;
import backend.geographie.Region;
import backend.lieu.LieuBasilic;
import backend.lieu.TypeLieu;
import backend.synchronisation.ImporteurBasilic;
import backend.synchronisation.ImporteurDataTourisme;

/**
 * Test de fumée de l'application complète : le contexte Spring démarre (entités, repositories, services,
 * contrôleurs), l'API répond, et un circuit survit à un aller-retour en base.
 * Base H2 en mémoire (jamais ./data) ; les importeurs sont simulés pour éviter tout appel réseau
 * (la synchronisation planifiée s'exécute au démarrage).
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:artlas-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ArtlasApplicationTest {

    @MockBean
    private ImporteurBasilic importeurBasilic;
    @MockBean
    private ImporteurDataTourisme importeurDataTourisme;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private CircuitRepository circuitRepo;
    @Autowired
    private LieuRepository lieuRepo;
    @Autowired
    private CommuneRepository communeRepo;
    @Autowired
    private DepartementRepository departementRepo;

    @AfterEach
    void nettoyer() {
        circuitRepo.deleteAll();
        lieuRepo.deleteAll();
        communeRepo.deleteAll();
        departementRepo.deleteAll();
    }

    @Test
    void contexteDemarre_etApiDepartementsRepond() throws Exception {
        mvc.perform(get("/api/departements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void circuit_survitAUnAllerRetourEnBase() {
        Commune rennes = communeRepo.renvoieOuCreeCommune("35238", "Rennes");
        LieuBasilic parlement = lieu("l1", "Parlement de Bretagne", rennes, 48.1137, -1.6836);
        LieuBasilic musee = lieu("l2", "Musée des Beaux-Arts", rennes, 48.1112, -1.6741);
        lieuRepo.save(parlement);
        lieuRepo.save(musee);

        Circuit circuit = new Circuit("Rennes, cœur historique", "Deux étapes");
        circuit.ajouterEtape(parlement, 30);
        circuit.ajouterEtape(musee, 45);
        assertTrue(circuitRepo.enregister(circuit));

        Circuit relu = circuitRepo.trouverParCodePartage(circuit.getCodePartage());

        assertNotNull(relu);
        assertEquals(circuit.getId(), relu.getId());
        assertEquals("Rennes, cœur historique", relu.getNom());
        assertEquals(circuit.getDistanceKm(), relu.getDistanceKm(), 1e-9);
        assertEquals(circuit.getDureeMinutes(), relu.getDureeMinutes());
        assertEquals("Rennes", relu.getCommuneDepart().getNom());
        // étapes dans l'ordre, avec leur lieu et leur durée de visite
        List<EtapeCircuit> etapes = relu.getEtapes();
        assertEquals(2, etapes.size());
        assertEquals(1, etapes.get(0).getNumOrdre());
        assertEquals("l1", etapes.get(0).getLieu().getId());
        assertEquals(30, etapes.get(0).getDureeVisiteMin());
        assertEquals(2, etapes.get(1).getNumOrdre());
        assertEquals("l2", etapes.get(1).getLieu().getId());
        assertEquals(45, etapes.get(1).getDureeVisiteMin());
        assertTrue(relu.estValide());
    }

    @Test
    void trouverParDepartement_compareLeCodeDuDepartement_pasLeCodePostal() {
        Region bretagne = new Region("53", "Bretagne");
        Commune rennes = communeRepo.save(new Commune("35238", "Rennes", new Departement("35", "Ille-et-Vilaine", bretagne)));
        Commune lannion = communeRepo.save(new Commune("22113", "Lannion", new Departement("22", "Côtes-d’Armor", bretagne)));
        Commune sansDepartement = communeRepo.renvoieOuCreeCommune("99999", "Inconnue");
        LieuBasilic r1 = lieuRepo.save(lieu("r1", "Parlement de Bretagne", rennes, 48.1137, -1.6836));
        LieuBasilic r2 = lieuRepo.save(lieu("r2", "Musée des Beaux-Arts", rennes, 48.1112, -1.6741));
        LieuBasilic l1 = lieuRepo.save(lieu("l1", "Chapelle de Lannion", lannion, 48.7326, -3.4600));
        LieuBasilic x1 = lieuRepo.save(lieu("x1", "Lieu sans département", sansDepartement, 47.0, -1.0));
        LieuBasilic sansAdresse = lieuRepo.save(new LieuBasilic("s1", "Lieu sans adresse", null, TypeLieu.AUTRE, null, null, null, LocalDate.now(), null));

        Circuit circuitRennes = new Circuit("Rennes", null);
        circuitRennes.ajouterEtape(r1, 30);
        circuitRennes.ajouterEtape(r2, 30);
        Circuit circuitLannion = new Circuit("Lannion", null);
        circuitLannion.ajouterEtape(l1, 30);
        circuitLannion.ajouterEtape(x1, 30);
        Circuit circuitIncomplet = new Circuit("Adresses incomplètes", null);
        circuitIncomplet.ajouterEtape(x1, 30);
        circuitIncomplet.ajouterEtape(sansAdresse, 30);
        circuitRepo.enregister(circuitRennes);
        circuitRepo.enregister(circuitLannion);
        circuitRepo.enregister(circuitIncomplet);

        // le code du département ("35") trouve le circuit de Rennes, dont les codes postaux sont "35000"
        assertEquals(List.of(circuitRennes.getId()), circuitRepo.trouverParDepartement("35").stream().map(Circuit::getId).toList());
        assertEquals(List.of(circuitLannion.getId()), circuitRepo.trouverParDepartement("22").stream().map(Circuit::getId).toList());
        assertTrue(circuitRepo.trouverParDepartement("56").isEmpty());
        // un code postal n'est pas un code de département
        assertTrue(circuitRepo.trouverParDepartement("35000").isEmpty());
    }

    @Test
    void supprimer_retireVraimentLeCircuitDeLaBase() {
        Commune rennes = communeRepo.renvoieOuCreeCommune("35238", "Rennes");
        LieuBasilic a = lieuRepo.save(lieu("a", "A", rennes, 48.11, -1.68));
        LieuBasilic b = lieuRepo.save(lieu("b", "B", rennes, 48.12, -1.67));
        Circuit circuit = new Circuit("À supprimer", null);
        circuit.ajouterEtape(a, 30);
        circuit.ajouterEtape(b, 30);
        circuitRepo.enregister(circuit);
        assertEquals(1, circuitRepo.count());

        assertTrue(circuitRepo.supprimer(circuit));

        assertEquals(0, circuitRepo.count());
        assertNull(circuitRepo.trouverParCodePartage(circuit.getCodePartage()));
        // déjà supprimé : rien à faire
        assertFalse(circuitRepo.supprimer(circuit));
    }

    private static LieuBasilic lieu(String id, String nom, Commune commune, double lat, double lon) {
        return new LieuBasilic(id, nom, null, TypeLieu.MONUMENT, null, null, null, LocalDate.now(),
            new Adresse(null, "35000", commune, lat, lon));
    }
}
