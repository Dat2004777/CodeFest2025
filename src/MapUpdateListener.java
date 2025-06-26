import controllers.ChestAndEggBreaker;
import controllers.SafeZoneHandler;
import controllers.GunSearcher;
import controllers.CombatManager;
import controllers.ChestAndEggBreaker;

import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;

public class MapUpdateListener implements Emitter.Listener {
    private final Hero hero;
    private final SafeZoneHandler safeZoneHandler;
    private final GunSearcher gunSearcher;
    private final CombatManager combatManager;
    private final ChestAndEggBreaker chestAndEggBreaker;

    public MapUpdateListener(Hero hero) {
        this.hero = hero;
        this.safeZoneHandler = new SafeZoneHandler(hero); // Khởi tạo đúng
        this.gunSearcher = new GunSearcher(hero);
        this.combatManager = new CombatManager(hero);// Khởi tạo đúng
        this.chestAndEggBreaker = new ChestAndEggBreaker(hero);
    }

    @Override
    public void call(Object... args) {
        try {
            if (args == null || args.length == 0) return;

            GameMap gameMap = hero.getGameMap();
            gameMap.updateOnUpdateMap(args[0]);

            Player player = gameMap.getCurrentPlayer();
            if (player == null || player.getHealth() == 0) {
                System.out.println("Player is dead or data is not available.");
                return;
            }

            // Step 1: Di chuyển vào safe zone nếu cần
            if (!safeZoneHandler.isInSafeZone(player)) {
                safeZoneHandler.moveToSafeZone(player);
                return;
            }

            // Step 2: Tìm và nhặt súng nếu chưa có
            if (hero.getInventory().getGun() == null) {
                gunSearcher.searchAndPickup(gameMap, player);
                return;
            }

            // Step 3: (Tuỳ chọn) Gắn thêm các logic khác tại đây, ví dụ:
            //Combat
            combatManager.engageEnemy();

            // phá chest
            chestAndEggBreaker.breakNearbyChestOrEgg();
            // itemManager.pickupHealingIfLowHP();

        } catch (Exception e) {
            System.err.println("Critical error in call method: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
