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

    /**
     * Kiểm tra nếu player đang ở trong vùng an toàn (vùng sáng).
     */
    public boolean isInSafeZone(Player player) {
        if (player == null) return false;

        GameMap map = hero.getGameMap();
        Node current = new Node(player.getX(), player.getY());

        return PathUtils.checkInsideSafeArea(
                current,
                map.getSafeZone(),
                map.getMapSize()
        );
    }

    /**
     * Di chuyển về trung tâm vùng an toàn nếu đang ở ngoài.
     */
    public void moveToSafeZone(Player player) {
        if (player == null || player.getHealth() == null || player.getHealth() <= 0) {
            System.out.println("⚠️ Invalid or dead player, skipping safe zone move.");
            return;
        }

        GameMap map = hero.getGameMap();
        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        Node current = new Node(player.getX(), player.getY());

        if (PathUtils.checkInsideSafeArea(current, safeZone, mapSize)) {
            System.out.println("🟢 Already in safe zone.");
            return;
        }

        Node center = new Node(mapSize / 2, mapSize / 2);

        if (!PathUtils.checkInsideSafeArea(center, safeZone, mapSize)) {
            System.out.println("⚠ Center is not inside safe zone — cannot use center as destination.");
            return;
        }

        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);
        String path = PathUtils.getShortestPath(map, avoid, current, center, true); // chỉ di chuyển trong vùng sáng

        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("🛡️ Moving to safe zone at center: " + path);
            } catch (IOException e) {
                System.err.println("❌ Failed to move to safe zone: " + e.getMessage());
            }
        } else {
            System.out.println("⚠ No valid path to safe zone center found.");
        }
    }
}
