

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ForkJoinPool;

public class Server {
    public static void main(String[] args) {
        try {
            // Create and export a remote object
            InvertedIndexService service = new ForkJoinPoolImpl();

            // Create registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1098);

            // Bind the remote object's stub in the registry
            registry.rebind("InvertedIndexService", service);

            System.out.println("Service ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

