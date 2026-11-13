package backend.circuits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.lieu.Lieu;
import backend.lieu.LieuBasilic;
import backend.lieu.TypeLieu;

import static org.junit.jupiter.api.Assertions.*;

class EtapeCircuitTest {

    private Lieu lieu;

    @BeforeEach
    void setUp() {
        // Lieu est abstraite : on teste via LieuBasilic
        lieu = new LieuBasilic("L1", "Musée des Beaux-Arts", null, TypeLieu.MUSEE, null, null, null, null, null);
    }

    //----------------------Constructeur et getters-----------------------------

    @Test
    void testConstructeur_initialiseLesChamps() {
        EtapeCircuit etape = new EtapeCircuit(lieu, 1, 30);
        assertSame(lieu, etape.getLieu());
        assertEquals(1, etape.getNumOrdre());
        assertEquals(30, etape.getDureeVisiteMin());
    }

    @Test
    void testConstructeur_lieuNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new EtapeCircuit(null, 1, 30));
    }

    @Test
    void testConstructeur_numOrdreZero_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new EtapeCircuit(lieu, 0, 30));
    }

    @Test
    void testConstructeur_numOrdreNegatif_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new EtapeCircuit(lieu, -1, 30));
    }

    @Test
    void testConstructeur_dureeVisiteNegative_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new EtapeCircuit(lieu, 1, -5));
    }

    @Test
    void testConstructeur_dureeVisiteNulle_estAcceptee() {
        EtapeCircuit etape = new EtapeCircuit(lieu, 1, 0);
        assertEquals(0, etape.getDureeVisiteMin());
    }

    //----------------------setNumOrdre (package-private)-----------------------------

    @Test
    void testSetNumOrdre_modifieLeNumOrdre() {
        EtapeCircuit etape = new EtapeCircuit(lieu, 1, 30);
        etape.setNumOrdre(3);
        assertEquals(3, etape.getNumOrdre());
    }

    @Test
    void testSetNumOrdre_neModifiePasLesAutresChamps() {
        EtapeCircuit etape = new EtapeCircuit(lieu, 1, 30);
        etape.setNumOrdre(2);
        assertSame(lieu, etape.getLieu());
        assertEquals(30, etape.getDureeVisiteMin());
    }
}
