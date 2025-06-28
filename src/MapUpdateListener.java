import controllers.*;

import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;

public class MapUpdateListener implements Emitter.Listener {
    private final Hero hero;
    private final SafeZoneHandler safeZoneHandler;
    private final GunSearcher gunSearcher;
    private final ChestAndEggBreaker chestAndEggBreaker;
    private final ArmorSearcher armorSearcher;
    private final MeleeSearcher meleeSearcher;
    private final HealingItemSearcher healingItemSearcher;
    private final ThrowableSearcher throwableSearcher;
    private final CombatManager combatManager;

    public MapUpdateListener(Hero hero) {
        this.hero = hero;
        this.safeZoneHandler = new SafeZoneHandler(hero);
        this.gunSearcher = new GunSearcher(hero);
        this.chestAndEggBreaker = new ChestAndEggBreaker(hero);
        this.armorSearcher = new ArmorSearcher(hero);
        this.meleeSearcher = new MeleeSearcher(hero);
        this.healingItemSearcher = new HealingItemSearcher(hero);
        this.throwableSearcher = new ThrowableSearcher(hero);
        this.combatManager = new CombatManager(hero);
    }

    @Override
    public void call(Object... args) {
        try {
            if (args == null || args.length == 0) return;

            GameMap gameMap = hero.getGameMap();
            gameMap.updateOnUpdateMap(args[0]);
            Player player = gameMap.getCurrentPlayer();

            if (player == null || player.getHealth() <= 0) {
                System.out.println("Player is dead or data is not available.");
                return;
            }

            // 1. Nếu ở ngoài safe zone → vào ngay
            if (!safeZoneHandler.isInSafeZone(player)) {
                safeZoneHandler.moveToSafeZone(player);
                return;
            }

            // 2. Nhặt súng nếu chưa có
            if (hero.getInventory().getGun() == null) {
                gunSearcher.searchAndPickup(gameMap, player);
                return;
            }

            // 3. Phá rương hoặc trứng nếu gần
            if (chestAndEggBreaker.breakNearestIfInRange()) {
                return;
            }

            // 4. Ưu tiên nhặt đồ theo thứ tự: giáp → cận chiến → hồi máu → throwable
            armorSearcher.searchAndPickup(gameMap, player);
            meleeSearcher.searchAndPickup(gameMap, player);
            healingItemSearcher.searchAndPickup(gameMap, player);
            throwableSearcher.searchAndPickup(gameMap, player);

            // 5. Tấn công player khác nếu gần
            combatManager.engageEnemy();

        } catch (Exception e) {
            System.err.println("Critical error in MapUpdateListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
