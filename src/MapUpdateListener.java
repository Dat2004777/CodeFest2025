import jsclub.codefest.sdk.model.support_items.SupportItem;
import jsclub.codefest.sdk.model.weapon.Weapon;
import managers.*;

import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import managers.combat.CombatManager;
import managers.combat.weapon.*;
import managers.healing.HealingManager;
import managers.healing.SpecialItemManager;
import searcher.ChestAndEggBreaker;
import searcher.items.*;
import utils.EnemyUtils;
import utils.SimpleWeaponEvaluator;
import utils.WeaponEvaluator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final CombatManager combatManager;
    private final HealingManager healingManager;
    private final SpecialItemManager specialItemManager;
    private final SafeZoneHandler safeZoneHandler;
    private final HelmetSearcher helmetSearcher;
    private final ItemRevokeManager revokeManager;

    // GUN priority
    Map<String, Integer> gunPriority = Map.of(
            "SHOTGUN", 100,
            "SCEPTER", 80,
            "CROSSBOW", 60,
            "RUBBER_GUN", 20
    );
    WeaponEvaluator<Weapon> gunEvaluator = new SimpleWeaponEvaluator<>(gunPriority);

    // MELEE priority
    Map<String, Integer> meleePriority = Map.of(
            "MACE", 100,
            "AXE", 80,
            "KNIFE", 60,
            "TREE_BRANCH", 40,
            "BONE", 20,
            "HAND", 10
    );
    WeaponEvaluator<Weapon> meleeEvaluator = new SimpleWeaponEvaluator<>(meleePriority);

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
                new ShotgunCombatStrategy(hero),
                new MeleeCombatStrategy(hero),
                new GunCombatStrategy(hero),
                new ThrowableCombatStrategy(hero),
                new SpecialWeaponCombatStrategy(hero)
        );
        this.combatManager = new CombatManager(hero, combatStrategies);
        this.healingManager = new HealingManager(hero);
        this.specialItemManager = new SpecialItemManager(hero);
        this.safeZoneHandler = new SafeZoneHandler(hero);
        this.helmetSearcher = new HelmetSearcher(hero);
        this.revokeManager = new ItemRevokeManager(hero, gunEvaluator, meleeEvaluator);
    }

    @Override
    public void call(Object... args) {
        try {
            if (args == null || args.length == 0) return;

            GameMap gameMap = hero.getGameMap();
            gameMap.updateOnUpdateMap(args[0]);
            Player player = gameMap.getCurrentPlayer();

            System.out.println("Current Score: " + player.getScore());

            if (player.getHealth() <= 0) {
                System.out.println("Player is dead or data is not available.");
                return;
            }

            int chestDist = chestAndEggBreaker.getClosestChestDistance(gameMap, player);
            int enemyDist = EnemyUtils.getClosestEnemyDistance(gameMap, player);
            float hp = player.getHealth();

            // 1. Safe zone check
            if (!safeZoneHandler.isInSafeZone(player)) {
                System.out.println("SAFEZONE: Executed.");
                safeZoneHandler.moveToSafeZone(player);
                return;
            }

            // 2. Revoke item logic (nếu cần)
            if (hero.getInventory().getGun() != null && !Objects.equals(hero.getInventory().getMelee().getId(), "HAND")) {
                if (revokeManager.handleRevokeBeforeSearch(gameMap, player)) {
                    System.out.println("REVOKE: Executed.");
                    return;
                }
            }

            // 3. Healing logic
             if (hp <= 40) {
                 if (specialItemManager.useSpecialItemsIfNeeded()) {
                     System.out.println("SPECIAL HEAL: Executed.");
                     return;
                 }
             }
            else {
                if (healingManager.handleHealingIfNeeded()) {
                    System.out.println("HEALING: Executed.");
                    return;
                }
            }

            // 4. Gun
            if (hero.getInventory().getGun() == null) {
                System.out.println("GUN: Executed.");
                if (gunSearcher.searchAndPickup(gameMap, player)) return;
            }

            // 5. Support Items
            if (hero.getInventory().getListSupportItem().size() < 4) {
                System.out.println("SUPPORT ITEM: Executed.");
                if (healingItemSearcher.searchAndPickup(gameMap, player)) return;
            }

            // 6. Armor
            if (hero.getInventory().getArmor() == null) {
                if (armorSearcher.isStandingOnArmor(gameMap, player)) {
                    System.out.println("ARMOR (on item): Executed.");
                    if (armorSearcher.searchAndPickup(gameMap, player)) return;
                } else {
                    System.out.println("ARMOR (move): Executed.");
                    if (armorSearcher.moveTo(gameMap, player)) return;
                }
            }

            // 7. Helmet
            if (hero.getInventory().getHelmet() == null) {
                if (helmetSearcher.isStandingOnHelmet(gameMap, player)) {
                    System.out.println("HELMET (on item): Executed.");
                    if (helmetSearcher.searchAndPickup(gameMap, player)) return;
                } else {
                    System.out.println("HELMET (move): Executed.");
                    if (helmetSearcher.moveTo(gameMap, player)) return;
                }
            }

            // 8. Melee
            if (hero.getInventory().getMelee() == null || "HAND".equalsIgnoreCase(hero.getInventory().getMelee().getId())) {
                System.out.println("MELEE: Executed.");
                if (meleeSearcher.searchAndPickup(gameMap, player)) return;
            }

            // 9. Throwable
            if (hero.getInventory().getThrowable() == null) {
                System.out.println("THROWABLE: Executed.");
                if (throwableSearcher.searchAndPickup(gameMap, player)) return;
            }

            // 10. Special
            if (hero.getInventory().getSpecial() == null) {
                System.out.println("SPECIAL: Executed.");
                if (specialSearcher.searchAndPickup(gameMap, player)) return;
            }

            // 11. Chest or Combat
            if (chestDist < enemyDist) {
                System.out.println("CHEST: Executed.");
                if (chestAndEggBreaker.breakIfAdjacent()) return;
                chestAndEggBreaker.moveToChestOrEgg();
            } else {
                System.out.println("COMBAT: Executed.");
                if (hp > 40 && hp <= 60) {
                    if (healingManager.handleHealingIfNeeded()) return;
                } else if (hp <= 40) {
                    if (specialItemManager.useSpecialItemsIfNeeded()) return;
                }
                combatManager.handleCombatIfNeeded(gameMap, player);
            }

            // 12. Final retry for better gun/melee
//            if (hero.getInventory().getGun() != null &&
//                    (hero.getInventory().getMelee() != null || "HAND".equalsIgnoreCase(hero.getInventory().getMelee().getId()))) {
//
//                System.out.println("RETRY GUN: Executed.");
//                if (gunSearcher.searchAndPickup(gameMap, player)) return;
//
//                System.out.println("RETRY MELEE: Executed.");
//                if (meleeSearcher.searchAndPickup(gameMap, player)) return;
//            }

        } catch (Exception e) {
            System.err.println("🔥 Critical error in MapUpdateListener: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
