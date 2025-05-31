package it.epicode.ComedyFlow.csv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsvImportController {

    @Autowired
    private CsvImportService csvImportService;

    @GetMapping("/importa-csv")
    public String importaCsv() {
        try {
            csvImportService.importComuniAndProvince("src/main/java/it/epicode/ComedyFlow//csv/comuni-italiani.csv", "src/main/java/it/epicode/ComedyFlow/csv/province-italiane.csv");
            return "Importazione completata!";
        } catch (Exception e) {
            return "Errore durante l'importazione: " + e.getMessage();
        }
    }
}
