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

    private boolean justBrokeChest = false;

    public MapUpdateListener(Hero hero) {
        this.hero = hero;
        this.safeZoneHandler = new SafeZoneHandler(hero);
        this.gunSearcher = new GunSearcher(hero);
        this.chestAndEggBreaker = new ChestAndEggBreaker(hero);
        this.combatManager = new CombatManager(hero);
        this.armorSearcher = new ArmorSearcher(hero);
        this.meleeSearcher = new MeleeSearcher(hero);
        this.healingItemSearcher = new HealingItemSearcher(hero);
        this.throwableSearcher = new ThrowableSearcher(hero);
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

            // 1. Đi vào SafeZone nếu cần
            if (!safeZoneHandler.isInSafeZone(player)) {
                System.out.println("🔵 Moving to Safe Zone");
                safeZoneHandler.moveToSafeZone(player);
                return;
            }

            // 2. Nhặt súng nếu chưa có
            if (hero.getInventory().getGun() == null) {
                System.out.println("🔫 Searching for Gun");
                gunSearcher.searchAndPickup(gameMap, player);
                return;
            }

            // 3. Tính khoảng cách đến rương và enemy
            int distToChest = chestAndEggBreaker.getClosestChestDistance(gameMap, player);
            int distToEnemy = combatManager.getClosestEnemyDistance(gameMap, player);

            // 4. Ưu tiên mục tiêu gần hơn: rương hoặc địch
            if (distToChest <= distToEnemy) {
                if (chestAndEggBreaker.breakIfAdjacent()) {
                    System.out.println("💥 Broke chest/egg nearby");
                    justBrokeChest = true;
                    return;
                }

                if (justBrokeChest) {
                    System.out.println("⏳ Waiting 1 turn after chest break");
                    justBrokeChest = false;
                    return;
                }

                if (chestAndEggBreaker.moveToChestOrEgg()) {
                    System.out.println("📦 Moving toward chest/egg");
                    return;
                }

            } else {
                if (combatManager.engageEnemy()) {
                    System.out.println("⚔️ Attacking nearby player");
                    return;
                }
            }

            // 5. Nếu không có hành động chính nào → nhặt item
            if (armorSearcher.searchAndPickup(gameMap, player)) return;
            if (meleeSearcher.searchAndPickup(gameMap, player)) return;
            if (healingItemSearcher.searchAndPickup(gameMap, player)) return;
            if (throwableSearcher.searchAndPickup(gameMap, player)) return;

            // 6. Không còn gì để làm → đứng yên hoặc xử lý tùy chọn
            System.out.println("Nothing urgent to do");

        } catch (Exception e) {
            System.err.println("🔥 Critical error in MapUpdateListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
}