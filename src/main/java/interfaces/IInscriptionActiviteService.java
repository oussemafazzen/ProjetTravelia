package interfaces;

import models.InscriptionActivite;
import java.sql.SQLException;
import java.util.List;

public interface IInscriptionActiviteService {
    void ajouterInscription(InscriptionActivite i) throws SQLException;
    void modifierInscription(InscriptionActivite i) throws SQLException;
    void supprimerInscription(InscriptionActivite i) throws SQLException;
    void supprimerInscriptionParId(int id) throws SQLException;
    List<InscriptionActivite> recupToutesInscriptions() throws SQLException;
    InscriptionActivite recupParIdInscription(int id) throws SQLException;
}
