package backend.recherche;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import backend.lieu.TypeLieu;

public class RechercheTest {

    /// ////////////////////////////////////////// ///
    ///                 Bbox                       ///
    /// ////////////////////////////////////////// ///

    @Test
    void bboxGetters() {
        Bbox bbox = new Bbox(47.0, 49.0, -5.0, -1.0);
        assertEquals(47.0, bbox.getLatMin());
        assertEquals(49.0, bbox.getLatMax());
        assertEquals(-5.0, bbox.getLonMin());
        assertEquals(-1.0, bbox.getLonMax());
    }
    @Test
    void bboxContientPointInterieurEtBords() {
        Bbox bretagne = new Bbox(47.0, 49.0, -5.0, -1.0);
        assertTrue(bretagne.contient(48.1173, -1.6778));
        assertTrue(bretagne.contient(47.0, -5.0));
    }
    @Test
    void bboxNeContientPasPointExterieur() {
        Bbox bretagne = new Bbox(47.0, 49.0, -5.0, -1.0);
        assertFalse(bretagne.contient(48.8566, 2.3522));
        assertFalse(bretagne.contient(45.0, -2.0));
    }

    /// ////////////////////////////////////////// ///
    ///                 FiltreRecherche            ///
    /// ////////////////////////////////////////// ///

    @Test
    void filtreRechercheComplet() {
        List<TypeLieu> types = List.of(TypeLieu.MUSEE, TypeLieu.MONUMENT);
        FiltreRecherche filtre = new FiltreRecherche(types, "35", "Musée de France", "Beaux-Arts");
        assertEquals(types, filtre.getTypes());
        assertEquals("35", filtre.getCodeDepartment());
        assertEquals("Musée de France", filtre.getLabel());
        assertEquals("Beaux-Arts", filtre.getTexte());
    }
    @Test
    void filtreRechercheTypesNullCreeListeVide() {
        FiltreRecherche filtre = new FiltreRecherche(null, null, null, null);
        assertNotNull(filtre.getTypes());
        assertTrue(filtre.getTypes().isEmpty());
        assertNull(filtre.getCodeDepartment());
        assertNull(filtre.getLabel());
        assertNull(filtre.getTexte());
    }
}
