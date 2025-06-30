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
     * Tìm throwable gần nhất trong SafeZone và cố gắng nhặt nếu có thể.
     * @return true nếu có hành động nhặt hoặc di chuyển.
     */
    public boolean searchAndPickup(GameMap map, Player player) {
        List<Weapon> throwables = map.getAllThrowable();
        if (throwables.isEmpty()) {
            System.out.println("🎯 No throwable found on map.");
            return false;
        }

        int safeZone = map.getSafeZone();
        int mapSize = map.getMapSize();

        Weapon closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Weapon t : throwables) {
            Node node = new Node(t.getX(), t.getY());

            if (!PathUtils.checkInsideSafeArea(node, safeZone, mapSize)) continue;

            int dist = Math.abs(t.getX() - player.getX()) + Math.abs(t.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = t;
            }
        }

        if (closest == null) {
            System.out.println("⚠️ No throwable in SafeZone.");
            return false;
        }

        if (player.getX() == closest.getX() && player.getY() == closest.getY()) {
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
                    System.out.println("➡️ Moving to throwable at (" + to.getX() + "," + to.getY() + "): " + path);
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to move to throwable: " + e.getMessage());
                }
            } else {
                System.out.println("🚫 No path to throwable due to obstacles.");
            }
        }

        return false;
    }

    /**
     * Tính khoảng cách ngắn nhất đến throwable trong safezone.
     */
    public int getClosestThrowableDistance(GameMap map, Player player) {
        int safeZone = map.getSafeZone();
        int mapSize = map.getMapSize();

        return map.getAllThrowable().stream()
                .filter(t -> PathUtils.checkInsideSafeArea(new Node(t.getX(), t.getY()), safeZone, mapSize))
                .mapToInt(t -> Math.abs(t.getX() - player.getX()) + Math.abs(t.getY() - player.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
