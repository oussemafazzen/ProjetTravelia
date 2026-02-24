package test;

import models.Client;
import models.enums.Role;
import models.enums.Statut;
import models.enums.NiveauFidelite;
import services.ClientService;
import java.sql.Date;
import java.sql.SQLException;
import models.Hebergement;
import services.HebergementService;

import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        // 1. Initialisation du service métier
        ClientService cs = new ClientService();

        System.out.println("--- DÉBUT DES TESTS DU CRUD ---");

        // 2. TEST : CREATE
        // Format Date : Année-Mois-Jour
        Date dateNaiss = Date.valueOf("1998-10-25");
        Client nouveau = new Client("salma@voyage.tn", "securePass123", Role.USER,
                Statut.ACTIF, "Sassi", "Salma", "55123456",
                "Tunisienne", dateNaiss);


        cs.add(nouveau);
        System.out.println("1. Ajout réussi (vérifie BCrypt dans phpMyAdmin)");
        HebergementService hs = new HebergementService();

        // 3. TEST : RETRIEVE
        List<Client> liste = cs.getAll();
        // On récupère le dernier client ajouté
        Client clientEnBD = liste.get(liste.size() - 1);
        System.out.println("2. Client récupéré : " + clientEnBD.getNom() + " (ID: " + clientEnBD.getId() + ")");
        // Test Add
        System.out.println("===== Test Add =====");
        hs.ajouterHebergement(new Hebergement("Hotel JERBA", "hotel", "10 Avenue ", "Jerba", "Tunisie", 80,
                "WiFi, Restaurant", 300.0));
        System.out.println(hs.recupTousHebergements());

        // 4. TEST : UPDATE
        clientEnBD.setTelephone("22998877");
        cs.update(clientEnBD);
        System.out.println("3. Modification du téléphone effectuée.");
        // Test GetAll
       // System.out.println("===== Test GetAll =====");
      //  List<Hebergement> liste = hs.getAll();
       // for (Hebergement h : liste) {
         //   System.out.println(h);
      //  }

        // 5. TEST MÉTIER AVANCÉ : SYSTÈME DE FIDÉLITÉ
        System.out.println("--- TEST LOGIQUE MÉTIER ---");
        // On ajoute 1500 points pour tester le passage automatique au niveau SILVER
        cs.addPoints(clientEnBD.getId(), 1500);
        // Test Update
       // System.out.println("===== Test Update =====");
      //  if (!liste.isEmpty()) {
        //    Hebergement hToUpdate = liste.get(0);
          //  hToUpdate.setNom("Hotel Tunis Deluxe");
            //hToUpdate.setTarifParNuit(200.0);
            //hs.update(hToUpdate);
         //   System.out.println("Apres modification:");
          //  System.out.println(hs.getAll());
        //}

        // On recharge le client pour voir l'évolution
        Client clientMaj = cs.getById(clientEnBD.getId());
        System.out.println("4. Nouveau Niveau : " + clientMaj.getNiveau_fidelite()); // Doit afficher SILVER
       // Test Delete
       // System.out.println("===== Test Delete =====");
       // List<Hebergement> listeApresUpdate = hs.getAll();
       // if (!listeApresUpdate.isEmpty()) {
         // Hebergement hToDelete = listeApresUpdate.get(0);
         //  hs.delete(hToDelete);
         //  System.out.println("Apres suppression:");
         //   System.out.println(hs.getAll());
      //  }

        // 6. TEST MÉTIER AVANCÉ : SÉCURITÉ
        System.out.println("5. Test de blocage manuel...");
        cs.blockClient(clientEnBD.getId());
        System.out.println("Statut après blocage : " + cs.getById(clientEnBD.getId()).getStatut());

        // 7. TEST : DELETE
        // cs.delete(clientEnBD.getId());
        // System.out.println("6. Client supprimé pour nettoyer la base.");

        System.out.println("--- FIN DES TESTS ---");
    }
}
