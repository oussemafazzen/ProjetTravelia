package interfaces;

import models.Activite;
import java.sql.SQLException;
import java.util.List;

public interface IActiviteService {
    void ajouterActivite(Activite a) throws SQLException;
    void modifierActivite(Activite a) throws SQLException;
    void supprimerActivite(Activite a) throws SQLException;
    void supprimerActiviteParId(int id) throws SQLException;
    List<Activite> recupToutesActivites() throws SQLException;
    Activite recupParIdActivite(int id) throws SQLException;
}
