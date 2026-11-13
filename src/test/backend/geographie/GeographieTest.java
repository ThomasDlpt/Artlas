package backend.geographie;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GeographieTest {

    @Test
    void egaliteParCode() {
        assertEquals(new Region("53", "Bretagne"), new Region("53", "Bretagne"));
    }

    @Test
    void parametresInvalidesRefuses() {
        assertThrows(IllegalArgumentException.class, () -> new Region(null, "Bretagne"));
        assertThrows(IllegalArgumentException.class, () -> new Region("53", ""));
    }
}
