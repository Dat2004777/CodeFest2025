package managers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import utils.DodgeUtils;
import utils.WeaponEvaluator;

import java.io.IOException;
import java.util.List;

public class ItemRevokeManager {
    private final Hero hero;
    private final WeaponEvaluator<Weapon> gunEvaluator;
    private final WeaponEvaluator<Weapon> meleeEvaluator;

    public ItemRevokeManager(Hero hero, WeaponEvaluator<Weapon> gunEval, WeaponEvaluator<Weapon> meleeEval) {
        this.hero = hero;
        this.gunEvaluator = gunEval;
        this.meleeEvaluator = meleeEval;
    }

    public boolean handleRevokeBeforeSearch(GameMap map, Player player) {
        boolean didAction = false;

        Weapon currentGun = hero.getInventory().getGun();
        Weapon currentMelee = hero.getInventory().getMelee();
        Node self = new Node(player.getX(), player.getY());

        // Xử lý GUN
        for (Weapon target : map.getAllGun()) {
            if (currentGun == null) continue;
            if (!shouldReplaceWeapon(currentGun, target, gunEvaluator)) continue;

            int dist = distance(self, target);
            if (dist == 1) {
                return tryRevoke(currentGun.getId(), currentGun.getId());
            }
            if (dist == 0) {
                return stepBackAndRevoke(currentGun.getId(), currentGun.getId(), self, map);
            }
        }

        // Xử lý MELEE
        for (Weapon target : map.getAllMelee()) {
            if (currentMelee == null || "HAND".equalsIgnoreCase(currentMelee.getId())) continue;
            if (!shouldReplaceWeapon(currentMelee, target, meleeEvaluator)) continue;

            int dist = distance(self, target);
            if (dist == 1) {
                return tryRevoke(currentMelee.getId(), currentMelee.getId());
            }
            if (dist == 0) {
                return stepBackAndRevoke(currentMelee.getId(), currentMelee.getId(), self, map);
            }
        }

        return didAction;
    }

    private boolean shouldReplaceWeapon(Weapon current, Weapon target, WeaponEvaluator<Weapon> evaluator) {
        return evaluator.evaluate(target) >= evaluator.evaluate(current);
    }

    private int distance(Node a, Weapon b) {
        return Math.abs(a.x - b.getX()) + Math.abs(a.y - b.getY());
    }

    private boolean tryRevoke(String itemId, String itemID) {
        try {
            hero.revokeItem(itemId);
            System.out.println("♻️ Revoked: " + itemID);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Failed to revoke: " + itemID + " - " + e.getMessage());
            return false;
        }
    }

    private boolean stepBackAndRevoke(String itemId, String type, Node self, GameMap map) {
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
                        System.out.println("↩️ Step back from " + type + ": " + path);
                        return true; // revoke sẽ được thực hiện ở tick sau
                    } catch (IOException e) {
                        System.err.println("❌ Failed to step back from " + type + ": " + e.getMessage());
                    }
                }
            }
        }
        return false;
    }
}
