package backend.accesdonnees;

import backend.geographie.Adresse;
import backend.geographie.Commune;
import backend.geographie.Departement;
import backend.geographie.Region;
import backend.circuits.Circuit;
import backend.lieu.Lieu;
import backend.lieu.LieuBasilic;
import backend.lieu.TypeLieu;
import backend.lieu.Source;
import backend.recherche.Bbox;
import backend.recherche.FiltreRecherche;
import backend.synchronisation.JournalSynchro;
import backend.synchronisation.StatutSynchro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;

public class AccesDonneesTest {

    private LieuRepository lieuRepo;
    private CommuneRepository communeRepo;
    private List<Lieu> baseLieux;
    private List<Commune> baseCommunes;
    private Lieu museeRennes;
    private Lieu monumentRennes;
    private Lieu museeParis;

    @BeforeEach
    void setUp() {
        baseLieux = new ArrayList<>();
        lieuRepo = mock(LieuRepository.class, Answers.CALLS_REAL_METHODS);
        when(lieuRepo.findAll()).thenReturn(baseLieux);
        when(lieuRepo.save(any(Lieu.class))).thenAnswer(inv -> {
            Lieu l = inv.getArgument(0);
            baseLieux.add(l);
            return l;
        });
        when(lieuRepo.findById(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            return baseLieux.stream().filter(l -> id.equals(l.getId())).findFirst();
        });
        baseCommunes = new ArrayList<>();
        communeRepo = mock(CommuneRepository.class, Answers.CALLS_REAL_METHODS);
        when(communeRepo.findAll()).thenReturn(baseCommunes);
        when(communeRepo.save(any(Commune.class))).thenAnswer(inv -> {
            Commune c = inv.getArgument(0);
            baseCommunes.add(c);
            return c;
        });
        when(communeRepo.findById(anyString())).thenAnswer(inv -> {
            String code = inv.getArgument(0);
            return baseCommunes.stream().filter(c -> code.equals(c.getCodeInsee())).findFirst();
        });
        when(communeRepo.count()).thenAnswer(inv -> (long) baseCommunes.size());
        Region bretagne = new Region("53", "Bretagne");
        Departement dept35 = new Departement("35", "Ille-et-Vilaine", bretagne);
        Commune rennes = new Commune("35238", "Rennes", dept35);
        Region idf = new Region("11", "Île-de-France");
        Departement dept75 = new Departement("75", "Paris", idf);
        Commune paris = new Commune("75056", "Paris", dept75);
        museeRennes = new LieuBasilic("L1", "Musée des Beaux-Arts", null, TypeLieu.MUSEE,
                null, null, List.of("Musée de France"), LocalDate.now(),
                new Adresse("20 quai Émile Zola", "35000", rennes, 48.1097, -1.6744));
        monumentRennes = new LieuBasilic("L2", "Parlement de Bretagne", null, TypeLieu.MONUMENT,
                null, null, List.of("Monument historique"), LocalDate.now(),
                new Adresse("Place du Parlement", "35000", rennes, 48.1124, -1.6781));
        museeParis = new LieuBasilic("L3", "Musée du Louvre", null, TypeLieu.MUSEE,
                null, null, List.of("Musée de France"), LocalDate.now(),
                new Adresse("Rue de Rivoli", "75001", paris, 48.8606, 2.3376));
    }

    /// ////////////////////////////////////////// ///
    ///                LieuRepository              ///
    /// ////////////////////////////////////////// ///

