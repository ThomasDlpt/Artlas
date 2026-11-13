package backend.synchronisation;

import backend.accesdonnees.CommuneRepository;
import backend.accesdonnees.JournalSynchroRepository;
import backend.accesdonnees.LieuRepository;
import backend.geographie.Adresse;
import backend.geographie.Commune;
import backend.lieu.Lieu;
import backend.lieu.LieuBasilic;
import backend.lieu.Source;
import backend.lieu.TypeLieu;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SynchronisationTest {
    private Importeur importeurMock;
    private JournalSynchroRepository journalRepo;
    private LieuRepository lieuRepo;
    private SynchronisationService service;
    private List<Lieu> baseLieux;
    private JournalSynchro dernierJournal;
    @BeforeEach
    void setUp() {
        // On mocke l'INTERFACE Importeur (compatible Java 26 !)
        importeurMock = mock(Importeur.class);
        when(importeurMock.getSource()).thenReturn(Source.BASILIC);
        journalRepo = mock(JournalSynchroRepository.class, Answers.CALLS_REAL_METHODS);
        when(journalRepo.trouverDernier(any())).thenAnswer(inv -> dernierJournal);
        when(journalRepo.save(any(JournalSynchro.class))).thenAnswer(inv -> {
            dernierJournal = inv.getArgument(0);
            return dernierJournal;
        });
        baseLieux = new ArrayList<>();
        lieuRepo = mock(LieuRepository.class, Answers.CALLS_REAL_METHODS);
        when(lieuRepo.save(any(Lieu.class))).thenAnswer(inv -> {
            Lieu l = inv.getArgument(0);
            if (!baseLieux.contains(l)) baseLieux.add(l);
            return l;
        });
        when(lieuRepo.findById(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            return baseLieux.stream().filter(l -> id.equals(l.getId())).findFirst();
        });
        CommuneRepository communeRepo = mock(CommuneRepository.class);
        ImporteurDataTourisme vraiDT = new ImporteurDataTourisme("https://api.datatourisme.fr/v1", "cle", communeRepo);
        ImporteurBasilic vraiBasilic = new ImporteurBasilic("https://tabular-api.data.gouv.fr/api/resources", communeRepo);
        service = new SynchronisationService(vraiDT, vraiBasilic, journalRepo, lieuRepo);
    }
    private Lieu creerLieuTest(String id, String nom) {
        Adresse adr = new Adresse("1 rue Test", "35000", new Commune("35238", "Rennes"), 48.11, -1.67);
        return new LieuBasilic(id, nom, null, TypeLieu.MUSEE, null, null, List.of(), LocalDate.now(), adr);
    }
    @Test
    void premierImportSansErreurDonneStatutSucces() {
        Lieu l1 = creerLieuTest("L1", "Musée 1");
        Lieu l2 = creerLieuTest("L2", "Musée 2");
        when(importeurMock.importer()).thenReturn(
                new ResultatImport(Source.BASILIC, 2, List.of(), List.of(l1, l2))
        );
        JournalSynchro j = service.synchroniserSource(importeurMock);
        assertNotNull(j);
        assertEquals(Source.BASILIC, j.getSource());
        assertEquals(StatutSynchro.SUCCES, j.getStatut());
        assertEquals(2, j.getNbAjouts());
        assertEquals(0, j.getNbRejets());
        assertEquals(2, j.getNbLignesLues());
        assertNotNull(j.getId());
        assertNotNull(j.getDateSynchro());
        assertTrue(j.getErreurs().isEmpty());
    }
    @Test
    void synchroMoinsDUnMoisRenvoieNull() {
        dernierJournal = new JournalSynchro(
                "SYNC_1", Source.BASILIC, LocalDateTime.now().minusDays(5),
                10, 0, 10, StatutSynchro.SUCCES, List.of()
        );
        JournalSynchro j = service.synchroniserSource(importeurMock);
        assertNull(j);
    }
    @Test
    void synchroAvecErreursEtAjoutsDonneStatutPartiel() {
        Lieu l1 = creerLieuTest("L1", "Musée 1");
        when(importeurMock.importer()).thenReturn(
                new ResultatImport(Source.BASILIC, 2, List.of("Ligne 2 invalide"), List.of(l1))
        );
        JournalSynchro j = service.synchroniserSource(importeurMock);
        assertEquals(StatutSynchro.PARTIEL, j.getStatut());
        assertEquals(1, j.getNbAjouts());
        assertEquals(1, j.getNbRejets());
    }
    @Test
    void synchroQueDesErreursDonneStatutEchec() {
        when(importeurMock.importer()).thenReturn(
                new ResultatImport(Source.BASILIC, 1, List.of("Erreur critique"), List.of())
        );
        JournalSynchro j = service.synchroniserSource(importeurMock);
        assertEquals(StatutSynchro.ECHEC, j.getStatut());
        assertEquals(0, j.getNbAjouts());
        assertEquals(1, j.getNbRejets());
    }
    @Test
    void synchroMetAJourLieuDejaExistant() {
        Lieu existant = creerLieuTest("L1", "Ancien Nom");
        lieuRepo.enregistrerLieu(existant);
        Lieu nouveau = creerLieuTest("L1", "Nouveau Nom");
        when(importeurMock.importer()).thenReturn(
                new ResultatImport(Source.BASILIC, 1, List.of(), List.of(nouveau))
        );
        JournalSynchro j = service.synchroniserSource(importeurMock);
        assertEquals(StatutSynchro.SUCCES, j.getStatut());
        assertEquals(0, j.getNbAjouts());
        assertEquals("Nouveau Nom", existant.getNom());
    }
    @Test
    void convertirCategoriesBasilicEtDataTourisme() throws Exception {
        CommuneRepository communeRepo = mock(CommuneRepository.class);
        ImporteurBasilic impB = new ImporteurBasilic("https://tabular-api.data.gouv.fr/api/resources", communeRepo);
        assertEquals(Source.BASILIC, impB.getSource());
        Method mB = ImporteurBasilic.class.getDeclaredMethod("convertirCategories", String.class);
        mB.setAccessible(true);
        assertEquals(TypeLieu.MUSEE, mB.invoke(impB, "Musée"));
        assertEquals(TypeLieu.BIBLIOTHEQUE, mB.invoke(impB, "Bibliothèque"));
        assertEquals(TypeLieu.CINEMA, mB.invoke(impB, "Cinéma"));
        assertEquals(TypeLieu.LIEU_EXPOSITION, mB.invoke(impB, "Lieu d'exposition"));
        assertEquals(TypeLieu.MONUMENT, mB.invoke(impB, "Monument"));
        assertEquals(TypeLieu.THEATRE, mB.invoke(impB, "Théâtre"));
        assertEquals(TypeLieu.AUTRE, mB.invoke(impB, "Autre"));
        ImporteurDataTourisme impDT = new ImporteurDataTourisme("https://api.datatourisme.fr/v1", "cle", communeRepo);
        assertEquals(Source.DATATOURISME, impDT.getSource());
        Method mDT = ImporteurDataTourisme.class.getDeclaredMethod("convertirCategories", List.class);
        mDT.setAccessible(true);
        assertEquals(TypeLieu.MUSEE, mDT.invoke(impDT, List.of("Museum")));
        assertEquals(TypeLieu.THEATRE, mDT.invoke(impDT, List.of("Theater")));
        assertEquals(TypeLieu.CINEMA, mDT.invoke(impDT, List.of("Cinema")));
        assertEquals(TypeLieu.BIBLIOTHEQUE, mDT.invoke(impDT, List.of("Library")));
        assertEquals(TypeLieu.LIEU_EXPOSITION, mDT.invoke(impDT, List.of("ArtGalleryOrExhibitionGallery")));
        assertEquals(TypeLieu.MONUMENT, mDT.invoke(impDT, List.of("Autre")));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void injecterClientMock(Object importeur, String jsonTexte) throws Exception {
        JsonNode json = new ObjectMapper().readTree(jsonTexte);
        RestClient clientMock = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(clientMock.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(JsonNode.class)).thenReturn(json);
        Field champClient = importeur.getClass().getDeclaredField("client");
        champClient.setAccessible(true);
        champClient.set(importeur, clientMock);
    }
    @Test
    void importerBasilicCouvreTouteLaMethode() throws Exception {
        CommuneRepository communeRepo = mock(CommuneRepository.class, Answers.CALLS_REAL_METHODS);
        when(communeRepo.save(any(Commune.class))).thenAnswer(inv -> inv.getArgument(0));
        when(communeRepo.findById(anyString())).thenReturn( java.util.Optional.empty());
        ImporteurBasilic impB = new ImporteurBasilic("https://tabular-api.data.gouv.fr/api/resources", communeRepo);
        String jsonBasilic = """
                {
                  "data": [
                    {
                      "Identifiant origine": "PA001",
                      "Nom": "Musée de Bretagne",
                      "Type équipement ou lieu": "Musée",
                      "Label et appellation": "Musée de France",
                      "Adresse": "20 cours des Alliés",
                      "Code Postal": "35000",
                      "Latitude": 48.105,
                      "Longitude": -1.674,
                      "code_insee": "35238",
                      "libelle_geographique": "Rennes",
                      "N_Département": "35",
                      "Département": "Ille-et-Vilaine",
                      "N_Région": "53",
                      "Région": "Bretagne"
                    },
                    {
                      "Identifiant origine": ""
                    }
                  ],
                  "links": { "next": null }
                }
                """;
        injecterClientMock(impB, jsonBasilic);
        ResultatImport res = impB.importer();
        assertEquals(2, res.getNbLignesLues());
        assertEquals(1, res.getLieux().size());
        assertEquals(1, res.getErrors().size());
        assertEquals("Musée de Bretagne", res.getLieux().get(0).getNom());
    }
    @Test
    void importerDataTourismeEtSynchroniserComplet() throws Exception {
        CommuneRepository communeRepo = mock(CommuneRepository.class, Answers.CALLS_REAL_METHODS);
        when(communeRepo.save(any(Commune.class))).thenAnswer(inv -> inv.getArgument(0));
        when(communeRepo.findById(anyString())).thenReturn(java.util.Optional.empty());
        ImporteurDataTourisme impDT = new ImporteurDataTourisme("https://api.datatourisme.fr/v1", "cle", communeRepo);
        String jsonDT = """
                {
                  "objects": [
                    {
                      "label": { "@fr": "Parlement de Bretagne" },
                      "uri": "https://data.datatourisme.fr/1",
                      "hasContact": [{ "homepage": ["https://parlement.fr"] }],
                      "hasDescription": [{ "shortDescription": { "@fr": "Monument rennais" } }],
                      "isLocatedAt": [{
                        "geo": { "latitude": 48.112, "longitude": -1.678 },
                        "address": [{
                          "postalCode": "35000",
                          "streetAddress": ["Place du Parlement", "Entrée Sud"],
                          "hasAddressCity": { "label": { "@fr": "Rennes" }, "insee": "35238" }
                        }]
                      }],
                      "type": ["Museum", "PointOfInterest"]
                    },
                    {
                      "uuid": "err-1",
                      "label": { "@fr": "Lieu sans commune" }
                    }
                  ],
                  "meta": { "next": null }
                }
                """;
        injecterClientMock(impDT, jsonDT);
        ResultatImport res = impDT.importer();
        assertEquals(2, res.getNbLignesLues());
        assertEquals(1, res.getLieux().size());
        assertEquals(1, res.getErrors().size());
        // Test de synchroniser() complet avec les deux importeurs mockés au niveau HTTP
        ImporteurBasilic impB = new ImporteurBasilic("https://tabular-api.data.gouv.fr/api/resources", communeRepo);
        injecterClientMock(impB, "{ \"data\": [], \"links\": { \"next\": null } }");
        SynchronisationService syncService = new SynchronisationService(impDT, impB, journalRepo, lieuRepo);
        List<JournalSynchro> journaux = syncService.synchroniser();
        assertFalse(journaux.isEmpty());
    }
}