package backend.synchronisation;

import backend.lieux.Source;
import backend.lieux.TypeLieu;
import org.springframework.stereotype.Component;

@Component
public class ImporteurBasilic implements Importeur{

    @Override
    public Source getSource() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSource'");
    }

    @Override
    public ResultatImport importer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSource'");
    }

    private TypeLieu convertirCategories(String categories){
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'importer'");
    }
    
}
