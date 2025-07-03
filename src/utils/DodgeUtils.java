package utils;

import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.npcs.Enemy;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.obstacles.ObstacleTag;
import jsclub.codefest.sdk.model.players.Player;

import java.util.*;

public class DodgeUtils {

    public static List<Node> getUnwalkableNodes(GameMap map) {
        Set<Node> blocked = new HashSet<>();

        // 1. Người chơi khác (còn sống)
        for (Player p : map.getOtherPlayerInfo()) {
            if (p.getHealth() > 0) {
                blocked.add(new Node(p.getX(), p.getY()));
            }
        }

        // 2. Obstacle không thể đi qua
        for (Obstacle obs : map.getListObstacles()) {
            String id = obs.getId().toUpperCase();
            if (obs.getTags().stream().noneMatch(tag -> tag == ObstacleTag.CAN_GO_THROUGH) || id.contains("WALL") || id.contains("ROCK") || id.contains("STATUE")
                    || id.contains("BIG") || id.contains("SMALL") || id.contains("BLOCK")
                    || id.contains("TRAP") || id.contains("BANANA_PEEL") || id.contains("DRAGON_EGG")
                    || id.contains("CHEST") || id.contains("HUNT_TRAP") || id.contains("INDESTRUCTIBLE")) {
                blocked.add(new Node(obs.getX(), obs.getY()));
            }
        }

// 3. Enemy: né vùng tấn công và vị trí dự đoán sẽ di chuyển tới
        int mapSize = map.getMapSize();
        for (Enemy e : map.getListEnemies()) {
            int ex = e.getX(), ey = e.getY();
            int range = e.getAttackRange();

            // Né vùng xung quanh enemy
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -range; dy <= range; dy++) {
                    if (Math.abs(dx) + Math.abs(dy) > range) continue;
                    int nx = ex + dx;
                    int ny = ey + dy;
                    if (nx >= 0 && ny >= 0 && nx < mapSize && ny < mapSize) {
                        blocked.add(new Node(nx, ny));
                    }
                }
            }

            // Né vị trí dự đoán enemy sẽ đi tới
            Node predicted = getPredictedEnemyStep(e, map);
            if (predicted.getX() >= 0 && predicted.getY() >= 0
                    && predicted.getX() < mapSize && predicted.getY() < mapSize) {
                blocked.add(predicted);
            }
        }

        return new ArrayList<>(blocked);
    }

    // Dự đoán bước kế tiếp của Enemy dựa vào player gần nhất
    private static Node getPredictedEnemyStep(Enemy e, GameMap map) {
        int ex = e.getX(), ey = e.getY();

        Player closest = map.getOtherPlayerInfo().stream()
                .filter(p -> p.getHealth() > 0)
                .min(Comparator.comparingInt(p -> Math.abs(p.getX() - ex) + Math.abs(p.getY() - ey)))
                .orElse(null);

        if (closest == null) return new Node(ex, ey); // đứng yên

        int dx = closest.getX() - ex;
        int dy = closest.getY() - ey;

        // Ưu tiên trục X
        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx > 0) return new Node(ex + 1, ey);
            if (dx < 0) return new Node(ex - 1, ey);
        }

        // Nếu không thì trục Y
        if (dy > 0) return new Node(ex, ey + 1);
        if (dy < 0) return new Node(ex, ey - 1);

        return new Node(ex, ey); // fallback
    }
}
