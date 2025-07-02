package managers.healing;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.npcs.Ally;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.healing_items.HealingItem;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


public class HealingManager {
    private final Hero hero;

    private static final Set<String> AUTO_HEAL_ITEMS = Set.of(
            "GOD_LEAF", "SPIRIT_TEAR", "MERMAID_TAIL", "PHOENIX_FEATHERS", "UNICORN_BLOOD"
    );

    public HealingManager(Hero hero) {
        this.hero = hero;
    }

    public boolean handleHealingIfNeeded() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        float currentHP = self.getHealth();

        List<HealingItem> items = hero.getInventory().getListHealingItem();
        for (HealingItem item : items) {
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
//        List<Ally> allies = map.getListAllies();
//        if (allies.isEmpty()) {
//            System.out.println("🧍 No allies to follow.");
//            return false;
//        }
//
//        Ally closest = allies.stream()
//                .filter(a -> PathUtils.checkInsideSafeArea(new Node(a.getX(), a.getY()), map.getSafeZone(), map.getMapSize()))
//                .min(Comparator.comparingInt(a -> Math.abs(a.getX() - self.getX()) + Math.abs(a.getY() - self.getY())))
//                .orElse(null);
//
//        if (closest != null) {
//            Node from = new Node(self.getX(), self.getY());
//            Node to = new Node(closest.getX(), closest.getY());
//            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);
//
//            String path = PathUtils.getShortestPath(map, avoid, from, to, false);
//            if (path != null && !path.isEmpty()) {
//                try {
//                    hero.move(path);
//                    System.out.println("🤝 Moving toward ally for healing: " + path);
//                    return true;
//                } catch (IOException e) {
//                    System.err.println("❌ Failed to move to ally: " + e.getMessage());
//                }
//            }
//        }

        return false;
    }

    private int getThreshold(String id) {
        switch (id) {
            case "ELIXIR_OF_LIFE": return 20;
            case "UNICORN_BLOOD": return 50;
            case "SPIRIT_TEAR": return 70;
            case "PHOENIX_FEATHERS": return 40;
            case "MERMAID_TAIL": return 60;
            default: return 40;
        }
    }
}
