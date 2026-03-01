package utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import models.ReservationAdminRow;
import services.ServiceReservation;

import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportUtil {

    /**
     * Exporte toutes les réservations admin dans un fichier Excel (.xlsx).
     */
    public static void exportAdminReservations(ServiceReservation sr, String path) throws Exception {

        List<ReservationAdminRow> rows = sr.getAllAdminRows();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            Sheet sh = wb.createSheet("Reservations");

            // En-têtes
            int r = 0;
            Row header = sh.createRow(r++);
            header.createCell(0).setCellValue("Destination");
            header.createCell(1).setCellValue("Client");
            header.createCell(2).setCellValue("Date");
            header.createCell(3).setCellValue("Statut");
            header.createCell(4).setCellValue("Paiement");

            // Données
            for (ReservationAdminRow x : rows) {
                Row row = sh.createRow(r++);
                row.createCell(0).setCellValue(x.getPaysdestination() == null ? "" : x.getPaysdestination());
                row.createCell(1).setCellValue(x.getClientFullName() == null ? "" : x.getClientFullName());
                row.createCell(2).setCellValue(x.getDateReservation() == null ? "" : x.getDateReservation().toString());
                row.createCell(3).setCellValue(x.getStatut() == null ? "" : x.getStatut());
                row.createCell(4).setCellValue(x.getModalitesPaiement() == null ? "" : x.getModalitesPaiement());
            }

            // Auto-size des colonnes
            for (int i = 0; i <= 4; i++) sh.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(path)) {
                wb.write(out);
            }
        }
    }
}
