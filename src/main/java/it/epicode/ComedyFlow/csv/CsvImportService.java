package it.epicode.ComedyFlow.csv;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import it.epicode.ComedyFlow.indirizzi.Comune;
import it.epicode.ComedyFlow.indirizzi.ComuneRepository;
import it.epicode.ComedyFlow.indirizzi.Provincia;
import it.epicode.ComedyFlow.indirizzi.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CsvImportService {

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private ComuneRepository comuneRepository;

    @Transactional
    public void importComuniAndProvince(String comuniFile, String provinceFile) throws IOException {
        Map<String, Provincia> provinceMap = new HashMap<>();

        // 1. Leggi e importa le Province
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(provinceFile))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            String[] line;
            boolean firstLine = true;
            while ((line = reader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String sigla = line[0].trim();
                String nomeProvincia = line[1].trim();
                String regione = line[2].trim();

                Provincia provincia = new Provincia();
                provincia.setSigla(sigla);
                provincia.setNome(nomeProvincia);
                provinciaRepository.save(provincia);
                provinceMap.put(sigla, provincia);
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }


        // 2. Leggi e importa i Comuni
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(comuniFile))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            String[] line;
            boolean isFirst = true;
            while ((line = reader.readNext()) != null) {
                if (isFirst) { isFirst = false; continue; } // Salta intestazione

                if (line.length < 4) continue; // Evita righe malformate

                String nomeComune = line[2].trim();
                String nomeProvincia = line[3].trim();

                Provincia provincia = provinceMap.values().stream()
                        .filter(p -> p.getNome().equalsIgnoreCase(nomeProvincia))
                        .findFirst()
                        .orElse(null);

                if (provincia != null) {
                    Comune comune = new Comune();
                    comune.setNome(nomeComune);
                    comune.setProvincia(provincia);
                    comuneRepository.save(comune);
                } else {
                    System.out.println("Provincia non trovata per il comune: " + nomeComune);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }}
    public boolean shouldRunImport() {
        return provinciaRepository.count() == 0 && comuneRepository.count() == 0;
    }}