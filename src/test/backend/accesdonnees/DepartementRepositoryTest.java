package backend.accesdonnees;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import backend.geographie.Departement;
import backend.geographie.Region;

/**
 * Test sur une vraie base H2 en mémoire : il vérifie notamment que la requête dérivée
 * findAllByOrderByCodeAsc() est bien comprise par Spring Data (sinon le contexte ne démarre pas).
 */
@DataJpaTest
class DepartementRepositoryTest {

    /**
     * Le projet n'a pas encore de classe @SpringBootApplication : ce test se donne sa propre configuration,
     * limitée à DepartementRepository et aux entités géographiques (les autres repositories ne sont pas concernés).
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Departement.class)
    @EnableJpaRepositories(
        basePackageClasses = DepartementRepository.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DepartementRepository.class))
    static class Configuration {
    }

    @Autowired
    private DepartementRepository repo;

    private final Region bretagne = new Region("53", "Bretagne");

    private void enregistrer(String code, String nom) {
        repo.save(new Departement(code, nom, bretagne));
    }

    // ---------- listerParCode ----------

    @Test
    void listerParCode_baseVide_renvoieListeVide() {
        assertTrue(repo.listerParCode().isEmpty());
    }

    @Test
    void listerParCode_trieParCode() {
        enregistrer("56", "Morbihan");
        enregistrer("22", "Côtes-d’Armor");
        enregistrer("35", "Ille-et-Vilaine");
        enregistrer("29", "Finistère");

        List<String> codes = repo.listerParCode().stream().map(Departement::getCode).toList();

        assertEquals(List.of("22", "29", "35", "56"), codes);
    }

    @Test
    void listerParCode_corseEnOrdreAlphabetique() {
        enregistrer("2A", "Corse-du-Sud");
        enregistrer("21", "Côte-d’Or");

        List<String> codes = repo.listerParCode().stream().map(Departement::getCode).toList();

        // tri sur une chaîne : "21" passe avant "2A" (le front ne dépend pas de cet ordre)
        assertEquals(List.of("21", "2A"), codes);
    }

    // ---------- trouverParCode ----------

    @Test
    void trouverParCode_codeConnu_renvoieLeDepartement() {
        enregistrer("35", "Ille-et-Vilaine");

        Departement d = repo.trouverParCode("35");

        assertNotNull(d);
        assertEquals("Ille-et-Vilaine", d.getNom());
    }

    @Test
    void trouverParCode_codeInconnu_renvoieNull() {
        assertNull(repo.trouverParCode("99"));
    }

    @Test
    void trouverParCode_codeNull_renvoieNull() {
        assertNull(repo.trouverParCode(null));
    }
}
