package searcher;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ChestAndEggBreaker {
    private final Hero hero;

    public ChestAndEggBreaker(Hero hero) {
        this.hero = hero;
    }

    /**
     * Tấn công nếu có rương/trứng kế bên.
     */
    public boolean breakIfAdjacent() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();

        List<Obstacle> targets = getSafeChestsAndEggs(map);
        if (targets.isEmpty()) return false;

        Obstacle closest = findClosestObstacle(targets, self);
        if (closest == null) return false;

        int dist = distance(self.getX(), self.getY(), closest.getX(), closest.getY());
        String direction = getDirection(self.getX(), self.getY(), closest.getX(), closest.getY());

        if (dist == 1 && !direction.isEmpty()) {
            try {
                hero.attack(direction);
                System.out.println("🪓 Attacking chest/egg at direction: " + direction);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to attack chest/egg: " + e.getMessage());
            }
        }

        return false;
    }

    /**
     * Di chuyển đến gần rương/trứng (1 ô kề bên).
     */
    public boolean moveToChestOrEgg() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();

        List<Obstacle> targets = getSafeChestsAndEggs(map);
        if (targets.isEmpty()) return false;

        Obstacle closest = findClosestObstacle(targets, self);
        if (closest == null) return false;

        Node from = new Node(self.getX(), self.getY());
        Node to = new Node(closest.getX(), closest.getY());

        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);
        String path = PathUtils.getShortestPath(map, avoid, from, to, false);

        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("🚶 Moving to chest/egg: " + path);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to move to chest/egg: " + e.getMessage());
            }
        } else {
            System.out.println("⚠ No path to chest/egg due to obstacles.");
        }

        return false;
    }

    public int getClosestChestDistance(GameMap map, Player self) {
        return getSafeChestsAndEggs(map).stream()
                .mapToInt(o -> distance(self.getX(), self.getY(), o.getX(), o.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    private List<Obstacle> getSafeChestsAndEggs(GameMap map) {
        return map.getListObstacles().stream()
                .filter(o -> {
                    String id = o.getId().toUpperCase(Locale.ROOT);
                    boolean isChestOrEgg = id.startsWith("CHEST") || id.startsWith("DRAGON_EGG");
                    boolean inSafeZone = PathUtils.checkInsideSafeArea(new Node(o.getX(), o.getY()), map.getSafeZone(), map.getMapSize());
                    return isChestOrEgg && inSafeZone;
                })
                .collect(Collectors.toList());
    }

    private Obstacle findClosestObstacle(List<Obstacle> targets, Player self) {
        return targets.stream()
                .min(Comparator.comparingInt(o -> distance(o.getX(), o.getY(), self.getX(), self.getY())))
                .orElse(null);
    }

    private int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private String getDirection(int x1, int y1, int x2, int y2) {
        if (x1 == x2) return y2 < y1 ? "d" : "u";
        else if (y1 == y2) return x2 < x1 ? "l" : "r";
        return "";
    }
}
