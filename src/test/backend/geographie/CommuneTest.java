package backend.geographie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommuneTest {

    private Region bretagne;
    private Departement illeEtVilaine;

    @BeforeEach
    void setUp() {
        bretagne = new Region("53", "Bretagne");
        illeEtVilaine = new Departement("35", "Ille-et-Vilaine", bretagne);
    }


    @Test
    void testConstructeur_sansDepartement_departementEstNull() {
        Commune c = new Commune("35238", "Rennes");
        assertEquals("35238", c.getCodeInsee());
        assertEquals("Rennes", c.getNom());
        assertNull(c.getDepartement());
    }

    @Test
    void testConstructeur_avecDepartement() {
        Commune c = new Commune("35238", "Rennes", illeEtVilaine);
        assertSame(illeEtVilaine, c.getDepartement());
    }

    @Test
    void testConstructeur_codeInseeVide_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Commune("", "Rennes"));
    }

    @Test
    void testConstructeur_codeInseeNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Commune(null, "Rennes"));
    }

    @Test
    void testConstructeur_nomNull_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Commune("35238", null));
    }

    @Test
    void testConstructeur_nomVide_leveException() {
        assertThrows(IllegalArgumentException.class, () -> new Commune("35238", "   "));
    }


    @Test
    void testSetDepartement_rattacheUneCommuneCreeeSansDepartement() {
        Commune c = new Commune("79126", "Les Fosses");
        assertNull(c.getDepartement());
        Departement deuxSevres = new Departement("79", "Deux-Sèvres", new Region("75", "Nouvelle-Aquitaine"));
        c.setDepartement(deuxSevres);
        assertEquals("79", c.getDepartement().getCode());
    }

    @Test
    void testNavigationVersRegion() {
        Commune rennes = new Commune("35238", "Rennes", illeEtVilaine);
        assertEquals("35", rennes.getDepartement().getCode());
        assertEquals("53", rennes.getDepartement().getRegion().getCode());
        assertEquals("Bretagne", rennes.getDepartement().getRegion().getNom());
    }


    @Test
    void testEquals_memeCodeInsee_sontEgales() {
        Commune rennes = new Commune("35238", "Rennes");
        assertEquals(new Commune("35238", "Rennes"), rennes);
    }

    @Test
    void testEquals_memeCodeInseeNomDifferent_sontQuandMemeEgales() {
        // equals() ne compare que le code INSEE (identifiant métier)
        Commune rennes = new Commune("35238", "Rennes");
        assertEquals(new Commune("35238", "Autre nom"), rennes);
    }

    @Test
    void testEquals_codeInseeDifferent_nonEgales() {
        Commune rennes = new Commune("35238", "Rennes");
        assertNotEquals(new Commune("35047", "Bruz"), rennes);
    }

    @Test
    void testEquals_null_ouAutreType_renvoieFaux() {
        Commune rennes = new Commune("35238", "Rennes");
        assertNotEquals(null, rennes);
        assertNotEquals("35238", rennes);
    }

    @Test
    void testHashCode_coherentAvecEquals() {
        assertEquals(new Commune("35238", "Rennes").hashCode(), new Commune("35238", "Rennes").hashCode());
    }


    @Test
    void testToString() {
        Commune rennes = new Commune("35238", "Rennes");
        assertEquals("Rennes (35238)", rennes.toString());
    }
}
