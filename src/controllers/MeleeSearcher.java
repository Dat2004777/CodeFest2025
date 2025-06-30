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

    /**
     * Tìm kiếm và nhặt vũ khí cận chiến gần nhất trong vùng an toàn.
     * @return true nếu đã thực hiện hành động (nhặt hoặc di chuyển), false nếu không làm gì.
     */
    public boolean searchAndPickup(GameMap map, Player player) {
        List<Weapon> melees = map.getAllMelee();
        if (melees.isEmpty()) {
            System.out.println("⚠️ No melee weapon found.");
            return false;
        }

        Weapon closestMelee = null;
        int minDist = Integer.MAX_VALUE;

        for (Weapon melee : melees) {
            Node node = new Node(melee.getX(), melee.getY());

            // ❗ Chỉ xét melee nằm trong vùng an toàn
            if (!PathUtils.checkInsideSafeArea(node, map.getSafeZone(), map.getMapSize())) continue;

            int dist = Math.abs(melee.getX() - player.getX()) + Math.abs(melee.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closestMelee = melee;
            }
        }

        if (closestMelee != null) {
            boolean sameCell = (player.getX() == closestMelee.getX() && player.getY() == closestMelee.getY());

            if (sameCell) {
                try {
                    hero.pickupItem();
                    System.out.println("🗡️ Picked up melee weapon at: " + closestMelee.getX() + "," + closestMelee.getY());
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to pickup melee weapon: " + e.getMessage());
                }
            } else {
                Node from = new Node(player.getX(), player.getY());
                Node to = new Node(closestMelee.getX(), closestMelee.getY());
                List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

                String path = PathUtils.getShortestPath(map, avoid, from, to, false);
                if (path != null && !path.isEmpty()) {
                    try {
                        hero.move(path);
                        System.out.println("➡️ Moving to melee weapon: " + path);
                        return true;
                    } catch (IOException e) {
                        System.err.println("❌ Failed to move to melee weapon: " + e.getMessage());
                    }
                } else {
                    System.out.println("🚫 No path to melee weapon due to obstacles or enemies.");
                }
            }
        }

        return false;
    }

    /**
     * Tính khoảng cách Manhattan đến vũ khí cận chiến gần nhất (chỉ trong safezone).
     */
    public int getClosestMeleeDistance(GameMap map, Player player) {
        return map.getAllMelee().stream()
                .filter(w -> PathUtils.checkInsideSafeArea(new Node(w.getX(), w.getY()), map.getSafeZone(), map.getMapSize()))
                .mapToInt(w -> Math.abs(w.getX() - player.getX()) + Math.abs(w.getY() - player.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
