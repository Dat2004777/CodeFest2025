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

    /**
     * Trả về danh sách các Node không thể đi qua để sử dụng khi tìm đường:
     * - Người chơi khác
     * - Obstacle không thể đi qua
     * - NPC Enemy (không có thông tin máu, nên mặc định tránh hết)
     */
    public static List<Node> getUnwalkableNodes(GameMap map) {
        Set<Node> blocked = new HashSet<>();

        // 1. Người chơi khác
        for (Player p : map.getOtherPlayerInfo()) {
            if (p.getHealth() > 0) {
                blocked.add(new Node(p.getX(), p.getY()));
            }
        }

        // 2. Obstacle không đi qua được
        for (Obstacle obs : map.getListObstacles()) {
            String id = obs.getId().toUpperCase();
            if (id.contains("WALL") || id.contains("ROCK") || id.contains("STATUE")
                    || id.contains("BIG") || id.contains("SMALL") || id.contains("BLOCK")) {
                blocked.add(new Node(obs.getX(), obs.getY()));
            }
        }

        // 3. NPC Enemy - tránh toàn bộ
        for (Enemy e : map.getListEnemies()) {
            blocked.add(new Node(e.getX(), e.getY()));
        }

        return new ArrayList<>(blocked);
    }
}
