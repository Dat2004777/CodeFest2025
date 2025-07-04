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

public class MeleeSearcher extends ItemSearcher<Weapon> {
    private WeaponEvaluator<Weapon> evaluator;

    public MeleeSearcher(Hero hero, WeaponEvaluator<Weapon> evaluator) {
        super(hero);
        this.evaluator = evaluator;
    }

    public MeleeSearcher(Hero hero) {
        super(hero);
    }

    @Override
    public boolean searchAndPickup(GameMap map, Player player) {
        Weapon currentMelee = hero.getInventory().getMelee();
        Weapon bestMelee = super.findClosestItem(map, player);
        if (bestMelee == null) return false;

        int dist = Math.abs(player.getX() - bestMelee.getX()) + Math.abs(player.getY() - bestMelee.getY());

        boolean hasRealMelee = currentMelee != null && !currentMelee.getId().equals("HAND");

        // Nếu có vũ khí hiện tại → đánh giá
        if (hasRealMelee && evaluator != null) {
            int newScore = evaluator.evaluate(bestMelee);
            int currentScore = evaluator.evaluate(currentMelee);

            if (newScore < currentScore) {
                System.out.println("🔽 Skipping lower priority melee weapon: " + bestMelee.getId());
                return false;
            }
        }

        // Trường hợp 1: đứng tại vũ khí
        if (dist == 0 && hasRealMelee) {
            try {
                hero.revokeItem(currentMelee.getId());
                System.out.println("♻️ Replaced current melee with: " + bestMelee.getId());
            } catch (IOException e) {
                System.err.println("❌ Failed to revoke melee: " + e.getMessage());
                return false;
            }

            try {
                hero.pickupItem();
                System.out.println("✅ Picked up melee: " + bestMelee.getId());
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to pick up melee: " + e.getMessage());
                return false;
            }
        }

        // Trường hợp 2: cách 1 ô → chuẩn bị đổi
        if (dist == 1 && hasRealMelee) {
            try {
                hero.revokeItem(currentMelee.getId());
                System.out.println("♻️ Revoked melee before moving to: " + bestMelee.getId());
            } catch (IOException e) {
                System.err.println("❌ Failed to revoke melee: " + e.getMessage());
                return false;
            }

            Node from = new Node(player.getX(), player.getY());
            Node to = new Node(bestMelee.getX(), bestMelee.getY());
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

            String path = PathUtils.getShortestPath(map, avoid, from, to, false);
            if (path != null && !path.isEmpty()) {
                try {
                    hero.move(path);
                    System.out.println("➡️ Moving to melee after revoke: " + path);
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to move to melee: " + e.getMessage());
                }
            }
            return false;
        }

        // Trường hợp 3: chưa gần → fallback
        return super.searchAndPickup(map, player);
    }

    @Override
    protected List<Weapon> getCandidateItems(GameMap map) {
        List<Weapon> melees = map.getAllMelee();
        // Bỏ HAND ra khỏi danh sách
        melees.removeIf(w -> w.getId().equals("HAND"));

        if (evaluator != null) {
            melees.sort(Comparator.comparingInt(evaluator::evaluate).reversed());
        }

        return melees;
    }

    @Override
    protected String getItemName() {
        return "melee";
    }
}
