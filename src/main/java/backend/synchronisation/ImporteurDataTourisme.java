package backend.synchronisation;

import backend.lieux.Source;
import backend.lieux.TypeLieu;
import backend.lieux.Lieu;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
@Component 
public class ImporteurDataTourisme implements Importeur{
    
    private static final String REQUETE = "/placeOfInterest?review=LabelRating_MonumentHistorique&page_size=100";

    private RestClient client;

    public ImporteurDataTourisme(
            @Value("${datatourisme.url}") String baseUrl,
            @Value("${datatourisme.api-key}") String apiKey) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)                        
                .defaultHeader("X-API-Key", apiKey)      
                .build();
    }

    @Override
    public Source getSource() {
        return Source.DATATOURISME;
    }

    @Override
    public ResultatImport importer() {
        JsonNode res = client.get().uri(REQUETE).retrieve().body(JsonNode.class);
       
        int size = res.path("objects").size();
        System.err.println(size);

        int lues = 0;
        List<String> erreurs = new ArrayList<>();
        List<Lieu> lieux = new ArrayList<>();
        
        
        for(JsonNode poi : res.path("objects")){
            lues++;
            try {
                String nom = poi.path("label").path("@fr").asText();
                System.err.println(nom);
                String id_uri = poi.path("uri").asText();
                System.err.println(id_uri);
                String siteWeb = poi.path("hasContact").path(0).path("homepage").path(0).asText();
                System.err.println(siteWeb);
                String descriptionCourte = poi.path("hasDescription").path(0).path("shortDescription").path("@fr").asText();
                
                System.err.println(descriptionCourte);
                List<String> types = new ArrayList<>();
                for(JsonNode type : poi.path("type")){
                    types.add(type.asText());
                }
                TypeLieu typeLieu = convertirCategories(types);
                System.err.println(typeLieu);

                //A COMPLETER POUR CREER UN LIEU
                Lieu l = new Lieu();
        
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        
        return new ResultatImport(getSource(), size, new ArrayList<>(), new ArrayList<>());
    }

    /*That’s the right plan. Your four steps map directly onto the class:
Read the docs and pick an endpoint. Done: /placeOfInterest.
Store the query in an attribute.
Fetch the JSON and loop over it.
Convert each entry to a Lieu, collect them in a list, and return new ResultatImport(...). */


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

    private Lieu convertir(JsonNode poi) {
        throw new UnsupportedOperationException("todo");
    }

    public static void main(String[] args) throws IOException {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("src/main/resources/application-secret.properties")) {
            p.load(in);
        }
        ImporteurDataTourisme test = new ImporteurDataTourisme(
                "https://api.datatourisme.fr/v1",
                p.getProperty("datatourisme.api-key"));
        test.importer();
        System.out.println(test.convertirCategories(List.of("PointOfInterest", "CulturalSite")));
        System.out.println(test.convertirCategories(List.of("PointOfInterest", "CulturalSite")));  
        System.out.println(test.convertirCategories(List.of("Museum", "CulturalSite")));           
        System.out.println(test.convertirCategories(List.of("Cinematheque")));                     
    }
}


