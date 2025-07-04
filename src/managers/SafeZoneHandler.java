package managers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class SafeZoneHandler {
    private final Hero hero;

    public SafeZoneHandler(Hero hero) {
        this.hero = hero;
    }

    // Kiểm tra nếu vị trí hiện tại nằm trong vùng an toàn (vùng sáng)
    public boolean isInSafeZone(Player player) {
        GameMap map = hero.getGameMap();
        return PathUtils.checkInsideSafeArea(
                new Node(player.getX(), player.getY()),
                map.getSafeZone(),
                map.getMapSize()
        );
    }

    // Di chuyển vào vùng an toàn (vùng sáng), né chướng ngại vật và enemy
    public void moveToSafeZone(Player player) {
        GameMap map = hero.getGameMap();
        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();
        int centerNode = (int) Math.sqrt(mapSize)/2;

        Node current = new Node(player.getX(), player.getY());
        Node center = new Node(centerNode, centerNode);



        // Nếu đang trong vùng an toàn thì không cần di chuyển
        if (PathUtils.checkInsideSafeArea(current, safeZone, mapSize)) {
            System.out.println("🟢 Already in safe zone.");
            return;
        }

        // Nếu center không nằm trong vùng sáng thì tìm điểm gần center
        if (!PathUtils.checkInsideSafeArea(center, safeZone, mapSize)) {
            System.out.println("⚠ Center is not inside safe zone!");
            return;
        }

        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);
        String path = PathUtils.getShortestPath(map, avoid, current, center, true); // chỉ trong vùng sáng

        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("🛡️ Moving to safe zone: " + path);
            } catch (IOException e) {
                System.err.println("❌ Failed to move to safe zone: " + e.getMessage());
            }
        } else {
            System.out.println("⚠ No path found to safe zone.");
        }
    }
}
