package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.equipments.Armor;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class ArmorSearcher {
    private final Hero hero;

    public ArmorSearcher(Hero hero) {
        this.hero = hero;
    }

    /**
     * Tìm và nhặt Armor gần nhất trong vùng an toàn.
     * @return true nếu có hành động (nhặt hoặc di chuyển)
     */
    public boolean searchAndPickup(GameMap map, Player player) {
        List<Armor> armors = map.getListArmors();
        if (armors.isEmpty()) {
            System.out.println("🛡️ No armor items found.");
            return false;
        }

        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        Armor closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Armor armor : armors) {
            Node armorNode = new Node(armor.getX(), armor.getY());

            // Bỏ qua armor ngoài vùng an toàn
            if (!PathUtils.checkInsideSafeArea(armorNode, safeZone, mapSize)) continue;

            int dist = Math.abs(armor.getX() - player.getX()) + Math.abs(armor.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = armor;
            }
        }

        if (closest == null) {
            System.out.println("⚠️ No armor in safe zone.");
            return false;
        }

        if (player.getX() == closest.getX() && player.getY() == closest.getY()) {
            try {
                hero.pickupItem();
                System.out.println("✅ Picked up armor at: (" + closest.getX() + "," + closest.getY() + ")");
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to pickup armor: " + e.getMessage());
            }
        } else {
            return moveTo(player, closest.getX(), closest.getY(), map);
        }

        return false;
    }

    private boolean moveTo(Player player, int tx, int ty, GameMap map) {
        Node from = new Node(player.getX(), player.getY());
        Node to = new Node(tx, ty);
        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map); // enemy + traps + obstacles

        String path = PathUtils.getShortestPath(map, avoid, from, to, false);
        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("➡️ Moving to armor at (" + tx + "," + ty + "): " + path);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to move to armor: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ No valid path to armor.");
        }

        return false;
    }

    public int getClosestArmorDistance(GameMap map, Player player) {
        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        return map.getListArmors().stream()
                .filter(a -> PathUtils.checkInsideSafeArea(new Node(a.getX(), a.getY()), safeZone, mapSize))
                .mapToInt(a -> Math.abs(a.getX() - player.getX()) + Math.abs(a.getY() - player.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
