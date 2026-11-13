package backend.circuits;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.geographie.Adresse;
import backend.geographie.Commune;
import backend.lieu.Lieu;
import backend.lieu.LieuBasilic;
import backend.lieu.TypeLieu;

import static org.junit.jupiter.api.Assertions.*;

class CircuitTest {

    private Circuit circuit;
    private Lieu lieuRennes;
    private Lieu lieuRennes2;
    private Lieu lieuNantes;

    // Lieu est abstraite : on teste via LieuBasilic
    private Lieu creerLieu(String id, String nom, Commune commune, double latitude, double longitude) {
        Adresse adresse = new Adresse("1 rue Test", "00000", commune, latitude, longitude);
        return new LieuBasilic(id, nom, null, TypeLieu.MUSEE, null, null, null, null, adresse);
    }

    @BeforeEach
    void setUp() {
        Commune rennes = new Commune("35238", "Rennes");
        Commune nantes = new Commune("44109", "Nantes");
        circuit = new Circuit("Balade", "Un petit circuit");
        lieuRennes = creerLieu("L1", "Musée des Beaux-Arts", rennes, 48.1100, -1.6750);
        lieuRennes2 = creerLieu("L2", "Parlement de Bretagne", rennes, 48.1130, -1.6790);
        lieuNantes = creerLieu("L3", "Château des ducs", nantes, 47.2160, -1.5500);
    }

    //----------------------Constructeur et getters-----------------------------

    @Test
    void testConstructeur_initialiseLesChamps() {
        assertEquals("Balade", circuit.getNom());
        assertEquals("Un petit circuit", circuit.getDescription());
        assertNotNull(circuit.getId());
        assertEquals(0, circuit.getNbEtapes());
        assertTrue(circuit.getEtapes().isEmpty());
        assertEquals(0.0, circuit.getDistanceKm());
        assertEquals(0, circuit.getDureeMinutes());
        assertNull(circuit.getCommuneDepart());
    }

