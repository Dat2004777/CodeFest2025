import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
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
import utils.DodgeUtils;
import utils.EnemyUtils;

import java.util.List;

import static utils.EnemyUtils.getClosestEnemyDistance;

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
    private final HealingManager healingManager;
    private final SpecialItemManager specialItemManager;
    private final SafeZoneHandler safeZoneHandler;
    private final CombatManager combatManager;

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
        this.healingManager = new HealingManager(hero);
        this.specialItemManager = new SpecialItemManager(hero);
        this.safeZoneHandler = new SafeZoneHandler(hero);
        this.combatManager = new CombatManager(hero, combatStrategies);
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

            String standingItem = armorSearcher.isStandingOnArmorOrHelmet(gameMap, player);
            int chestDist = chestAndEggBreaker.getClosestChestDistance(gameMap, player);
            int enemyDist = getClosestEnemyDistance(gameMap, player);
            float hp = player.getHealth();
            //Logic bắt đầu từ đây


        } catch (Exception e) {
            System.err.println("🔥 Critical error in MapUpdateListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
