package controllers;

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

    // Di chuyển vào vùng an toàn, né chướng ngại vật và enemy
    public void moveToSafeZone(Player player) {
        GameMap map = hero.getGameMap();
        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        Node current = new Node(player.getX(), player.getY());
        Node center = PathUtils.getCenterOfMap(mapSize);

        if (!PathUtils.checkInsideSafeArea(center, safeZone, mapSize)) {
            System.out.println("⚠ Center is not inside safe zone!");
            return;
        }

        // Lấy danh sách các vị trí không thể đi qua
        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

        // Tìm đường đi đến center, tránh vật cản
        String path = PathUtils.getShortestPath(
                map,
                avoid,
                current,
                center,
                true // chỉ đi trong vùng sáng
        );

        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("✅ Moving to center of safe zone: " + path);
            } catch (IOException e) {
                System.err.println("❌ Failed to move to center: " + e.getMessage());
            }
        } else {
            System.out.println("⚠ No reachable path to safe zone center found.");
        }
    }
}
