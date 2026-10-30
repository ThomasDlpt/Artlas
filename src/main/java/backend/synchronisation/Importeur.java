package backend.synchronisation;

import backend.lieux.Source;

public interface Importeur {
    public Source getSource();

    public ResultatImport importer();
}
