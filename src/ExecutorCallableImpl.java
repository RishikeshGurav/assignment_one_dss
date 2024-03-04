import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ExecutorCallableImpl extends UnicastRemoteObject implements InvertedIndexService {

    private final ExecutorService executorService;

    public ExecutorCallableImpl() throws RemoteException {
        super();
        // Creating a fixed-size thread pool, you can adjust the size based on your requirements
        this.executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        try {
            List<String> lines = FileRead.fileRead(fileName);

            // Submitting the Callable task for processing lines
            Future<List<String>> linesFuture = executorService.submit(new LinesProcessor(lines));

            // Getting the processed lines
            List<String> processedLines = linesFuture.get();

            // Submitting the Callable task for building the inverted index
            Future<Map<String, List<Integer>>> invertedIndexFuture = executorService.submit(new InvertedIndexBuilder(processedLines));

            // Getting the inverted index result
            return invertedIndexFuture.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RemoteException("Error processing file", e);
        }
    }

    private static class LinesProcessor implements Callable<List<String>> {
        private final List<String> lines;

        public LinesProcessor(List<String> lines) {
            this.lines = lines;
        }

        @Override
        public List<String> call() throws Exception {
            List<String> processedLines = new ArrayList<>();
            for (String line : lines) {
                // You can add any processing logic here
                processedLines.add(line);
            }
            return processedLines;
        }
    }

    private static class InvertedIndexBuilder implements Callable<Map<String, List<Integer>>> {
        private final List<String> lines;

        public InvertedIndexBuilder(List<String> lines) {
            this.lines = lines;
        }

        @Override
        public Map<String, List<Integer>> call() throws Exception {
            Map<String, List<Integer>> invertedIndex = new HashMap<>();
            for (int i = 0; i < lines.size(); i++) {
                String[] words = lines.get(i).split("\\s+");
                for (String word : words) {
                    word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                    invertedIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(i + 1);
                }
            }
            return invertedIndex;
        }
    }
}
