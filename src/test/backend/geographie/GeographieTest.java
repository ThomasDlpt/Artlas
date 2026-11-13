package backend.geographie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeographieTest {

    private Region bretagne;
    private Departement illeEtVilaine;
    private Commune rennes;

    @BeforeEach
    void setUp() {
        bretagne = new Region("53", "Bretagne");
        illeEtVilaine = new Departement("35", "Ille-et-Vilaine", bretagne);
        rennes = new Commune("35238", "Rennes", illeEtVilaine);
    }

    // ---------- Hiérarchie Commune -> Departement -> Region ----------

    @Test
    void navigationCommuneVersRegion() {
        assertEquals("35", rennes.getDepartement().getCode());
        assertEquals("53", rennes.getDepartement().getRegion().getCode());
        assertEquals("Bretagne", rennes.getDepartement().getRegion().getNom());
    }

    @Test
    void communeSansDepartementPuisRattachement() {
        Commune c = new Commune("79126", "Les Fosses");
        assertNull(c.getDepartement());
        Departement deuxSevres = new Departement("79", "Deux-Sèvres", new Region("75", "Nouvelle-Aquitaine"));
        c.setDepartement(deuxSevres);
        assertEquals("79", c.getDepartement().getCode());
    }

    @Test
    void egaliteParCode() {
        assertEquals(new Region("53", "Bretagne"), bretagne);
        assertEquals(new Departement("35", "Ille-et-Vilaine", bretagne), illeEtVilaine);
        assertEquals(new Commune("35238", "Rennes"), rennes);
        assertEquals(new Commune("35238", "Rennes").hashCode(), rennes.hashCode());
        assertNotEquals(new Commune("35047", "Bruz"), rennes);
    }

    @Test
    void parametresInvalidesRefuses() {
        assertThrows(IllegalArgumentException.class, () -> new Region(null, "Bretagne"));
        assertThrows(IllegalArgumentException.class, () -> new Region("53", ""));
        assertThrows(IllegalArgumentException.class, () -> new Departement("35", "Ille-et-Vilaine", null));
        assertThrows(IllegalArgumentException.class, () -> new Commune("", "Rennes"));
        assertThrows(IllegalArgumentException.class, () -> new Commune("35238", null));
    }
}
