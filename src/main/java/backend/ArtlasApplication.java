package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du backend Spring Boot (API REST sur le port 8080, base PostgreSQL : voir docker-compose.yml).
 * L'analyse des composants part de ce package : repositories, services et contrôleurs des sous-packages sont trouvés seuls.
 */
@SpringBootApplication
public class ArtlasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtlasApplication.class, args);
    }
}
