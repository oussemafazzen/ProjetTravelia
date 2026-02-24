package interfaces;

import models.Hebergement;
import java.sql.SQLException;
import java.util.List;

public interface IHebergementService {
    void ajouterHebergement(Hebergement h) throws SQLException;
    void modifierHebergement(Hebergement h) throws SQLException;
    void supprimerHebergement(Hebergement h) throws SQLException;
    void supprimerHebergementParId(int id) throws SQLException;
    List<Hebergement> recupTousHebergements() throws SQLException;
    Hebergement recupParIdHebergement(int id) throws SQLException;
}
