package test;

import models.Hebergement;
import services.HebergementService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        HebergementService hs = new HebergementService();

        // Test Add
        System.out.println("===== Test Add =====");
        hs.add(new Hebergement("Hotel JERBA", "hotel", "10 Avenue ", "Jerba", "Tunisie", 80,
                "WiFi, Restaurant", 300.0));
        System.out.println(hs.getAll());

        // Test GetAll
       // System.out.println("===== Test GetAll =====");
      //  List<Hebergement> liste = hs.getAll();
       // for (Hebergement h : liste) {
         //   System.out.println(h);
      //  }

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

       // Test Delete
       // System.out.println("===== Test Delete =====");
       // List<Hebergement> listeApresUpdate = hs.getAll();
       // if (!listeApresUpdate.isEmpty()) {
         // Hebergement hToDelete = listeApresUpdate.get(0);
         //  hs.delete(hToDelete);
         //  System.out.println("Apres suppression:");
         //   System.out.println(hs.getAll());
      //  }

    }
}
