package backend.api;

import backend.geographie.Departement;

/**
 * Département tel que le frontend le reçoit : { "code": "35", "nom": "Ille-et-Vilaine" }.
 * On n'envoie pas l'entité : elle embarque aussi sa région, dont le frontend n'a pas besoin ici.
 */
public record DepartementDto(String code, String nom) {

    public static DepartementDto de(Departement departement) {
        return new DepartementDto(departement.getCode(), departement.getNom());
    }
}
