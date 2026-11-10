package backend.lieu;

import java.time.LocalDateTime;

public class SourceLieu
{
    private Source source;
    private String idExterne;
    private LocalDateTime dateImport;

    public SourceLieu(Source source, String idExterne, LocalDateTime dateImport){
        this.source =source;
        this.idExterne = idExterne;
        this.dateImport = dateImport;
    }

    public Source getSource(){
        return source;
    }
    public void setSource(Source source){
        this.source =source;
    }

    public String getIdExterne(){
        return idExterne;
    }

    public void setIdExterne(String idExterne){
        this.idExterne = idExterne;
    }

    public LocalDateTime getDateImport() {
        return dateImport;
    }

    public void setDateImport(LocalDateTime dateImport) {
        this.dateImport = dateImport;
    }
}