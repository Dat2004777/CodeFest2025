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

    public boolean searchAndPickup(GameMap map, Player player) {
        Weapon currentMelee = hero.getInventory().getMelee();
        Weapon bestMelee = super.findClosestItem(map, player);
        if (bestMelee == null) return false;

        // Nếu đang cầm súng, so sánh priority
        if (currentMelee != null) {
            int newScore = evaluator.evaluate(bestMelee);
            int currentScore = evaluator.evaluate(currentMelee);

            if (newScore < currentScore) {
                System.out.println("🔽 Skipping lower priority gun: " + bestMelee.getId());
                return false;
            }

            // Tính đường đi đến súng
            Node from = new Node(player.getX(), player.getY());
            Node to = new Node(bestMelee.getX(), bestMelee.getY());
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

            String path = PathUtils.getShortestPath(map, avoid, from, to, false);
            if (path != null && path.length() == 1) {
                try {
                    hero.revokeItem(currentMelee.getId());
                    System.out.println("♻️ Preparing to replace current melee with: " + bestMelee.getId());
                } catch (IOException e) {
                    System.err.println("❌ Failed to revoke current melee: " + e.getMessage());
                    return false;
                }
            }

            // Nếu đã đứng ngay trên súng thì cũng cần revoke
            if (player.getX() == bestMelee.getX() && player.getY() == bestMelee.getY()) {
                try {
                    hero.revokeItem(currentMelee.getId());
                    System.out.println("♻️ Replaced current melee with: " + bestMelee.getId());
                } catch (IOException e) {
                    System.err.println("❌ Failed to revoke current melee: " + e.getMessage());
                    return false;
                }
            }
        }

        // Gọi hàm gốc để thực hiện move & pickup
        return super.searchAndPickup(map, player);
    }

    @Override
    protected List<Weapon> getCandidateItems(GameMap map) {
        List<Weapon> melees = map.getAllMelee();
        melees.sort(Comparator.comparingInt((Weapon melee) -> evaluator.evaluate(melee)).reversed());
        return melees;
    }

    @Override
    protected String getItemName() {
        return "melee";
    }
}
