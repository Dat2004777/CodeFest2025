package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class CombatManager {
    private final Hero hero;

    public CombatManager(Hero hero) {
        this.hero = hero;
    }

    public boolean engageEnemy() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        List<Player> opponents = map.getOtherPlayerInfo();

        if (opponents.isEmpty()) return false;

        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        for (Player p : opponents) {
            if (p.getHealth() <= 0) continue;

            // Kiểm tra vị trí cả hai trong safe zone
            Node myNode = new Node(self.getX(), self.getY());
            Node enemyNode = new Node(p.getX(), p.getY());

            boolean inSafe = PathUtils.checkInsideSafeArea(myNode, safeZone, mapSize) &&
                    PathUtils.checkInsideSafeArea(enemyNode, safeZone, mapSize);

            if (!inSafe) continue; // bỏ qua nếu 1 trong 2 ngoài safe zone

            int dx = Math.abs(p.getX() - self.getX());
            int dy = Math.abs(p.getY() - self.getY());

            if (dx + dy == 1) {
                String dir = getDirection(self.getX(), self.getY(), p.getX(), p.getY());
                if (!dir.isEmpty()) {
                    try {
                        hero.attack(dir);
                        System.out.println("🗡️ Attacking player (in safezone) at: " + dir);
                        return true;
                    } catch (IOException e) {
                        System.err.println("⚠️ Melee attack failed: " + e.getMessage());
                    }
                }
            }

            if (hero.getInventory().getGun() != null &&
                    ((dx == 0 && dy > 1 && dy <= 2) || (dy == 0 && dx > 1 && dx <= 2))) {
                String dir = getDirection(self.getX(), self.getY(), p.getX(), p.getY());
                if (!dir.isEmpty()) {
                    try {
                        hero.shoot(dir);
                        System.out.println("🔫 Shooting player (in safezone) at: " + dir);
                        return true;
                    } catch (IOException e) {
                        System.err.println("⚠️ Shooting failed: " + e.getMessage());
                    }
                }
            }
        }

        // Nếu không thể tấn công → di chuyển lại gần nhất (không cần trong safezone vì mục tiêu vẫn là enemy gần nhất)
        Player closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Player p : opponents) {
            if (p.getHealth() <= 0) continue;
            int dist = Math.abs(p.getX() - self.getX()) + Math.abs(p.getY() - self.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = p;
            }
        }

        if (closest != null) {
            Node from = new Node(self.getX(), self.getY());
            Node to = new Node(closest.getX(), closest.getY());
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

            String path = PathUtils.getShortestPath(map, avoid, from, to, false);

            if (path != null && !path.isEmpty()) {
                try {
                    hero.move(path);
                    System.out.println("🚶 Moving toward player: " + path);
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to move to player: " + e.getMessage());
                }
            }
        }

        return false;
    }

    private String getDirection(int x1, int y1, int x2, int y2) {
        if (x1 == x2) return y2 < y1 ? "d" : y2 > y1 ? "u" : "";
        if (y1 == y2) return x2 < x1 ? "l" : x2 > x1 ? "r" : "";
        return "";
    }

    public int getClosestEnemyDistance(GameMap map, Player self) {
        return map.getOtherPlayerInfo().stream()
                .filter(p -> p.getHealth() > 0)
                .mapToInt(p -> Math.abs(p.getX() - self.getX()) + Math.abs(p.getY() - self.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
