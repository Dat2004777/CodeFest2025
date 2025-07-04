package managers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class ItemRevokeManager {
    private final Hero hero;

    public ItemRevokeManager(Hero hero) {
        this.hero = hero;
    }

    public boolean handleRevokeBeforeSearch(GameMap map, Player player) {
        boolean didAction = false;

        Weapon currentGun = hero.getInventory().getGun();
        Weapon currentMelee = hero.getInventory().getMelee();

        List<Weapon> guns = map.getAllGun();
        List<Weapon> melees = map.getAllMelee();

        Node self = new Node(player.getX(), player.getY());

        for (Weapon target : guns) {
            int dist = Math.abs(target.getX() - self.x) + Math.abs(target.getY() - self.y);
            if (dist <= 1 && currentGun != null && !currentGun.getId().equals(target.getId())) {
                didAction |= tryRevoke(currentGun.getId(), "gun", self, map, target);
                break;
            }
        }

        for (Weapon target : melees) {
            int dist = Math.abs(target.getX() - self.x) + Math.abs(target.getY() - self.y);
            if (dist <= 1 && currentMelee != null && !currentMelee.getId().equals("HAND") && !currentMelee.getId().equals(target.getId())) {
                didAction |= tryRevoke(currentMelee.getId(), "melee", self, map, target);
                break;
            }
        }

        return didAction;
    }

    private boolean tryRevoke(String itemId, String type, Node self, GameMap map, Weapon target) {
        // Nếu đứng tại item, phải lùi lại 1 bước trước
        if (self.x == target.getX() && self.y == target.getY()) {
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);
            int[] dx = {-1, 1, 0, 0};
            int[] dy = {0, 0, -1, 1};

            for (int i = 0; i < 4; i++) {
                int nx = self.x + dx[i];
                int ny = self.y + dy[i];

                if (nx < 0 || ny < 0 || nx >= map.getMapSize() || ny >= map.getMapSize()) continue;
                Node to = new Node(nx, ny);

                if (!avoid.contains(to)) {
                    String path = PathUtils.getShortestPath(map, avoid, self, to, false);
                    if (path != null && !path.isEmpty()) {
                        try {
                            hero.move(path);
                            System.out.println("↩️ Step off " + type + " before revoke: " + path);
                            return true;
                        } catch (IOException e) {
                            System.err.println("❌ Failed to move off item: " + e.getMessage());
                        }
                    }
                }
            }
            return false;
        }

        // Nếu cách 1 ô thì revoke luôn
        try {
            hero.revokeItem(itemId);
            System.out.println("♻️ Revoked " + type + " before pickup: " + itemId);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Failed to revoke " + type + ": " + e.getMessage());
            return false;
        }
    }
}
