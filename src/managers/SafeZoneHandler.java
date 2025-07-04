package managers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SafeZoneHandler {
    private final Hero hero;

    public SafeZoneHandler(Hero hero) {
        this.hero = hero;
    }

    public boolean isInSafeZone(Player player) {
        if (player == null) return false;

        GameMap map = hero.getGameMap();
        Node current = new Node(player.getX(), player.getY());

        return PathUtils.checkInsideSafeArea(current, map.getSafeZone(), map.getMapSize());
    }

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

        // Tìm tất cả các điểm nằm trong vùng sáng
        List<Node> targets = new ArrayList<>();
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                Node node = new Node(x, y);
                if (PathUtils.checkInsideSafeArea(node, safeZone, mapSize)) {
                    targets.add(node);
                }
            }
        }

        if (targets.isEmpty()) {
            System.out.println("⚠ No valid tiles found in safe zone.");
            return;
        }

        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

//        Node bestTarget = null;
        String bestPath = null;
        int bestLength = Integer.MAX_VALUE;

        for (Node target : targets) {
            String path = PathUtils.getShortestPath(map, avoid, current, target, true);
            if (path != null && !path.isEmpty() && path.length() < bestLength) {
//                bestTarget = target;
                bestPath = path;
                bestLength = path.length();
            }
        }

        if (bestPath != null) {
            try {
                hero.move(bestPath);
                System.out.println("🛡️ Moving to safe zone: " + bestPath);
            } catch (IOException e) {
                System.err.println("❌ Failed to move to safe zone: " + e.getMessage());
            }
        } else {
            System.out.println("⚠ No valid path to any safe zone tile found.");
        }
    }
}
