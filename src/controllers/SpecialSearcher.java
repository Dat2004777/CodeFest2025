package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class SpecialSearcher {
    private final Hero hero;

    public SpecialSearcher(Hero hero) {
        this.hero = hero;
    }

    /**
     * Tìm và nhặt vũ khí đặc biệt (Special Weapon) trong vùng an toàn.
     * @return true nếu có hành động (nhặt hoặc di chuyển), false nếu không làm gì.
     */
    public boolean searchAndPickup(GameMap map, Player player) {
        List<Weapon> specials = map.getAllSpecial();
        if (specials.isEmpty()) {
            System.out.println("🔮 No special weapons on the map.");
            return false;
        }

        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        Weapon closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Weapon sp : specials) {
            Node node = new Node(sp.getX(), sp.getY());

            // Bỏ qua nếu ngoài vùng an toàn
            if (!PathUtils.checkInsideSafeArea(node, safeZone, mapSize)) continue;

            int dist = Math.abs(sp.getX() - player.getX()) + Math.abs(sp.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = sp;
            }
        }

        if (closest == null) {
            System.out.println("⚠️ No special weapon in SafeZone.");
            return false;
        }

        if (player.getX() == closest.getX() && player.getY() == closest.getY()) {
            try {
                hero.pickupItem();
                System.out.println("✅ Picked up special weapon at: (" + closest.getX() + "," + closest.getY() + ")");
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to pickup special weapon: " + e.getMessage());
                return false;
            }
        } else {
            return moveTo(closest.getX(), closest.getY(), player, map);
        }
    }

    private boolean moveTo(int tx, int ty, Player player, GameMap map) {
        Node from = new Node(player.getX(), player.getY());
        Node to = new Node(tx, ty);
        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map); // bao gồm chướng ngại, trap, enemy

        String path = PathUtils.getShortestPath(map, avoid, from, to, false);
        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("➡️ Moving to special weapon at (" + tx + "," + ty + "): " + path);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to move to special weapon: " + e.getMessage());
            }
        } else {
            System.out.println("🚫 No path to special weapon due to obstacles.");
        }

        return false;
    }

    /**
     * Tính khoảng cách đến vũ khí đặc biệt gần nhất trong vùng an toàn.
     */
    public int getClosestSpecialDistance(GameMap map, Player player) {
        List<Weapon> specials = map.getAllSpecial();
        if (specials.isEmpty()) return Integer.MAX_VALUE;

        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        return specials.stream()
                .filter(sp -> PathUtils.checkInsideSafeArea(new Node(sp.getX(), sp.getY()), safeZone, mapSize))
                .mapToInt(sp -> Math.abs(sp.getX() - player.getX()) + Math.abs(sp.getY() - player.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