    @Test
    void testConstructeur_nomNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Circuit(null, "desc"));
    }

    @Test
    void testConstructeur_nomVide_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Circuit("   ", "desc"));
    }

    @Test
    void testIdEtCodePartage_differentsEntreDeuxCircuits() {
        Circuit autre = new Circuit("Autre", null);
        assertNotEquals(circuit.getId(), autre.getId());
        assertNotEquals(circuit.getCodePartage(), autre.getCodePartage());
    }

    @Test
    void testCodePartage_8CaracteresAutorises() {
        String code = circuit.getCodePartage();
        assertEquals(8, code.length());
        // pas de caractères ambigus (I, O, 0, 1)
        assertTrue(code.matches("[ABCDEFGHJKLMNPQRSTUVWXYZ2-9]{8}"));
    }

    @Test
    void testSetNom_modifieLeNom() {
        circuit.setNom("Nouveau nom");
        assertEquals("Nouveau nom", circuit.getNom());
    }

    @Test
    void testSetNom_vide_leveExceptionEtGardeAncienNom() {
        assertThrows(IllegalArgumentException.class, () -> circuit.setNom(""));
        assertEquals("Balade", circuit.getNom());
    }

    @Test
    void testSetDescription() {
        circuit.setDescription("autre description");
        assertEquals("autre description", circuit.getDescription());
    }

    //----------------------ajouterEtape-----------------------------

    @Test
    void testAjouterEtape_ajouteALaFinAvecBonNumOrdre() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuRennes2, 45);

        List<EtapeCircuit> etapes = circuit.getEtapes();
        assertEquals(2, circuit.getNbEtapes());
        assertSame(lieuRennes, etapes.get(0).getLieu());
        assertEquals(1, etapes.get(0).getNumOrdre());
        assertEquals(30, etapes.get(0).getDureeVisiteMin());
        assertSame(lieuRennes2, etapes.get(1).getLieu());
        assertEquals(2, etapes.get(1).getNumOrdre());
    }

    @Test
    void testAjouterEtape_lieuNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> circuit.ajouterEtape(null, 30));
    }

    @Test
    void testAjouterEtape_lieuDejaPresent_leveException() {
        circuit.ajouterEtape(lieuRennes, 30);
        assertThrows(IllegalArgumentException.class, () -> circuit.ajouterEtape(lieuRennes, 10));
        assertEquals(1, circuit.getNbEtapes());
    }

    @Test
    void testAjouterEtape_autreObjetMemeId_leveException() {
        circuit.ajouterEtape(lieuRennes, 30);
        Lieu copie = creerLieu("L1", "Copie", new Commune("35238", "Rennes"), 48.0, -1.0);
        assertThrows(IllegalArgumentException.class, () -> circuit.ajouterEtape(copie, 10));
    }

    @Test
    void testAjouterEtape_dureeNegative_leveException() {
        assertThrows(IllegalArgumentException.class, () -> circuit.ajouterEtape(lieuRennes, -5));
        assertEquals(0, circuit.getNbEtapes());
    }

    @Test
    void testGetEtapes_listeNonModifiable() {
        circuit.ajouterEtape(lieuRennes, 30);
        List<EtapeCircuit> etapes = circuit.getEtapes();
        assertThrows(UnsupportedOperationException.class, () -> etapes.clear());
    }

    //----------------------retirerEtape-----------------------------

    @Test
    void testRetirerEtape_renumeroteLesEtapesSuivantes() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuRennes2, 30);
        circuit.ajouterEtape(lieuNantes, 30);

        circuit.retirerEtape(1);

        List<EtapeCircuit> etapes = circuit.getEtapes();
        assertEquals(2, circuit.getNbEtapes());
        assertSame(lieuRennes2, etapes.get(0).getLieu());
        assertEquals(1, etapes.get(0).getNumOrdre());
        assertSame(lieuNantes, etapes.get(1).getLieu());
        assertEquals(2, etapes.get(1).getNumOrdre());
    }

    @Test
    void testRetirerEtape_positionInvalide_leveException() {
        circuit.ajouterEtape(lieuRennes, 30);
        assertThrows(IndexOutOfBoundsException.class, () -> circuit.retirerEtape(0));
        assertThrows(IndexOutOfBoundsException.class, () -> circuit.retirerEtape(2));
    }

    @Test
    void testRetirerEtape_circuitVide_leveException() {
        assertThrows(IndexOutOfBoundsException.class, () -> circuit.retirerEtape(1));
    }

    //----------------------reordonner-----------------------------

    @Test
    void testReordonner_deplaceEtRenumerote() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuRennes2, 30);
        circuit.ajouterEtape(lieuNantes, 30);

        circuit.reordonner(3, 1); // Nantes passe en premier

        List<EtapeCircuit> etapes = circuit.getEtapes();
        assertSame(lieuNantes, etapes.get(0).getLieu());
        assertSame(lieuRennes, etapes.get(1).getLieu());
        assertSame(lieuRennes2, etapes.get(2).getLieu());
        for (int i = 0; i < etapes.size(); i++) {
            assertEquals(i + 1, etapes.get(i).getNumOrdre());
        }
    }

    @Test
    void testReordonner_memePosition_neChangeRien() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuRennes2, 30);

        circuit.reordonner(2, 2);

        assertSame(lieuRennes, circuit.getEtapes().get(0).getLieu());
        assertSame(lieuRennes2, circuit.getEtapes().get(1).getLieu());
    }

    @Test
    void testReordonner_positionInvalide_leveException() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuRennes2, 30);
        assertThrows(IndexOutOfBoundsException.class, () -> circuit.reordonner(1, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> circuit.reordonner(0, 1));
    }

    //----------------------recalculer (distance, durée, commune de départ)-----------------------------

    @Test
    void testRecalculer_uneEtape_distanceNulleEtDureeVisite() {
        circuit.ajouterEtape(lieuRennes, 40);
        assertEquals(0.0, circuit.getDistanceKm());
        assertEquals(40, circuit.getDureeMinutes());
    }

    @Test
    void testRecalculer_distanceEtDureeAvecTrajet() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuNantes, 60);

        double distanceAttendue = lieuRennes.getAdresse().distanceKm(lieuNantes.getAdresse());
        int trajetAttendu = (int) Math.round(distanceAttendue / Circuit.VITESSE_MARCHE_KMH * 60);

        assertEquals(distanceAttendue, circuit.getDistanceKm(), 0.001);
        assertTrue(circuit.getDistanceKm() > 90 && circuit.getDistanceKm() < 110); // Rennes-Nantes ≈ 100 km
        assertEquals(30 + 60 + trajetAttendu, circuit.getDureeMinutes());
    }

    @Test
    void testRecalculer_apresRetrait_metAJourDistance() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuNantes, 30);
        circuit.retirerEtape(2);
        assertEquals(0.0, circuit.getDistanceKm());
        assertEquals(30, circuit.getDureeMinutes());
    }

    @Test
    void testRecalculer_lieuSansAdresse_ignoreLaDistance() {
        Lieu sansAdresse = new LieuBasilic("L9", "Sans adresse", null, TypeLieu.MUSEE, null, null, null, null, null);
        circuit.ajouterEtape(lieuRennes, 10);
        circuit.ajouterEtape(sansAdresse, 20);
        assertEquals(0.0, circuit.getDistanceKm());
        assertEquals(30, circuit.getDureeMinutes());
    }

    @Test
    void testCommuneDepart_estCelleDeLaPremiereEtape() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuNantes, 30);
        assertEquals("Rennes", circuit.getCommuneDepart().getNom());

        circuit.reordonner(2, 1);
        assertEquals("Nantes", circuit.getCommuneDepart().getNom());
    }

    @Test
    void testCommuneDepart_nullQuandCircuitRedevientVide() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.retirerEtape(1);
        assertNull(circuit.getCommuneDepart());
    }

    //----------------------estValide / contientLieu-----------------------------

    @Test
    void testEstValide_fauxAvecMoinsDe2Etapes() {
        assertFalse(circuit.estValide());
        circuit.ajouterEtape(lieuRennes, 30);
        assertFalse(circuit.estValide());
    }

    @Test
    void testEstValide_vraiAPartirDe2Etapes() {
        circuit.ajouterEtape(lieuRennes, 30);
        circuit.ajouterEtape(lieuRennes2, 30);
        assertTrue(circuit.estValide());
    }

    @Test
    void testContientLieu() {
        circuit.ajouterEtape(lieuRennes, 30);
        assertTrue(circuit.contientLieu(lieuRennes));
        assertFalse(circuit.contientLieu(lieuNantes));
    }
}
