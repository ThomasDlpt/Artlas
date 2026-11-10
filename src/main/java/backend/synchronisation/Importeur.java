package backend.synchronisation;

import backend.lieu.Source;

public interface Importeur {
    public Source getSource();

    public ResultatImport importer();
}
