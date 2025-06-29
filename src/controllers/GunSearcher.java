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

    public boolean searchAndPickup(GameMap map, Player player) {
        List<Weapon> guns = map.getAllGun();
        if (guns.isEmpty()) {
            System.out.println("No gun available on the map.");
            return false;
        }

        Weapon closestGun = null;
        int minDist = Integer.MAX_VALUE;

        for (Weapon gun : guns) {
            // ❗ Bỏ qua nếu súng nằm ngoài safe zone
            if (!PathUtils.checkInsideSafeArea(new Node(gun.getX(), gun.getY()), map.getSafeZone(), map.getMapSize())) {
                continue;
            }

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
                    System.out.println("✅ Picked up gun at: " + closestGun.getX() + "," + closestGun.getY());
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to pickup gun: " + e.getMessage());
                }
            } else {
                Node from = new Node(player.getX(), player.getY());
                Node to = new Node(closestGun.getX(), closestGun.getY());
                List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

                String path = PathUtils.getShortestPath(map, avoid, from, to, false);
                if (path != null && !path.isEmpty()) {
                    try {
                        hero.move(path);
                        System.out.println("➡️ Moving to pickup gun: " + path);
                        return true;
                    } catch (IOException e) {
                        System.err.println("❌ Failed to move to gun: " + e.getMessage());
                    }
                } else {
                    System.out.println("🚫 No path to gun due to obstacles or enemies.");
                }
            }
        }

        return false;
    }
}
