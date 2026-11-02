package backend.synchronisation;

import backend.lieux.Source;
import backend.lieux.TypeLieu;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import backend.lieux.Lieu;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImporteurBasilic implements Importeur{

    private static final String REQUETE = "/dced78ee-0823-4b61-86e6-57717308d4e4/data/?page_size=100";

    private final RestClient client;

    public ImporteurBasilic(@Value("${basilic.url}") String url) {
        this.client = RestClient.create(url);
    }

    @Override
    public Source getSource() {
        return Source.BASILIC;
    }

    @Override
    public ResultatImport importer() {
        JsonNode res = client.get().uri(REQUETE).retrieve().body(JsonNode.class).path("data");
        List<String> erreurs = new ArrayList<>();
        List<Lieu> lieux = new ArrayList<>();

        for (JsonNode obj : res) {
            try{
                String nom = obj.path("Nom").asText();
                String id = obj.path("Identifiant origine").asText();
                String adresse = obj.path("Adresse postale").asText();
                String departement = obj.path("Département").asText();
                String region = obj.path("Région").asText();
                String latitude = obj.path("Latitude").asText();
                String longitude = obj.path("Longitude").asText();
                String desc = obj.path("desc").asText();
                String webSite = obj.path("Website").asText();
                TypeLieu type = convertirCategories(obj.path("Type équipement ou lieu").asText());
                //System.out.println(res.get(0));
                System.out.println(nom + " | " + type + " | " + id + " | " + adresse + " | " + departement + " | " + region + " | " + latitude + " | " + longitude);
                lieux.add(new Lieu());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new ResultatImport(getSource(), res.size(), erreurs, lieux);
    }

    private TypeLieu convertirCategories(String types){
        return switch (types){
            case "Musée" -> TypeLieu.MUSEE;
            case "Bibliothèque" -> TypeLieu.BIBLIOTHEQUE;
            case "Cinéma" -> TypeLieu.CINEMA;
            case "Exposition" -> TypeLieu.LIEU_EXPOSITION;
            case "Monument" -> TypeLieu.MONUMENT;
            case "Théâtre" -> TypeLieu.THEATRE;
            default -> TypeLieu.AUTRE;
        };
    }

    public static void main(String[] args) {
        ImporteurBasilic test = new ImporteurBasilic("https://tabular-api.data.gouv.fr/api/resources");
        ResultatImport res = test.importer();
        System.out.println("Lignes lues : " + res.getNbLignesLues() + ", Lieux : " + res.getLieux().size());
    }
}
