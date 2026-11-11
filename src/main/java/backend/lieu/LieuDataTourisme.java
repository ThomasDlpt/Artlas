package backend.lieu;

import backend.geographie.Adresse;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "lieu_datatourisme")
public class LieuDataTourisme extends Lieu {
    public LieuDataTourisme() {
        super();
    }

    public LieuDataTourisme(String id, String nom, String description, TypeLieu type, String siteWeb, String photo, List<String> labels, LocalDate dateMiseAJour, Adresse adresse) {
        super(id, nom, description, type, siteWeb, photo, labels, dateMiseAJour, adresse);
    }
} 
    

