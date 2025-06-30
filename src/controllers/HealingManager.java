package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.equipments.HealingItem;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.List;

public class HealingManager {
    private final Hero hero;

    public HealingManager(Hero hero) {
        this.hero = hero;
    }

    /**
     * Kiểm tra máu và sử dụng item hồi máu nếu máu < 50%
     * @return true nếu có hành động sử dụng item, false nếu không
     */
    public boolean tryToHeal() {
        GameMap map = hero.getGameMap();
        Player player = map.getCurrentPlayer();

        if (player == null || player.getHealth() >= 50) {
            return false; // Không cần hồi máu
        }

        List<HealingItem> healingItems = hero.getInventory().getListHealingItem();
        if (healingItems == null || healingItems.isEmpty()) {
            System.out.println("❌ No healing items to use.");
            return false;
        }

        for (HealingItem item : healingItems) {
            try {
                hero.useItem(item.getId());
                System.out.println("❤️ Used healing item: " + item.getId() + " at HP = " + player.getHealth());
                return true;
            } catch (IOException e) {
                System.err.println("⚠️ Failed to use healing item " + item.getId() + ": " + e.getMessage());
            }
        }

        return false;
    }
}
