import com.unieats.dao.FoodItemDao;
import com.unieats.services.StockUpdateService;

public class test_stock_update {
    public static void main(String[] args) {
        try {
            System.out.println("Testing stock update functionality...");
            
            // Test direct DAO update
            FoodItemDao foodItemDao = new FoodItemDao();
            
            // Get a food item to test with
            var item = foodItemDao.getById(1);
            if (item != null) {
                System.out.println("Original stock for item " + item.getName() + ": " + item.getStock());
                
                // Test stock update
                foodItemDao.updateStock(1, 1);
                
                // Check updated stock
                var updatedItem = foodItemDao.getById(1);
                System.out.println("Updated stock for item " + updatedItem.getName() + ": " + updatedItem.getStock());
            }
            
            // Test real-time service
            System.out.println("\nTesting real-time stock service...");
            StockUpdateService service = StockUpdateService.getInstance();
            service.start();
            
            // Add a test listener
            service.addListener(new StockUpdateService.StockUpdateListener() {
                @Override
                public void onStockUpdated(int itemId, int quantityReduced) {
                    System.out.println("Real-time update: Item " + itemId + " stock reduced by " + quantityReduced);
                }
                
                @Override
                public void onStockUpdateError(int itemId, String error) {
                    System.out.println("Real-time error: Item " + itemId + " - " + error);
                }
                
                @Override
                public void onAllItemsRefreshed() {
                    System.out.println("All items refreshed");
                }
            });
            
            // Test real-time update
            service.updateStock(1, 1);
            
            Thread.sleep(2000); // Wait for async updates
            
            service.stop();
            System.out.println("Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
