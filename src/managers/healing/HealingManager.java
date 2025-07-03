package managers.healing;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.npcs.Ally;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.support_items.SupportItem;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class HealingManager {
    private final Hero hero;

    // Danh sách các item hồi máu auto sử dụng
    private static final Set<String> AUTO_HEAL_ITEMS = Set.of(
            "GOD_LEAF", "SPIRIT_TEAR", "MERMAID_TAIL", "PHOENIX_FEATHERS", "UNICORN_BLOOD"
    );

    public HealingManager(Hero hero) {
        this.hero = hero;
    }

    public boolean handleHealingIfNeeded() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        Float currentHP = self.getHealth();
        if (currentHP == null || currentHP <= 0) return false;

        List<SupportItem> items = hero.getInventory().getListSupportItem();
        for (SupportItem item : items) {
            String id = item.getId();
            if (AUTO_HEAL_ITEMS.contains(id) && currentHP <= getThreshold(id)) {
                try {
                    hero.useItem(id);
                    System.out.println("❤️ Using healing item: " + id);
                    return true;
                } catch (IOException e) {
                    System.err.println("❌ Failed to use healing item: " + e.getMessage());
                }
            }
        }

        // Không có item phù hợp → tìm đồng minh trong safe zone
        // Không có item phù hợp → tìm đồng minh trong safe zone
        List<Ally> allies = map.getListAllies();
        if (allies.isEmpty()) {
            System.out.println("🧍 No allies to follow.");
            return false;
        }

        Ally closest = allies.stream()
                .filter(a -> PathUtils.checkInsideSafeArea(new Node(a.getX(), a.getY()), map.getSafeZone(), map.getMapSize()))
                .min(Comparator.comparingInt(a -> Math.abs(a.getX() - self.getX()) + Math.abs(a.getY() - self.getY())))
                .orElse(null);

        if (closest != null) {
            int hx = self.getX(), hy = self.getY();
            int ax = closest.getX(), ay = closest.getY();

            Node from = new Node(hx, hy);
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

            // Nếu chưa cùng hàng với Ally → về cùng hàng trước
            if (hy != ay) {
                Node to = new Node(hx, ay);  // Đưa bot về cùng hàng
                String path = PathUtils.getShortestPath(map, avoid, from, to, false);
                if (path != null && !path.isEmpty()) {
                    try {
                        hero.move(path);
                        System.out.println("↕️ Aligning with Ally on same row: " + path);
                        return true;
                    } catch (IOException e) {
                        System.err.println("❌ Error aligning with ally: " + e.getMessage());
                    }
                }
            } else {
                // Đã cùng hàng, tiến lại gần Ally theo X
                int distance = Math.abs(ax - hx);
                if (distance > 1) {  // Nếu đã đủ gần thì không cần nữa
                    Node to = new Node(ax, ay);
                    String path = PathUtils.getShortestPath(map, avoid, from, to, false);
                    if (path != null && !path.isEmpty()) {
                        try {
                            hero.move(path);
                            System.out.println("↔️ Approaching ally on same row: " + path);
                            return true;
                        } catch (IOException e) {
                            System.err.println("❌ Error approaching ally: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("🧍 Already close enough to ally.");
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Ngưỡng máu cho từng item - khi nào thì nên dùng item đó.
     */
    private int getThreshold(String id) {
        switch (id) {
            case "ELIXIR_OF_LIFE": return 20;
            case "UNICORN_BLOOD": return 50;
            case "SPIRIT_TEAR": return 70;
            case "MERMAID_TAIL": return 60;
            case "PHOENIX_FEATHERS": return 40;
            default: return 40;
        }
    }
}
