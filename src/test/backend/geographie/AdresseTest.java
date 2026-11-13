package backend.geographie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdresseTest {

    private Commune rennes;

    @BeforeEach
    void setUp() {
        rennes = new Commune("35238", "Rennes");
    }


    @Test
    void testConstructeur_initialiseLesChamps() {
        Adresse a = new Adresse("20 quai Émile Zola", "35000", rennes, 48.1097, -1.6744);
        assertEquals("20 quai Émile Zola", a.getRue());
        assertEquals("35000", a.getCodePostal());
        assertSame(rennes, a.getCommune());
        assertEquals(48.1097, a.getLatitude());
        assertEquals(-1.6744, a.getLongitude());
    }

    @Test
    void testConstructeur_rueNull_estAutorisee() {
        Adresse a = new Adresse(null, "35000", rennes, 48.11, -1.67);
        assertNull(a.getRue());
    }

    @Test
    void testConstructeur_communeNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", null, 48.1, -1.6));
    }

    @Test
    void testConstructeur_latitudeHorsBornes_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, 90.1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, -90.1, 0));
    }

    @Test
    void testConstructeur_latitudeBornes_sontAcceptees() {
        assertDoesNotThrow(() -> new Adresse("r", "35000", rennes, 90, 0));
        assertDoesNotThrow(() -> new Adresse("r", "35000", rennes, -90, 0));
    }

    @Test
    void testConstructeur_longitudeHorsBornes_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, 0, 180.1));
        assertThrows(IllegalArgumentException.class, () -> new Adresse("r", "35000", rennes, 0, -180.1));
    }

    @Test
    void testConstructeur_longitudeBornes_sontAcceptees() {
        assertDoesNotThrow(() -> new Adresse("r", "35000", rennes, 0, 180));
        assertDoesNotThrow(() -> new Adresse("r", "35000", rennes, 0, -180));
    }


    @Test
    void testDistanceKm_versSoiMeme_estNulle() {
        Adresse a = new Adresse("r", "35000", rennes, 48.1124, -1.6781);
        assertEquals(0.0, a.distanceKm(a), 1e-9);
    }

    @Test
    void testDistanceKm_estSymetrique() {
        Adresse parlement = new Adresse("Place du Parlement de Bretagne", "35000", rennes, 48.1124, -1.6781);
        Adresse musee = new Adresse("20 quai Émile Zola", "35000", rennes, 48.1097, -1.6744);
        assertEquals(parlement.distanceKm(musee), musee.distanceKm(parlement), 1e-9);
    }

    @Test
    void testDistanceKm_entreDeuxAdressesProches() {
        Adresse parlement = new Adresse("Place du Parlement de Bretagne", "35000", rennes, 48.1124, -1.6781);
        Adresse musee = new Adresse("20 quai Émile Zola", "35000", rennes, 48.1097, -1.6744);
        assertEquals(0.40, parlement.distanceKm(musee), 0.02); // ~400 m entre le Parlement et le Musée des Beaux-Arts
    }

    @Test
    void testDistanceKm_rennesParis_valeurApproximative() {
        Adresse a = new Adresse(null, "35000", rennes, 48.1173, -1.6778);
        Commune paris = new Commune("75056", "Paris");
        Adresse b = new Adresse(null, "75001", paris, 48.8566, 2.3522);
        assertEquals(308, a.distanceKm(b), 5);
    }

    @Test
    void testDistanceKm_versNull_leveException() {
        Adresse a = new Adresse("r", "35000", rennes, 48.1, -1.6);
        assertThrows(IllegalArgumentException.class, () -> a.distanceKm(null));
    }


    @Test
    void testToString_avecRueEtCodePostal() {
        Adresse a = new Adresse("20 quai Émile Zola", "35000", rennes, 48.1097, -1.6744);
        assertEquals("20 quai Émile Zola, 35000 Rennes", a.toString());
    }

    @Test
    void testToString_sansRue() {
        Adresse a = new Adresse(null, "35000", rennes, 48.11, -1.67);
        assertEquals("35000 Rennes", a.toString());
    }
}
