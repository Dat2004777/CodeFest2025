package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class ThrowableSearcher {
    private final Hero hero;

    public ThrowableSearcher(Hero hero) {
        this.hero = hero;
    }

    /**
     * Tìm throwable gần nhất và cố gắng nhặt nếu có thể.
     * @return true nếu có hành động (di chuyển hoặc nhặt), false nếu không làm gì.
     */
    public boolean searchAndPickup(GameMap map, Player player) {
        List<Weapon> throwables = map.getAllThrowable();
        if (throwables.isEmpty()) return false;

        Weapon closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Weapon t : throwables) {
            int dist = Math.abs(t.getX() - player.getX()) + Math.abs(t.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = t;
            }
        }

        if (closest != null) {
            boolean sameCell = (player.getX() == closest.getX() && player.getY() == closest.getY());

            if (sameCell) {
                try {
                    hero.pickupItem();
                    System.out.println("✅ Picked up throwable at: " + closest.getX() + "," + closest.getY());
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to pickup throwable: " + e.getMessage());
                }
            } else {
                Node from = new Node(player.getX(), player.getY());
                Node to = new Node(closest.getX(), closest.getY());
                List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

                String path = PathUtils.getShortestPath(map, avoid, from, to, false);
                if (path != null && !path.isEmpty()) {
                    try {
                        hero.move(path);
                        System.out.println("🚶 Moving to throwable: " + path);
                        return true;
                    } catch (IOException e) {
                        System.err.println("❌ Failed to move to throwable: " + e.getMessage());
                    }
                } else {
                    System.out.println("⚠ No path to throwable due to obstacles.");
                }
            }
        }

        return false;
    }
}
