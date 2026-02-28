package org.example.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.models.ReservationAdminRow;
import org.example.services.ServiceReservation;

import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportXlsxUtil {

    // Export de la table admin (exactement comme ce que tu vois)
    public static void exportAdminReservations(ServiceReservation sr, String path) throws Exception {

        List<ReservationAdminRow> rows = sr.getAllAdminRows(null);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            Sheet sh = wb.createSheet("Reservations");

            int r = 0;
            Row header = sh.createRow(r++);
            header.createCell(0).setCellValue("id_reservation");
            header.createCell(1).setCellValue("client");
            header.createCell(2).setCellValue("date_reservation");
            header.createCell(3).setCellValue("statut");
            header.createCell(4).setCellValue("modalites_paiement");
            header.createCell(5).setCellValue("montant_total");

            for (ReservationAdminRow x : rows) {
                Row row = sh.createRow(r++);
                row.createCell(0).setCellValue(x.getIdReservation());
                row.createCell(1).setCellValue(x.getClientFullName() == null ? "" : x.getClientFullName());
                row.createCell(2).setCellValue(x.getDateReservation() == null ? "" : x.getDateReservation());
                row.createCell(3).setCellValue(x.getStatut() == null ? "" : x.getStatut());
                row.createCell(4).setCellValue(x.getModalitesPaiement() == null ? "" : x.getModalitesPaiement());
                row.createCell(5).setCellValue(x.getMontantTotal());
            }

            for (int i = 0; i <= 5; i++) sh.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(path)) {
                wb.write(out);
            }
        }
    }
}