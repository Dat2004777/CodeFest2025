package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;
import java.util.Locale;

/**
 * Chiến lược dùng chung cho tất cả vũ khí đặc biệt: ROPE, BELL, SAHUR_BAT.
 */
public class SpecialWeaponCombatStrategy extends WeaponCombatStrategy {

    public SpecialWeaponCombatStrategy(Hero hero) {
        super(hero);
    }

    @Override
    public boolean isUsable() {
        Weapon special = hero.getInventory().getSpecial();
        return special != null && special.getUseCounts() > 0;
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        Weapon special = hero.getInventory().getSpecial();
        if (special == null) return false;

        String id = special.getId().toUpperCase(Locale.ROOT);
        int dx = Math.abs(self.getX() - target.getX());
        int dy = Math.abs(self.getY() - target.getY());
        int dist = dx + dy;

        switch (id) {
            case "ROPE":
                return (dx == 0 || dy == 0) && dist == 6;
            case "BELL":
                return dx <= 2 && dy <= 2; // 5x5 square
            case "SAHUR_BAT":
                return dist == 1; // melee
            default:
                return false;
        }
    }

    @Override
    public boolean attack(Player self, Player target) {
        Weapon special = hero.getInventory().getSpecial();
        if (special == null) return false;

        String id = special.getId().toUpperCase(Locale.ROOT);
        String dir = getDirection(self, target);

        try {
            switch (id) {
                case "ROPE":
                    hero.useSpecial(dir);
                    System.out.println("🪢 Using ROPE on " + dir);
                    return true;
                case "BELL":
                    hero.useSpecial(dir); // AOE center
                    System.out.println("🔔 Using BELL in area at " + dir);
                    return true;
                case "SAHUR_BAT":
                    hero.useSpecial(dir); // melee
                    System.out.println("🦇 Using SAHUR_BAT at " + dir);
                    return true;
                default:
                    System.out.println("⚠️ Unknown special weapon: " + id);
                    return false;
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to use special weapon " + id + ": " + e.getMessage());
            return false;
        }
    }

    private String getDirection(Player self, Player target) {
        if (self.getX() == target.getX()) return target.getY() < self.getY() ? "d" : "u";
        if (self.getY() == target.getY()) return target.getX() < self.getX() ? "l" : "r";
        return "u"; // fallback
    }
}
