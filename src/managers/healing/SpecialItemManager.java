package managers.healing;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.support_items.SupportItem;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.List;

public class SpecialItemManager {
    private final Hero hero;

    public SpecialItemManager(Hero hero) {
        this.hero = hero;
    }

    public boolean useSpecialItemsIfNeeded() {
        Player self = hero.getGameMap().getCurrentPlayer();
        List<SupportItem> items = hero.getInventory().getListSupportItem();

        for (SupportItem item : items) {
            String id = item.getId().toUpperCase();

            try {
                // Sử dụng nếu máu cực thấp
                if (id.equals("ELIXIR_OF_LIFE") && self.getHealth() <= 40) {
                    hero.useItem(item.getId());
                    System.out.println("🧬 Used ELIXIR_OF_LIFE to trigger resurrection + immortality");
                    return true;
                }

                // Sử dụng nếu bị khống chế hoặc chuẩn bị combat gấp
                if (id.equals("ELIXIR") && self.getHealth() <= 40) {
                    hero.useItem(item.getId());
                    System.out.println("🧪 Used ELIXIR for control immunity");
                    return true;
                }

                // Sử dụng để tàng hình – tấn công bất ngờ hoặc rút lui
                if (id.equals("MAGIC") && self.getHealth() <= 40) {
                    hero.useItem(item.getId());
                    System.out.println("🪄 Used MAGIC for stealth");
                    return true;
                }

                // Sử dụng la bàn để gây choáng enemy gần nếu bị vây
                if (id.equals("COMPASS")) {
                    boolean surrounded = hero.getGameMap().getOtherPlayerInfo().stream()
                            .anyMatch(p -> p.getHealth() > 0 &&
                                    Math.abs(p.getX() - self.getX()) + Math.abs(p.getY() - self.getY()) <= 3);

                    if (surrounded) {
                        hero.useItem(item.getId());
                        System.out.println("🧭 Used COMPASS to stun nearby enemies");
                        return true;
                    }
                }

            } catch (IOException e) {
                System.err.println("❌ Failed to use special item " + id + ": " + e.getMessage());
            }
        }

        return false;
    }
}
