package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.equipments.HealingItem;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class HealingItemSearcher {
    private final Hero hero;

    public HealingItemSearcher(Hero hero) {
        this.hero = hero;
    }

    /**
     * Tìm healing item gần nhất trong vùng an toàn, cố gắng nhặt hoặc di chuyển tới.
     * @return true nếu có hành động (pickup hoặc move)
     */
    public boolean searchAndPickup(GameMap map, Player player) {
        List<HealingItem> items = map.getListHealingItems();
        if (items.isEmpty()) {
            System.out.println("🩹 No healing items found.");
            return false;
        }

        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        HealingItem closest = null;
        int minDist = Integer.MAX_VALUE;

        for (HealingItem item : items) {
            Node itemNode = new Node(item.getX(), item.getY());

            // Bỏ qua item nằm ngoài vùng an toàn
            if (!PathUtils.checkInsideSafeArea(itemNode, safeZone, mapSize)) continue;

            int dist = Math.abs(item.getX() - player.getX()) + Math.abs(item.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = item;
            }
        }

        if (closest == null) {
            System.out.println("⚠️ No healing item in SafeZone.");
            return false;
        }

        if (player.getX() == closest.getX() && player.getY() == closest.getY()) {
            try {
                hero.pickupItem();
                System.out.println("✅ Picked up healing item at: (" + closest.getX() + "," + closest.getY() + ")");
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to pickup healing item: " + e.getMessage());
                return false;
            }
        } else {
            return moveTo(player, closest.getX(), closest.getY(), map);
        }
    }

    private boolean moveTo(Player player, int tx, int ty, GameMap map) {
        Node from = new Node(player.getX(), player.getY());
        Node to = new Node(tx, ty);
        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map); // enemy + traps + obstacles

        String path = PathUtils.getShortestPath(map, avoid, from, to, false);
        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("➡️ Moving to healing item at (" + tx + "," + ty + "): " + path);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to move to healing item: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ No valid path to healing item.");
        }
        return false;
    }

    /**
     * Trả về khoảng cách đến healing item gần nhất (chỉ tính trong vùng an toàn).
     */
    public int getClosestHealingItemDistance(GameMap map, Player player) {
        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        return map.getListHealingItems().stream()
                .filter(item -> PathUtils.checkInsideSafeArea(
                        new Node(item.getX(), item.getY()), safeZone, mapSize))
                .mapToInt(item -> Math.abs(item.getX() - player.getX()) + Math.abs(item.getY() - player.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
