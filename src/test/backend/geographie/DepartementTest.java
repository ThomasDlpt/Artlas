package backend.geographie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DepartementTest {

    private Region bretagne;

    @BeforeEach
    void setUp() {
        bretagne = new Region("53", "Bretagne");
    }


    @Test
    void testConstructeur_initialiseLesChamps() {
        Departement d = new Departement("35", "Ille-et-Vilaine", bretagne);
        assertEquals("35", d.getCode());
        assertEquals("Ille-et-Vilaine", d.getNom());
        assertSame(bretagne, d.getRegion());
    }

    @Test
    void testConstructeur_codeNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Departement(null, "Ille-et-Vilaine", bretagne));
    }

    @Test
    void testConstructeur_codeVide_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Departement("  ", "Ille-et-Vilaine", bretagne));
    }

    @Test
    void testConstructeur_nomNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Departement("35", null, bretagne));
    }

    @Test
    void testConstructeur_nomVide_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Departement("35", "  ", bretagne));
    }

    @Test
    void testConstructeur_regionNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Departement("35", "Ille-et-Vilaine", null));
    }


    @Test
    void testGetRegion_navigation() {
        Departement d = new Departement("35", "Ille-et-Vilaine", bretagne);
        assertEquals("53", d.getRegion().getCode());
        assertEquals("Bretagne", d.getRegion().getNom());
    }


    @Test
    void testEquals_memeCode_sontEgaux() {
        Departement d = new Departement("35", "Ille-et-Vilaine", bretagne);
        assertEquals(new Departement("35", "Ille-et-Vilaine", bretagne), d);
    }

    @Test
    void testEquals_memeCodeNomOuRegionDifferents_sontQuandMemeEgaux() {
        // equals() ne compare que le code (identifiant métier)
        Departement d = new Departement("35", "Ille-et-Vilaine", bretagne);
        Departement autre = new Departement("35", "Autre nom", new Region("75", "Nouvelle-Aquitaine"));
        assertEquals(autre, d);
    }

    @Test
    void testEquals_codeDifferent_nonEgaux() {
        Departement d = new Departement("35", "Ille-et-Vilaine", bretagne);
        assertNotEquals(new Departement("44", "Loire-Atlantique", bretagne), d);
    }

    @Test
    void testEquals_null_ouAutreType_renvoieFaux() {
        Departement d = new Departement("35", "Ille-et-Vilaine", bretagne);
        assertNotEquals(null, d);
        assertNotEquals("35", d);
    }

    @Test
    void testHashCode_coherentAvecEquals() {
        assertEquals(
                new Departement("35", "Ille-et-Vilaine", bretagne).hashCode(),
                new Departement("35", "Ille-et-Vilaine", bretagne).hashCode());
    }


    @Test
    void testToString() {
        Departement d = new Departement("35", "Ille-et-Vilaine", bretagne);
        assertEquals("Ille-et-Vilaine (35)", d.toString());
    }
}
