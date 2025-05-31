package it.epicode.ComedyFlow.csv;

import it.epicode.ComedyFlow.csv.CsvConstants;
import it.epicode.ComedyFlow.csv.CsvImportService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0) // PRIORITÀ ASSOLUTA
public class CsvImportRunner implements ApplicationRunner {

    private final CsvImportService csvImportService;

    public CsvImportRunner(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (csvImportService.shouldRunImport()) { // Facoltativo: per evitare doppi insert
                csvImportService.importComuniAndProvince(CsvConstants.COMUNI_FILE, CsvConstants.PROVINCE_FILE);
                System.out.println("✅ Importazione CSV avviata automaticamente.");
            } else {
                System.out.println("ℹ️ Dati già presenti, importazione saltata.");
            }
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'importazione automatica: " + e.getMessage());
        }
    }
}
