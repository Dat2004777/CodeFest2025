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

    public boolean searchAndPickup(GameMap map, Player player) {
        List<HealingItem> items = map.getListHealingItems();
        if (items.isEmpty()) return false;

        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        HealingItem closest = null;
        int minDist = Integer.MAX_VALUE;

        for (HealingItem item : items) {
            Node itemNode = new Node(item.getX(), item.getY());

            // Chỉ xét item trong vùng sáng
            if (!PathUtils.checkInsideSafeArea(itemNode, safeZone, mapSize)) continue;

            int dist = Math.abs(item.getX() - player.getX()) + Math.abs(item.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = item;
            }
        }

        if (closest != null) {
            boolean sameCell = (player.getX() == closest.getX() && player.getY() == closest.getY());

            if (sameCell) {
                try {
                    hero.pickupItem();
                    System.out.println("❤️ Picked up healing item at: " + closest.getX() + "," + closest.getY());
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to pickup healing item: " + e.getMessage());
                }
            } else {
                return moveTo(player, closest.getX(), closest.getY(), map);
            }
        }

        return false;
    }

    private boolean moveTo(Player player, int tx, int ty, GameMap map) {
        Node from = new Node(player.getX(), player.getY());
        Node to = new Node(tx, ty);
        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

        String path = PathUtils.getShortestPath(map, avoid, from, to, false);
        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("➡️ Moving to healing item: " + path);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to move to healing item: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ No path to healing item due to obstacles.");
        }
        return false;
    }
}
