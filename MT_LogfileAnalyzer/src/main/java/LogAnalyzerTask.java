import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.Callable;

public class LogAnalyzerTask implements Callable<Map<String, Object>> {
    private final File logFile;

    public LogAnalyzerTask(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        Map<String, Integer> countMap = new HashMap<>();
        List<String> filteredLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse Log-Level (Annahme: Level ist erstes Token)
                String[] parts = line.split("\\s+", 3);
                if (parts.length >= 1) {
                    // Bereinige das Level von Nicht-Buchstaben (z. B. ":")
                    String level = parts[0].replaceAll("[^A-Za-z]", "");

                    // Aktualisiere die Zählung
                    countMap.put(level, countMap.getOrDefault(level, 0) + 1);

                    // Füge Zeilen mit ERROR oder WARN zur Liste hinzu
                    if ("ERROR".equalsIgnoreCase(level) || "WARN".equalsIgnoreCase(level)) {
                        filteredLines.add(line);
                    }
                }
            }
        }

        // Ergebnismap erstellen
        Map<String, Object> result = new HashMap<>();
        result.put("filename", logFile.getName());
        result.put("counts", countMap);
        result.put("filteredLines", filteredLines);

        return result;
    }
}