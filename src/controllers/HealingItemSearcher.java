package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.equipments.HealingItem;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.List;

public class HealingItemSearcher {
    private final Hero hero;

    public HealingItemSearcher(Hero hero) {
        this.hero = hero;
    }

    public void searchAndPickup(GameMap map, Player player) {
        List<HealingItem> items = map.getListHealingItems();
        if (items.isEmpty()) return;

        HealingItem closest = null;
        int minDist = Integer.MAX_VALUE;

        for (HealingItem item : items) {
            int dist = Math.abs(item.getX() - player.getX()) + Math.abs(item.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = item;
            }
        }

        if (closest != null) {
            if (isAdjacent(player, closest)) {
                try {
                    hero.pickupItem();
                } catch (IOException ignored) {}
            } else {
                moveTo(player, closest.getX(), closest.getY(), map);
            }
        }
    }

    private boolean isAdjacent(Player player, HealingItem item) {
        int dx = Math.abs(player.getX() - item.getX());
        int dy = Math.abs(player.getY() - item.getY());
        return dx + dy == 1;
    }

    private void moveTo(Player player, int tx, int ty, GameMap map) {
        Node from = new Node(player.getX(), player.getY());
        Node to = new Node(tx, ty);
        String path = PathUtils.getShortestPath(map, List.of(), from, to, false);
        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
            } catch (IOException ignored) {}
        }
    }
}
