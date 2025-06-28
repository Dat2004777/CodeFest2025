package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.equipments.Armor;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.List;

public class ArmorSearcher {
    private final Hero hero;

    public ArmorSearcher(Hero hero) {
        this.hero = hero;
    }

    public void searchAndPickup(GameMap map, Player player) {
        List<Armor> armors = map.getListArmors();

        if (armors.isEmpty()) return;

        Armor closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Armor a : armors) {
            int dist = Math.abs(a.getX() - player.getX()) + Math.abs(a.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = a;
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

    private boolean isAdjacent(Player player, Armor armor) {
        int dx = Math.abs(player.getX() - armor.getX());
        int dy = Math.abs(player.getY() - armor.getY());
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
