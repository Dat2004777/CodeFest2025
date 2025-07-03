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

    public MapUpdateListener(Hero hero) {
        this.hero = hero;
        this.armorSearcher = new ArmorSearcher(hero);
        this.gunSearcher = new GunSearcher(hero, gunEvaluator);
        this.healingItemSearcher = new HealingItemSearcher(hero);
        this.meleeSearcher = new MeleeSearcher(hero, meleeEvaluator);
        this.specialSearcher = new SpecialSearcher(hero);
        this.throwableSearcher = new ThrowableSearcher(hero);
        this.chestAndEggBreaker = new ChestAndEggBreaker(hero);
        this.combatStrategies = List.of(
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
    }

    // GUN priority
    Map<String, Integer> gunPriority = Map.of(
            "AK47", 100,
            "UZI", 80,
            "PISTOL", 50,
            "REVOLVER", 60
    );
    WeaponEvaluator<Weapon> gunEvaluator = new SimpleWeaponEvaluator<>(gunPriority);

    // MELEE priority
    Map<String, Integer> meleePriority = Map.of(
            "KNIFE", 90,
            "TREE_BRANCH", 60,
            "HAND", 10
    );
    WeaponEvaluator<Weapon> meleeEvaluator = new SimpleWeaponEvaluator<>(meleePriority);

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

            if (hp > 40 && hp <= 60) {
                if (healingManager.handleHealingIfNeeded()) return;
            } else if (hp <= 40) {
                if (specialItemManager.useSpecialItemsIfNeeded()) return;
            }

            // nhặt súng
            if (hero.getInventory().getGun() == null) {
                System.out.println("GUN: Executed. ");
                gunSearcher.searchAndPickup(gameMap, player);
                return;
            }

//            if (hero.getInventory().getListSupportItem().size() < 4) {
//                System.out.println(hero.getInventory().getListSupportItem().size());
//                if (healingItemSearcher.searchAndPickup(gameMap, player)) {
//                    return;
//                }
//            }

            // Armor
            if (hero.getInventory().getArmor() == null) {
                if (armorSearcher.isStandingOnArmor(gameMap, player)) {
                    System.out.println("GUN: Executed. ");
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

            if (hero.getInventory().getMelee() == null
                    || "HAND".equalsIgnoreCase(hero.getInventory().getMelee().getId())) {
                if (meleeSearcher.searchAndPickup(gameMap, player)) {
                    return;
                }
            }

            if (hero.getInventory().getThrowable() == null) {
                if (throwableSearcher.searchAndPickup(gameMap, player)) {
                    return;
                }
            }

            if (hero.getInventory().getSpecial() == null) {
                if (specialSearcher.searchAndPickup(gameMap, player)) {
                    return;
                }
            }

            if (chestDist < enemyDist) {
                if (chestAndEggBreaker.breakIfAdjacent()) return;

//                if (hero.getInventory().getListSupportItem().size() < 4) {
//                    if (healingItemSearcher.searchAndPickup(gameMap, player)) {
//                        return;
//                    }
//                }

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

                if (hero.getInventory().getMelee() == null
                        || "HAND".equalsIgnoreCase(hero.getInventory().getMelee().getId())) {
                    if (meleeSearcher.searchAndPickup(gameMap, player)) {
                        return;
                    }
                }

                if (hero.getInventory().getThrowable() == null) {
                    if (throwableSearcher.searchAndPickup(gameMap, player)) {
                        return;
                    }
                }

                if (hero.getInventory().getSpecial() == null) {
                    if (specialSearcher.searchAndPickup(gameMap, player)) {
                        return;
                    }
                }

                chestAndEggBreaker.moveToChestOrEgg();
            } else {
                if (hp > 40 && hp <= 60) {
                    if (healingManager.handleHealingIfNeeded()) return;
                } else if (hp <= 40) {
                    if (specialItemManager.useSpecialItemsIfNeeded()) return;
                }

                combatManager.handleCombatIfNeeded(gameMap, player);
            }

        } catch (Exception e) {
            System.err.println("🔥 Critical error in MapUpdateListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
