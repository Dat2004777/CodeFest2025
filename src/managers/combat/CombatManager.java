package managers.combat;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import managers.combat.weapon.WeaponCombatStrategy;
import utils.DodgeUtils;
import utils.EnemyUtils;

import java.io.IOException;
import java.util.List;

public class CombatManager {
    private final Hero hero;
    private final List<WeaponCombatStrategy> combatStrategies;

    public CombatManager(Hero hero, List<WeaponCombatStrategy> combatStrategies) {
        this.hero = hero;
        this.combatStrategies = combatStrategies;
    }

    public boolean handleCombatIfNeeded(GameMap gameMap, Player self) {
        // Tìm kẻ địch gần nhất
        Player target = EnemyUtils.getClosestEnemy(gameMap, self);
        if (target == null) return false;

        // 1. Thử tấn công nếu có vũ khí phù hợp và trong tầm
        for (WeaponCombatStrategy strategy : combatStrategies) {
            if (strategy.isUsable() && strategy.isInRange(self, target)) {
                boolean success = strategy.attack(self, target);
                if (success) {
                    System.out.println("⚔️ Attacked enemy using strategy: " + strategy.getClass().getSimpleName());
                    return true;
                }
            }
        }

        // 2. Nếu chưa trong tầm → di chuyển đến gần enemy
        Node from = new Node(self.getX(), self.getY());
        Node to = new Node(target.getX(), target.getY());

        List<Node> avoid = DodgeUtils.getUnwalkableNodes(gameMap);
        String path = PathUtils.getShortestPath(gameMap, avoid, from, to, false);

        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("🚶 Moving toward enemy: " + path);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to move to enemy: " + e.getMessage());
            }
        } else {
            System.out.println("🚫 No path found to approach enemy.");
        }

        return false;
    }
}
