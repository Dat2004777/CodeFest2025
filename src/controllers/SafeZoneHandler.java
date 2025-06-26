package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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

    // Tìm và di chuyển đến điểm gần nhất trong safe zone
    public void moveToSafeZone(Player player) {
        GameMap map = hero.getGameMap();
        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone(); // bán kính vùng tối

        Node current = new Node(player.getX(), player.getY());
        Node center = PathUtils.getCenterOfMap(mapSize);

        // Nếu trung tâm không nằm trong vùng sáng, không nên di chuyển
        if (!PathUtils.checkInsideSafeArea(center, safeZone, mapSize)) {
            System.out.println("⚠ Center is not inside safe zone!");
            return;
        }

        // Tìm đường đi từ vị trí hiện tại đến trung tâm
        String path = PathUtils.getShortestPath(
                map,
                List.of(),  // không tránh chướng ngại thêm
                current,
                center,
                true // Chỉ cho phép di chuyển qua vùng sáng
        );

        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("✅ Moving to center of safe zone: " + path);
            } catch (IOException e) {
                System.err.println("❌ Failed to move to center: " + e.getMessage());
            }
        } else {
            System.out.println("⚠ No valid path to center inside safe zone found.");
        }
    }

}
