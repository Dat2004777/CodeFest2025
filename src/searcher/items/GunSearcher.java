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


    public boolean searchAndPickup(GameMap map, Player player) {
        Weapon currentGun = hero.getInventory().getGun();
        Weapon bestGun = super.findClosestItem(map, player);
        if (bestGun == null) return false;

        // Nếu đang cầm súng, so sánh priority
        if (currentGun != null) {
            int newScore = evaluator.evaluate(bestGun);
            int currentScore = evaluator.evaluate(currentGun);

            if (newScore < currentScore) {
                System.out.println("🔽 Skipping lower priority gun: " + bestGun.getId());
                return false;
            }

            // Tính đường đi đến súng
            Node from = new Node(player.getX(), player.getY());
            Node to = new Node(bestGun.getX(), bestGun.getY());
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

            String path = PathUtils.getShortestPath(map, avoid, from, to, false);
            if (path != null && path.length() == 1) {
                try {
                    hero.revokeItem(currentGun.getId());
                    System.out.println("♻️ Preparing to replace current gun with: " + bestGun.getId());
                } catch (IOException e) {
                    System.err.println("❌ Failed to revoke current gun: " + e.getMessage());
                    return false;
                }
            }

            // Nếu đã đứng ngay trên súng thì cũng cần revoke
            if (player.getX() == bestGun.getX() && player.getY() == bestGun.getY()) {
                try {
                    hero.revokeItem(currentGun.getId());
                    System.out.println("♻️ Replaced current gun with: " + bestGun.getId());
                } catch (IOException e) {
                    System.err.println("❌ Failed to revoke current gun: " + e.getMessage());
                    return false;
                }
            }
        }

        // Gọi hàm gốc để thực hiện move & pickup
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
