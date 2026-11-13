package backend.geographie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeographieTest {

    private Region bretagne;
    private Departement illeEtVilaine;

    @BeforeEach
    void setUp() {
        bretagne = new Region("53", "Bretagne");
        illeEtVilaine = new Departement("35", "Ille-et-Vilaine", bretagne);
    }

    // ---------- Departement -> Region ----------

    @Test
    void departementVersRegion() {
        assertEquals("53", illeEtVilaine.getRegion().getCode());
        assertEquals("Bretagne", illeEtVilaine.getRegion().getNom());
    }

    @Test
    void egaliteParCode() {
        assertEquals(new Region("53", "Bretagne"), bretagne);
        assertEquals(new Departement("35", "Ille-et-Vilaine", bretagne), illeEtVilaine);
    }

    @Test
    void parametresInvalidesRefuses() {
        assertThrows(IllegalArgumentException.class, () -> new Region(null, "Bretagne"));
        assertThrows(IllegalArgumentException.class, () -> new Region("53", ""));
        assertThrows(IllegalArgumentException.class, () -> new Departement("35", "Ille-et-Vilaine", null));
    }
}