    @Test
    void enregistrerEtTrouverParId() {
        assertTrue(lieuRepo.enregistrerLieu(museeRennes));
        assertEquals(museeRennes, lieuRepo.trouverParId("L1"));
        // Doublon d'ID ou lieu null refusé
        assertFalse(lieuRepo.enregistrerLieu(museeRennes));
        assertFalse(lieuRepo.enregistrerLieu(null));
        assertNull(lieuRepo.trouverParId("INCONNU"));
    }
    @Test
    void trouverParTypeEtParDepartement() {
        lieuRepo.enregistrerLieu(museeRennes);
        lieuRepo.enregistrerLieu(monumentRennes);
        lieuRepo.enregistrerLieu(museeParis);
        assertEquals(2, lieuRepo.trouverParType(TypeLieu.MUSEE).size());
        assertEquals(1, lieuRepo.trouverParType(TypeLieu.MONUMENT).size());
        assertEquals(2, lieuRepo.trouverParDepartement("35").size());
        assertEquals(1, lieuRepo.trouverParDepartement("75").size());
    }
    @Test
    void trouverParFiltreMulticriteres() {
        lieuRepo.enregistrerLieu(museeRennes);
        lieuRepo.enregistrerLieu(monumentRennes);
        lieuRepo.enregistrerLieu(museeParis);
        FiltreRecherche filtre = new FiltreRecherche(List.of(TypeLieu.MUSEE), "35", "Musée de France", "beaux");
        List<Lieu> res = lieuRepo.trouverParFiltre(filtre);
        assertEquals(1, res.size());
        assertEquals(museeRennes, res.get(0));
    }
    @Test
    void trouverDansZoneEtTrouverProche() {
        lieuRepo.enregistrerLieu(museeRennes);
        lieuRepo.enregistrerLieu(monumentRennes);
        lieuRepo.enregistrerLieu(museeParis);
        Bbox zoneRennes = new Bbox(48.0, 48.2, -1.8, -1.5);
        assertEquals(2, lieuRepo.trouverDansZone(zoneRennes, null).size());
        // Dans un rayon de 2 km autour du centre de Rennes
        List<Lieu> proches = lieuRepo.trouverProche(48.11, -1.67, 2.0, null);
        assertEquals(2, proches.size());
    }

    /// ///////////////////////////////////////////// ///
    ///                CommuneRepository              ///
    /// ///////////////////////////////////////////// ///

    @Test
    void communeRepositoryRenvoieOuCree() {
        Commune c1 = communeRepo.renvoieOuCreeCommune("35238", "Rennes");
        Commune c2 = communeRepo.renvoieOuCreeCommune("35238", "Rennes");
        assertSame(c1, c2);
        assertEquals(1, communeRepo.tailleRepo());
        assertEquals(c1, communeRepo.trouverParCodePostalEtNom("35000", "Rennes"));
        assertThrows(IllegalArgumentException.class, () -> communeRepo.renvoieOuCreeCommune("", "Rennes"));
        assertThrows(IllegalArgumentException.class, () -> communeRepo.renvoieOuCreeCommune("35238", null));
    }

    /// //////////////////////////////////////////////////// ///
    ///                JournalSynchroRepository              ///
    /// //////////////////////////////////////////////////// ///

    @Test
    void journalSynchroRepositoryEnregistrerEtTrouverDernier() {
        JournalSynchroRepository jRepo = mock(JournalSynchroRepository.class, Answers.CALLS_REAL_METHODS);
        JournalSynchro j = new JournalSynchro("ID1", Source.BASILIC, LocalDateTime.now(), 5, 0, 5, StatutSynchro.SUCCES, List.of());
        when(jRepo.findFirstBySourceOrderByDateSynchroDesc(Source.BASILIC)).thenReturn(j);
        jRepo.enregistrer(j);
        jRepo.enregistrer(null);
        assertEquals(j, jRepo.trouverDernier(Source.BASILIC));
    }

    /// ///////////////////////////////////////////// ///
    ///                CircuitRepository              ///
    /// ///////////////////////////////////////////// ///

    @Test
    void circuitRepositoryComplet() {
        List<Circuit> baseCircuits = new ArrayList<>();
        CircuitRepository circuitRepo = mock(CircuitRepository.class, Answers.CALLS_REAL_METHODS);
        when(circuitRepo.findAll()).thenReturn(baseCircuits);
        when(circuitRepo.save(any(Circuit.class))).thenAnswer(inv -> {
            Circuit c = inv.getArgument(0);
            baseCircuits.add(c);
            return c;
        });
        Circuit c = new Circuit("Circuit Rennes", "Visite du centre");
        c.ajouterEtape(museeRennes, 60);
        c.ajouterEtape(monumentRennes, 45);
        assertTrue(circuitRepo.enregister(c));
        assertFalse(circuitRepo.enregister(c)); // Déjà enregistré
        assertFalse(circuitRepo.enregister(null));
        assertEquals(c, circuitRepo.trouverParCodePartage(c.getCodePartage()));
        assertNull(circuitRepo.trouverParCodePartage("INCONNU"));
        assertThrows(IllegalArgumentException.class, () -> circuitRepo.trouverParCodePartage(""));
        assertEquals(1, circuitRepo.trouverParDepartement("35000").size());
        assertThrows(IllegalArgumentException.class, () -> circuitRepo.trouverParDepartement(null));
        assertTrue(circuitRepo.supprimer(c));
        assertFalse(circuitRepo.supprimer(null));
    }
}
