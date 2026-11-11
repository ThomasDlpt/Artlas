package backend.lieu;

import backend.geographie.Adresse;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.List;

@Entity
public class LieuBasilic extends Lieu{
    public LieuBasilic() {
        super();
    }

    public LieuBasilic(String id, String nom, String description, TypeLieu type, String siteWeb, String photo, List<String> labels, LocalDate dateMiseAJour, Adresse adresse) {
        super(id, nom, description, type, siteWeb, photo, labels, dateMiseAJour, adresse);
    }
}
