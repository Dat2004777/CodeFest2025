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
import managers.healing.SpecialItemHealingManager;
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
    private final SpecialItemHealingManager specialItemHealingManager;
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
                new GunCombatStrategy(hero),
                new MeleeCombatStrategy(hero),
                new ThrowableCombatStrategy(hero),
                new SpecialWeaponCombatStrategy(hero)
        );
        this.combatManager = new CombatManager(hero, combatStrategies);
        this.healingManager = new HealingManager(hero);
        this.specialItemHealingManager = new SpecialItemHealingManager(hero);
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

            // nếu địch có súng và mình không có súng thì CHẠY

            int chestDist = chestAndEggBreaker.getClosestChestDistance(gameMap, player);
            int enemyDist = EnemyUtils.getClosestEnemyDistance(gameMap, player);
            float hp = player.getHealth();

            if (!safeZoneHandler.isInSafeZone(player)) {
                safeZoneHandler.moveToSafeZone(player);
                return;
            }

            // Trường hợp 1: HP thấp hơn 40 → dùng item đặc biệt
            if (hp <= 40) {
                if (specialItemHealingManager.useSpecialItemsIfNeeded()) return;
            }
            // Trường hợp 2: HP từ 41-60 → dùng item hồi máu nếu có
            else {
                if (healingManager.handleHealingIfNeeded()) return;
            }

            // nhặt súng
            if (hero.getInventory().getGun() == null) {
                gunSearcher.searchAndPickup(gameMap, player);
                return;
            }

            if (hero.getInventory().getListSupportItem().size() < 4) {
                if (healingItemSearcher.searchAndPickup(gameMap, player)) {
                    return;
                }
            }

            // Armor
            if (hero.getInventory().getArmor() == null) {
                if (armorSearcher.isStandingOnArmor(gameMap, player)) {
                    if (armorSearcher.searchAndPickup(gameMap, player)) return;
                } else if (armorSearcher.moveTo(gameMap, player)) {
                    return;
                }
            }

//            // Helmet
//            if (hero.getInventory().getHelmet() == null) {
//                if (helmetSearcher.isStandingOnHelmet(gameMap, player)) {
//                    if (helmetSearcher.searchAndPickup(gameMap, player)) return;
//                } else if (helmetSearcher.moveTo(gameMap, player)) {
//                    return;
//                }
//            }
//
//            if (hero.getInventory().getMelee() == null
//                    || "HAND".equalsIgnoreCase(hero.getInventory().getMelee().getId())) {
//                if (meleeSearcher.searchAndPickup(gameMap, player)) {
//                    return;
//                }
//            }
//
//            if (hero.getInventory().getThrowable() == null) {
//                if (throwableSearcher.searchAndPickup(gameMap, player)) {
//                    return;
//                }
//            }
//
//            if (hero.getInventory().getSpecial() == null) {
//                if (specialSearcher.searchAndPickup(gameMap, player)) {
//                    return;
//                }
//            }

            if (chestDist < enemyDist && !isInventoryFull()) {
                if (chestAndEggBreaker.breakIfAdjacent()) return;

                // HealingItem
                if (hero.getInventory().getListSupportItem().size() < 4) {
                    if (healingItemSearcher.searchAndPickup(gameMap, player)) {
                        return;
                    }
                }

                // Armor
                if (hero.getInventory().getArmor() == null) {
                    if (armorSearcher.isStandingOnArmor(gameMap, player)) {
                        if (armorSearcher.searchAndPickup(gameMap, player)) return;
                    } else if (armorSearcher.moveTo(gameMap, player)) {
                        return;
                    }
                }

                // Helmet
                if (hero.getInventory().getHelmet() == null) {
                    if (helmetSearcher.isStandingOnHelmet(gameMap, player)) {
                        if (helmetSearcher.searchAndPickup(gameMap, player)) return;
                    } else if (helmetSearcher.moveTo(gameMap, player)) {
                        return;
                    }
                }

                // melee
                if (hero.getInventory().getMelee() == null
                        || "HAND".equalsIgnoreCase(hero.getInventory().getMelee().getId())) {
                    if (meleeSearcher.searchAndPickup(gameMap, player)) {
                        return;
                    }
                }

                // throwable
                if (hero.getInventory().getThrowable() == null) {
                    if (throwableSearcher.searchAndPickup(gameMap, player)) {
                        return;
                    }
                }

                // special
                if (hero.getInventory().getSpecial() == null) {
                    if (specialSearcher.searchAndPickup(gameMap, player)) {
                        return;
                    }
                }

                chestAndEggBreaker.moveToChestOrEgg();
            } else {
                // Trường hợp 1: HP thấp hơn 40 → dùng item đặc biệt
                if (hp <= 40) {
                    if (specialItemHealingManager.useSpecialItemsIfNeeded()) return;
                }
                // Trường hợp 2: HP từ 41-60 → dùng item hồi máu nếu có
                else {
                    if (healingManager.handleHealingIfNeeded()) return;
                }

                combatManager.handleCombatIfNeeded(gameMap, player);
            }

            // 2. Revoke item logic (nếu cần)
            if (hero.getInventory().getGun() != null && !Objects.equals(hero.getInventory().getMelee().getId(), "HAND")) {
                if (revokeManager.handleRevokeBeforeSearch(gameMap, player)) {
                    System.out.println("REVOKE: Executed.");
                    return;
                }
            }

        } catch (Exception e) {
            System.err.println("🔥 Critical error in MapUpdateListener: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isInventoryFull() {
        return hero.getInventory().getGun() != null
                && hero.getInventory().getArmor() != null
                && hero.getInventory().getHelmet() != null
                && hero.getInventory().getMelee() != null
                && hero.getInventory().getThrowable() != null
                && hero.getInventory().getSpecial() != null
                && hero.getInventory().getListSupportItem().size() >= 4;
    }

}
