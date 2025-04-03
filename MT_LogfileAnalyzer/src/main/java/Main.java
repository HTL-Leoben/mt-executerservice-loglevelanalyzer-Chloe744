import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Map<String, Object>>> futures = new ArrayList<>();
        File logDir = new File("path/to/logs"); // Pfad anpassen

        // Sammle Log-Dateien
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (logFiles == null) {
            System.out.println("Keine Log-Dateien gefunden.");
            return;
        }

        // Starte Tasks
        for (File file : logFiles) {
            futures.add(executor.submit(new LogAnalyzerTask(file)));
        }

        Map<String, Integer> totalCounts = new HashMap<>();

        // Verarbeite Ergebnisse
        try {
            for (Future<Map<String, Object>> future : futures) {
                Map<String, Object> result = future.get();
                String filename = (String) result.get("filename");
                Map<String, Integer> counts = (Map<String, Integer>) result.get("counts");
                List<String> lines = (List<String>) result.get("filteredLines");

                // Aggregiere Gesamtzählung
                counts.forEach((level, count) ->
                        totalCounts.merge(level, count, Integer::sum)
                );

                // Ausgabe für die Datei
                System.out.println("\n=== Datei: " + filename + " ===");
                System.out.println("Log-Level-Zählungen: " + counts);
                System.out.println("ERROR/WARN-Zeilen (max. 10):");
                lines.stream().limit(10).forEach(System.out::println);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        // Gesamtzusammenfassung
        System.out.println("\n=== Gesamtzusammenfassung ===");
        totalCounts.forEach((level, count) ->
                System.out.println(level + ": " + count)
        );
    }
}