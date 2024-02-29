

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ForkJoinPoolImpl extends UnicastRemoteObject implements InvertedIndexService {

    protected ForkJoinPoolImpl() throws RemoteException {
        super();
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        try {
            List<String> lines = FileRead.fileRead(fileName);
            ForkJoinPool pool = new ForkJoinPool();
            InvertedIndexTask task = new InvertedIndexTask(lines, 0, lines.size());
            pool.invoke(task);
            return task.getResult();
        } catch (IOException e) {
            throw new RemoteException("Error reading file", e);
        }
    }

    private static class InvertedIndexTask extends RecursiveAction {
        private final List<String> lines;
        private final int start;
        private final int end;
        private final Map<String, List<Integer>> result;
        private static final int THRESHOLD = 1;

        public InvertedIndexTask(List<String> lines, int start, int end) {
            this.lines = lines;
            this.start = start;
            this.end = end;
            this.result = new HashMap<>();
        }

        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                FileRead.locateWords(lines.subList(start, end)).forEach((key, value) ->
                        result.merge(key, value, (v1, v2) -> {
                            List<Integer> merged = new ArrayList<>(v1);
                            merged.addAll(v2);
                            return merged;
                        }));
            } else {
                System.out.println("Fork");
                int mid = start + (end - start) / 2;
                InvertedIndexTask left = new InvertedIndexTask(lines, start, mid);
                InvertedIndexTask right = new InvertedIndexTask(lines, mid, end);
                left.fork();
                right.compute();
                left.join();
            }
        }

        public Map<String, List<Integer>> getResult() {
            return result;
        }
    }
}
