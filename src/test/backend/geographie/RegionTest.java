package backend.geographie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RegionTest {

    //----------------------Constructeur et getters-----------------------------

    @Test
    void testConstructeur_initialiseLesChamps() {
        Region r = new Region("53", "Bretagne");
        assertEquals("53", r.getCode());
        assertEquals("Bretagne", r.getNom());
    }

    @Test
    void testConstructeur_codeNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Region(null, "Bretagne"));
    }

    @Test
    void testConstructeur_codeVide_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Region("  ", "Bretagne"));
    }

    @Test
    void testConstructeur_nomNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Region("53", null));
    }

    @Test
    void testConstructeur_nomVide_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Region("53", "  "));
    }

    //----------------------equals / hashCode-----------------------------

    @Test
    void testEquals_memeCode_sontEgales() {
        assertEquals(new Region("53", "Bretagne"), new Region("53", "Bretagne"));
    }

    @Test
    void testEquals_memeCodeNomDifferent_sontQuandMemeEgales() {
        // equals() ne compare que le code (identifiant métier)
        assertEquals(new Region("53", "Autre nom"), new Region("53", "Bretagne"));
    }

    @Test
    void testEquals_codeDifferent_nonEgales() {
        assertNotEquals(new Region("75", "Nouvelle-Aquitaine"), new Region("53", "Bretagne"));
    }

    @Test
    void testEquals_null_ouAutreType_renvoieFaux() {
        Region bretagne = new Region("53", "Bretagne");
        assertNotEquals(null, bretagne);
        assertNotEquals("53", bretagne);
    }

    @Test
    void testHashCode_coherentAvecEquals() {
        assertEquals(new Region("53", "Bretagne").hashCode(), new Region("53", "Bretagne").hashCode());
    }

    //----------------------toString-----------------------------

    @Test
    void testToString() {
        Region bretagne = new Region("53", "Bretagne");
        assertEquals("Bretagne (53)", bretagne.toString());
    }
}
