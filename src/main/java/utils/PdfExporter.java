package utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.function.Function;

public class PdfExporter {

    public static <T> void exportToPdf(String title, String fileName, List<T> data, String[] headers, Function<T, String>[] fieldExtractors) {
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        String dest = desktopPath + File.separator + fileName;

        try {
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add Title
            document.add(new Paragraph(title).setFontSize(20).setBold());
            document.add(new Paragraph("Généré le: " + new java.util.Date().toString()).setFontSize(10));
            document.add(new Paragraph("\n"));

            // Create Table
            Table table = new Table(UnitValue.createPercentArray(headers.length)).useAllAvailableWidth();

            // Add Headers
            for (String header : headers) {
                table.addHeaderCell(new Paragraph(header).setBold());
            }

            // Add Data Rows
            for (T item : data) {
                for (Function<T, String> extractor : fieldExtractors) {
                    table.addCell(new Paragraph(extractor.apply(item)));
                }
            }

            document.add(table);
            document.close();

            System.out.println("PDF créé avec succès: " + dest);
        } catch (FileNotFoundException e) {
            System.err.println("Erreur lors de la création du PDF: " + e.getMessage());
        }
    }
}
