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

public class MeleeSearcher {
    private final Hero hero;

    public MeleeSearcher(Hero hero) {
        this.hero = hero;
    }

    public boolean searchAndPickup(GameMap map, Player player) {
        List<Weapon> melees = map.getAllMelee();
        if (melees.isEmpty()) return false;

        Weapon closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Weapon m : melees) {
            int dist = Math.abs(m.getX() - player.getX()) + Math.abs(m.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = m;
            }
        }

        if (closest != null) {
            boolean sameCell = (player.getX() == closest.getX() && player.getY() == closest.getY());
            if (sameCell) {
                try {
                    hero.pickupItem();
                    System.out.println("Picked up melee at: " + closest.getX() + "," + closest.getY());
                    return true;
                } catch (IOException e) {
                    System.err.println("Failed to pickup melee: " + e.getMessage());
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
                System.out.println("Moving to melee: " + path);
                return true;
            } catch (IOException e) {
                System.err.println("Failed to move to melee: " + e.getMessage());
            }
        } else {
            System.out.println("No path to melee due to obstacles.");
        }
        return false;
    }
}
