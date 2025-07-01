package managers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.npcs.Ally;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.healing_items.HealingItem;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class HealingManager {
    private final Hero hero;

    public HealingManager(Hero hero) {
        this.hero = hero;
    }

    public boolean handleHealingIfNeeded() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();

        if (self.getHealth() >= 50) return false;

        List<HealingItem> healingItems = hero.getInventory().getListHealingItem();
        if (!healingItems.isEmpty()) {
            try {
                hero.useItem(healingItems.get(0).getId());
                System.out.println("❤️ Using healing item: " + healingItems.get(0).getId());
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to use healing item: " + e.getMessage());
            }
        } else {
            // Không có item hồi máu → đi theo ally trong safezone
            List<Ally> allies = map.getListAllies();
            if (allies.isEmpty()) {
                System.out.println("🧍 No allies to follow.");
                return false;
            }

            int safeZone = map.getSafeZone();
            int mapSize = map.getMapSize();

            Ally closest = null;
            int minDist = Integer.MAX_VALUE;

            for (Ally a : allies) {
                Node allyNode = new Node(a.getX(), a.getY());
                if (!PathUtils.checkInsideSafeArea(allyNode, safeZone, mapSize)) continue;

                int dist = Math.abs(a.getX() - self.getX()) + Math.abs(a.getY() - self.getY());
                if (dist < minDist) {
                    minDist = dist;
                    closest = a;
                }
            }

            if (closest != null) {
                Node from = new Node(self.getX(), self.getY());
                Node to = new Node(closest.getX(), closest.getY());
                List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

                String path = PathUtils.getShortestPath(map, avoid, from, to, false);
                if (path != null && !path.isEmpty()) {
                    try {
                        hero.move(path);
                        System.out.println("🤝 Moving toward ally in safezone: " + path);
                        return true;
                    } catch (IOException e) {
                        System.err.println("❌ Failed to move to ally: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("⚠️ No allies inside safe zone to follow.");
            }
        }

        return false;
    }
}
