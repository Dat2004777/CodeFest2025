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

public class GunSearcher {
    private final Hero hero;

    public GunSearcher(Hero hero) {
        this.hero = hero;
    }

    public void searchAndPickup(GameMap map, Player player) {
        List<Weapon> guns = map.getAllGun();
        if (guns.isEmpty()) {
            System.out.println("No gun available on the map.");
            return;
        }

        Weapon closestGun = null;
        int minDist = Integer.MAX_VALUE;

        for (Weapon gun : guns) {
            int dist = Math.abs(gun.getX() - player.getX()) + Math.abs(gun.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closestGun = gun;
            }
        }

        if (closestGun != null) {
            boolean sameCell = (player.getX() == closestGun.getX() && player.getY() == closestGun.getY());

            if (sameCell) {
                try {
                    hero.pickupItem();
                    System.out.println("Picked up gun at: " + closestGun.getX() + "," + closestGun.getY());
                } catch (IOException e) {
                    System.err.println("Failed to pickup gun: " + e.getMessage());
                }
            } else {
                Node from = new Node(player.getX(), player.getY());
                Node to = new Node(closestGun.getX(), closestGun.getY());
                List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

                String path = PathUtils.getShortestPath(map, avoid, from, to, false);
                if (path != null && !path.isEmpty()) {
                    try {
                        hero.move(path);
                        System.out.println("Moving to pickup gun: " + path);
                    } catch (IOException e) {
                        System.err.println("Failed to move to gun: " + e.getMessage());
                    }
                } else {
                    System.out.println("No path to gun due to obstacles or enemies.");
                }
            }
        }
    }
}
