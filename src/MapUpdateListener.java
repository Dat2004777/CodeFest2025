import managers.*;

import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import managers.combat.weapon.*;
import searcher.ChestAndEggBreaker;
import searcher.items.*;

import java.util.Comparator;
import java.util.List;

public class MapUpdateListener implements Emitter.Listener {
    private final Hero hero;
    private final ArmorSearcher armorSearcher;
    private final GunSearcher gunSearcher;
    private final HealingItemSearcher healingItemSearcher;
    private final MeleeSearcher meleeSearcher;
    private final SpecialSearcher specialSearcher;
    private final ThrowableSearcher throwableSearcher;
    private final ChestAndEggBreaker chestAndEggBreaker;
    private final List<WeaponCombatStrategy> combatStrategies;

    public MapUpdateListener(Hero hero) {
        this.hero = hero;
        this.armorSearcher = new ArmorSearcher(hero);
        this.gunSearcher = new GunSearcher(hero);
        this.healingItemSearcher = new HealingItemSearcher(hero);
        this.meleeSearcher = new MeleeSearcher(hero);
        this.specialSearcher = new SpecialSearcher(hero);
        this.throwableSearcher = new ThrowableSearcher(hero);
        this.chestAndEggBreaker = new ChestAndEggBreaker(hero);
        this.combatStrategies = List.of(
                new MeleeCombatStrategy(hero),
                new GunCombatStrategy(hero),
                new ThrowableCombatStrategy(hero),
                new SpecialWeaponCombatStrategy(hero)
        );
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

            if (hero.getInventory().getGun() == null) {
                if (gunSearcher.searchAndPickup(gameMap, player)) return;
            }

            //Logic bắt đầu từ đây
            if (chestAndEggBreaker.breakIfAdjacent()) {
                return;
            }

            if (hero.getInventory().getArmor() == null
                    || hero.getInventory().getHelmet() == null) {
                if (armorSearcher.searchAndPickup(gameMap, player)) return;
            }

            if (hero.getInventory().getMelee() == null && !"HAND".equalsIgnoreCase(hero.getInventory().getMelee().getId())) {
                if (meleeSearcher.searchAndPickup(gameMap, player)) return;
            }

            if (hero.getInventory().getListHealingItem().size() < 4) {
                if (healingItemSearcher.searchAndPickup(gameMap, player)) return;
            }

            if (hero.getInventory().getThrowable() == null) {
                if (throwableSearcher.searchAndPickup(gameMap, player)) return;
            }

            if (hero.getInventory().getSpecial() == null) {
                if (specialSearcher.searchAndPickup(gameMap, player)) return;
            }

            List<Player> enemies = gameMap.getOtherPlayerInfo();

            Player self = player;
            Player target = enemies.stream()
                    .filter(e -> e.getHealth() > 0)
                    .min(Comparator.comparingInt(e ->
                            Math.abs(e.getX() - self.getX()) + Math.abs(e.getY() - self.getY())))
                    .orElse(null);

            if (target != null) {
                for (WeaponCombatStrategy strategy : combatStrategies) {
                    if (strategy.isUsable() && strategy.isInRange(self, target)) {
                        if (strategy.attack(self, target)) {
                            System.out.println("⚔️ Attacked enemy using strategy: " + strategy.getClass().getSimpleName());
                            return;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("🔥 Critical error in MapUpdateListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
