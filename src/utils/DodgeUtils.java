package utils;

import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.npcs.Enemy;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            if (id.contains("WALL") || id.contains("ROCK") || id.contains("STATUE")
                    || id.contains("BIG") || id.contains("SMALL") || id.contains("BLOCK")
                    || id.contains("TRAP") || id.contains("BANANA_PEEL") || id.contains("DRAGON_EGG")
                    || id.contains("CHEST")) {
                blocked.add(new Node(obs.getX(), obs.getY()));
            }
        }

        // 3. Enemy: né enemy và cả vùng ảnh hưởng quanh enemy
        for (Enemy e : map.getListEnemies()) {
            int ex = e.getX();
            int ey = e.getY();
            blocked.add(new Node(ex, ey)); // chính nó
            blocked.add(new Node(ex + 1, ey));
            blocked.add(new Node(ex - 1, ey));
            blocked.add(new Node(ex, ey + 1));
            blocked.add(new Node(ex, ey - 1));
        }

        return new ArrayList<>(blocked);
    }
}
