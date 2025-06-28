package utils;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.npcs.Enemy;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnemyDetector {
    private final Hero hero;

    public EnemyDetector(Hero hero) {
        this.hero = hero;
    }

    public void avoidNearbyEnemies() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        List<Enemy> enemies = map.getListEnemies();
        Node current = new Node(self.getX(), self.getY());

        List<Node> dangerZones = new ArrayList<>();
        for (Enemy e : enemies) {
            int dist = Math.abs(e.getX() - self.getX()) + Math.abs(e.getY() - self.getY());
            if (dist <= 2) {  // Cảnh báo nếu enemy gần trong vòng 2 ô
                dangerZones.add(new Node(e.getX(), e.getY()));
            }
        }

        if (dangerZones.isEmpty()) return;

        // Di chuyển đến điểm cách xa enemy
        Node bestEscape = null;
        String bestPath = null;
        int maxDist = -1;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int nx = self.getX() + dx;
                int ny = self.getY() + dy;
                if (nx < 0 || ny < 0 || nx >= map.getMapSize() || ny >= map.getMapSize()) continue;

                Node candidate = new Node(nx, ny);
                boolean safe = true;
                int totalDist = 0;

                for (Node danger : dangerZones) {
                    int d = Math.abs(candidate.x - danger.x) + Math.abs(candidate.y - danger.y);
                    if (d <= 1) {  // Tránh di chuyển vào vùng liền kề
                        safe = false;
                        break;
                    }
                    totalDist += d;
                }

                if (safe && totalDist > maxDist) {
                    String path = PathUtils.getShortestPath(map, List.of(), current, candidate, true);
                    if (path != null && !path.isEmpty()) {
                        bestEscape = candidate;
                        bestPath = path;
                        maxDist = totalDist;
                    }
                }
            }
        }

        if (bestPath != null) {
            try {
                hero.move(bestPath);
                System.out.println("Avoiding enemy, moving: " + bestPath);
            } catch (IOException e) {
                System.err.println("Failed to escape enemy: " + e.getMessage());
            }
        }
    }
}
