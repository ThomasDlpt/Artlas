package backend.synchronisation;

import backend.lieu.Source;
import backend.lieu.TypeLieu;
import backend.accesdonnees.CommuneRepository;
import backend.geographie.Adresse;
import backend.geographie.Commune;
import backend.lieu.Lieu;
import backend.lieu.LieuBasilic;
import backend.lieu.LieuDataTourisme;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
@Component 
public class ImporteurDataTourisme implements Importeur{
    
    private static final String REQUETE = "/placeOfInterest?review=LabelRating_MonumentHistorique&page_size=100";

    private RestClient client;

    private CommuneRepository repo;
    
    public ImporteurDataTourisme(
            @Value("${datatourisme.url}") String baseUrl,
            @Value("${datatourisme.api-key}") String apiKey,
            CommuneRepository repo) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)                        
                .defaultHeader("X-API-Key", apiKey)      
                .build();
        this.repo = repo;
    }

    @Override
    public Source getSource() {
        return Source.DATATOURISME;
    }

    @Override
    public ResultatImport importer() {

        String url = REQUETE;
        int lues = 0;
        List<String> erreurs = new ArrayList<>();
        List<Lieu> lieux = new ArrayList<>();
        
        while (url != null) {
            
            JsonNode res = client.get().uri(url).retrieve().body(JsonNode.class);

            for(JsonNode poi : res.path("objects")){
                lues++;
                try {
                    String nom = poi.path("label").path("@fr").asText();
                    String id_uri = poi.path("uri").asText();
                    String siteWeb = poi.path("hasContact").path(0).path("homepage").path(0).asText();
                    String descriptionCourte = poi.path("hasDescription").path(0).path("shortDescription").path("@fr").asText();
                    double latitude = poi.path("isLocatedAt").path(0).path("geo").path("latitude").asDouble();
                    double longitude = poi.path("isLocatedAt").path(0).path("geo").path("longitude").asDouble();
                    String codePostal = poi.path("isLocatedAt").path(0).path("address").path(0).path("postalCode").asText();
                    //String codeDep = codePostal.substring(0, 2);
                    String nomCommune = poi.path("isLocatedAt").path(0).path("address").path(0).path("hasAddressCity").path("label").path("@fr").asText();
                    String insee = poi.path("isLocatedAt").path(0).path("address").path(0).path("hasAddressCity").path("insee").asText();
                    //System.err.println(insee);
                    
                    JsonNode adresseRueLigne = poi.path("isLocatedAt").path(0).path("address").path(0).path("streetAddress");
                    String adresseRue = "";
                    //pour un meilleur affichage
                    int nbLignes = adresseRueLigne.size();
                    int counter = 0;
                    for(JsonNode ligne :  adresseRueLigne){
                        counter++;
                        if (counter == nbLignes) {
                            adresseRue += ligne.asText();
                        }
                        else{adresseRue += ligne.asText() + ", ";}
                        
                    }

                    List<String> types = new ArrayList<>();
                    for(JsonNode type : poi.path("type")){
                        types.add(type.asText());
                    }
                    TypeLieu typeLieu = convertirCategories(types);                    
                    
                    //sauvegarde dans le repo CommuneRepository
                    Commune c = repo.renvoieOuCreeCommune(insee, nomCommune);
                    //A COMPLETER POUR CREER UN LIEU
                    Lieu l = new LieuDataTourisme(id_uri, nom, descriptionCourte, typeLieu, siteWeb, null, types, LocalDate.now(), new Adresse(adresseRue, codePostal, c, latitude, longitude));
                    lieux.add(l);
                } catch (Exception e) {
                    e.printStackTrace();
                    erreurs.add(e.toString() + poi.path("uuid").asText("?"));
                }
            }
            JsonNode next = res.path("meta").path("next");
            if(next.isTextual()){
                url = next.asText();
            }
            else{
                url = null;
            }
        }
        return new ResultatImport(getSource(), lues, erreurs, lieux);
    }




    private TypeLieu convertirCategories(List<String> categories){
       Map<String, TypeLieu> m = new LinkedHashMap<>();
       m.put("Museum", TypeLieu.MUSEE);
       m.put("Theater", TypeLieu.THEATRE);
       m.put("Cinema", TypeLieu.CINEMA);
       m.put("Library", TypeLieu.BIBLIOTHEQUE);
       m.put("ArtGalleryOrExhibitionGallery", TypeLieu.LIEU_EXPOSITION);

       for(String key : m.keySet()){
            if(categories.contains(key)){
                return m.get(key);
            } 
       }

       return TypeLieu.MONUMENT;
    }

    public static void main(String[] args) throws IOException {
        /*Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("src/main/resources/application-secret.properties")) {
            p.load(in);
        }
        CommuneRepository repo = new CommuneRepository();
        ImporteurDataTourisme test = new ImporteurDataTourisme(
                "https://api.datatourisme.fr/v1",
                p.getProperty("datatourisme.api-key"), 
            repo);
        ResultatImport res = test.importer();
        System.err.println(res.getLieux().size());
        System.err.println(res.getSource());
        System.err.println(res.getNbLignesLues());
        System.err.println(res.getErrors().toString());
        System.err.println(repo.tailleRepo());
        /*System.out.println(test.convertirCategories(List.of("PointOfInterest", "CulturalSite")));
        System.out.println(test.convertirCategories(List.of("PointOfInterest", "CulturalSite")));  
        System.out.println(test.convertirCategories(List.of("Museum", "CulturalSite")));           
        System.out.println(test.convertirCategories(List.of("Cinematheque")));*/                     
    }
}


