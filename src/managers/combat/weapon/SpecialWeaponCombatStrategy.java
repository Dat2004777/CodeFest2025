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
        return special != null && special.getUseCount() > 0;
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        Weapon special = hero.getInventory().getSpecial();
        if (special == null) return false;

        int[] range = special.getRange(); // SDK mới
        int dx = Math.abs(self.getX() - target.getX());
        int dy = Math.abs(self.getY() - target.getY());

        // Hỗ trợ tấn công nếu trên cùng hàng/cột (theo quy định các vũ khí đặc biệt)
        if (dx * dy == 0) {
            int dist = dx + dy;
            return dist >= range[0] && dist <= range[1];
        }

        // Trường hợp BELL có thể đánh AOE vuông (dx <= 2 && dy <= 2)
        if ("BELL".equalsIgnoreCase(special.getId())) {
            return dx <= range[0] && dy <= range[1];
        }

        return false;
    }

    @Override
    public boolean attack(Player self, Player target) {
        Weapon special = hero.getInventory().getSpecial();
        if (special == null) return false;

        String id = special.getId().toUpperCase(Locale.ROOT);
        String dir = getDirection(self, target);

        try {
            hero.useSpecial(dir);
            System.out.println("✨ Using special weapon [" + id + "] towards: " + dir);
            return true;
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
