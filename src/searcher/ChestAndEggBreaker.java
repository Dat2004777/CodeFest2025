package searcher;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChestAndEggBreaker {
    private final Hero hero;

    public ChestAndEggBreaker(Hero hero) {
        this.hero = hero;
    }

    /**
     * Trả về true nếu đã tấn công thành công 1 rương/trứng ở kế bên (nằm trong vùng an toàn).
     */
    public boolean breakIfAdjacent() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        List<Obstacle> chests = map.getListChests();
        List<Obstacle> targets = new ArrayList<>();

        for (Obstacle o : chests) {
            if ((o.getId().startsWith("CHEST") || o.getId().startsWith("DRAGON_EGG")) &&
                    PathUtils.checkInsideSafeArea(new Node(o.getX(), o.getY()), map.getSafeZone(), map.getMapSize())) {
                targets.add(o);
            }
        }

        if (targets.isEmpty()) return false;

        Obstacle closest = targets.stream()
                .min(Comparator.comparingInt(o ->
                        Math.abs(o.getX() - self.getX()) + Math.abs(o.getY() - self.getY())))
                .orElse(null);

        if (closest == null) return false;

        int dist = Math.abs(closest.getX() - self.getX()) + Math.abs(closest.getY() - self.getY());
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
     * Di chuyển đến gần rương/trứng (1 ô kề bên) nếu có, tránh vật cản và chỉ nếu trong safe zone.
     */
    public boolean moveToChestOrEgg() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        List<Obstacle> chests = map.getListChests();
        List<Obstacle> targets = new ArrayList<>();

        for (Obstacle o : chests) {
            if ((o.getId().startsWith("CHEST") || o.getId().startsWith("DRAGON_EGG")) &&
                    PathUtils.checkInsideSafeArea(new Node(o.getX(), o.getY()), map.getSafeZone(), map.getMapSize())) {
                targets.add(o);
            }
        }

        if (targets.isEmpty()) return false;

        Obstacle closest = targets.stream()
                .min(Comparator.comparingInt(o ->
                        Math.abs(o.getX() - self.getX()) + Math.abs(o.getY() - self.getY())))
                .orElse(null);

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

    private String getDirection(int x1, int y1, int x2, int y2) {
        if (x1 == x2) return y2 < y1 ? "d" : "u";
        else if (y1 == y2) return x2 < x1 ? "l" : "r";
        return "";
    }

    /**
     * Trả về khoảng cách tới rương/trứng gần nhất nằm trong vùng an toàn
     */
    public int getClosestChestDistance(GameMap map, Player self) {
        return map.getListChests().stream()
                .filter(o ->
                        (o.getId().startsWith("CHEST") || o.getId().startsWith("DRAGON_EGG")) &&
                                PathUtils.checkInsideSafeArea(new Node(o.getX(), o.getY()), map.getSafeZone(), map.getMapSize()))
                .mapToInt(o -> Math.abs(o.getX() - self.getX()) + Math.abs(o.getY() - self.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
