package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;
import utils.DodgeUtils;
import utils.WeaponEvaluator;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class GunSearcher extends ItemSearcher<Weapon> {
    private WeaponEvaluator<Weapon> evaluator;

    public GunSearcher(Hero hero, WeaponEvaluator<Weapon> evaluator) {
        super(hero);
        this.evaluator = evaluator;
    }
    public GunSearcher(Hero hero) {
        super(hero);
    }


    @Override
    public boolean searchAndPickup(GameMap map, Player player) {
        Weapon currentGun = hero.getInventory().getGun();
        Weapon bestGun = super.findClosestItem(map, player);
        if (bestGun == null) return false;

        int dist = Math.abs(player.getX() - bestGun.getX()) + Math.abs(player.getY() - bestGun.getY());

        // Nếu đang cầm súng, so sánh điểm
        if (currentGun != null) {
            int newScore = evaluator.evaluate(bestGun);
            int currentScore = evaluator.evaluate(currentGun);

            if (newScore < currentScore) {
                System.out.println("🔽 Skipping lower priority gun: " + bestGun.getId());
                return false;
            }
        }

        // Trường hợp 1: đứng tại vũ khí
        if (dist == 0 && currentGun != null) {
            try {
                hero.revokeItem(currentGun.getId());
                System.out.println("♻️ Replaced current melee with: " + bestGun.getId());
            } catch (IOException e) {
                System.err.println("❌ Failed to revoke melee: " + e.getMessage());
                return false;
            }
        }

        // Trường hợp 2: cách 1 ô → revoke trước rồi tự move
        if (dist == 1 && currentGun != null) {
            try {
                hero.revokeItem(currentGun.getId());
                System.out.println("♻️ Revoked gun before moving to: " + bestGun.getId());
            } catch (IOException e) {
                System.err.println("❌ Failed to revoke current gun: " + e.getMessage());
                return false;
            }

            // Di chuyển thủ công
            Node from = new Node(player.getX(), player.getY());
            Node to = new Node(bestGun.getX(), bestGun.getY());
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);
            String path = PathUtils.getShortestPath(map, avoid, from, to, false);
            if (path != null && !path.isEmpty()) {
                try {
                    hero.move(path);
                    System.out.println("➡️ Moving to gun after revoke: " + path);
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to move to gun: " + e.getMessage());
                    return false;
                }
            }
            return false; // không tìm được path
        }

        // Trường hợp 3: chưa gần, gọi lại logic gốc để tự xử lý move + pickup
        return super.searchAndPickup(map, player);

    }



    @Override
    protected List<Weapon> getCandidateItems(GameMap map) {
        List<Weapon> guns = map.getAllGun();
        guns.sort(Comparator.comparingInt((Weapon gun) -> evaluator.evaluate(gun)).reversed());
        return guns;
    }

    @Override
    protected String getItemName() {
        return "gun";
    }
}
