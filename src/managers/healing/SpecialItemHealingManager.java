package managers.healing;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.support_items.SupportItem;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpecialItemHealingManager {
    private final Hero hero;

    public SpecialItemHealingManager(Hero hero) {
        this.hero = hero;
    }

    public boolean useSpecialItemsIfNeeded() {
        Player self = hero.getGameMap().getCurrentPlayer();
        Float hp = self.getHealth();
        if (hp == null || hp <= 0) return false;

        List<SupportItem> inventoryItems = new ArrayList<>(hero.getInventory().getListSupportItem()); // tránh ConcurrentModificationException

        for (SupportItem item : inventoryItems) {
            String id = item.getId().toUpperCase();

            try {
                switch (id) {
                    case "ELIXIR_OF_LIFE":
                        if (hp <= 40) {
                            hero.useItem(item.getId());
                            System.out.println("🧬 Used ELIXIR_OF_LIFE for resurrection + temporary immortality");
                            return true;
                        }
                        break;

                    case "ELIXIR":
                        if (hp <= 40) {
                            hero.useItem(item.getId());
                            System.out.println("🧪 Used ELIXIR for control immunity");
                            return true;
                        }
                        break;

                    case "MAGIC":
                        if (hp <= 40) {
                            hero.useItem(item.getId());
                            System.out.println("🪄 Used MAGIC for stealth");
                            return true;
                        }
                        break;

                    case "COMPASS":
                        long nearbyEnemies = hero.getGameMap().getOtherPlayerInfo().stream()
                                .filter(p -> p.getHealth() != null && p.getHealth() > 0)
                                .filter(p -> Math.abs(p.getX() - self.getX()) + Math.abs(p.getY() - self.getY()) <= 3)
                                .count();

                        if (nearbyEnemies >= 2) {
                            hero.useItem(item.getId());
                            System.out.println("🧭 Used COMPASS to stun " + nearbyEnemies + " nearby enemies");
                            return true;
                        }
                        break;

                    default:
                        break;
                }

            } catch (IOException e) {
                System.err.println("❌ Failed to use special item " + id + ": " + e.getMessage());
            }
        }

        return false;
    }
}
