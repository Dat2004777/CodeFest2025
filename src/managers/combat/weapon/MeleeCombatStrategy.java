package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;
import java.util.Set;

public class MeleeCombatStrategy extends WeaponCombatStrategy {
    private static final Set<String> CLOSE_RANGE_ONLY = Set.of("BONE");

    public MeleeCombatStrategy(Hero hero) {
        super(hero);
    }

    @Override
    public boolean isUsable() {
        Weapon melee = hero.getInventory().getMelee();
        return melee != null && !"HAND".equalsIgnoreCase(melee.getId());
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        Weapon melee = hero.getInventory().getMelee();
        if (melee == null) return false;

        int dx = Math.abs(self.getX() - target.getX());
        int dy = Math.abs(self.getY() - target.getY());
        int dist = dx + dy;

        if (CLOSE_RANGE_ONLY.contains(melee.getId())) {
            return dist == 1;
        }

        // Nếu vũ khí có tầm xa: từ 1 đến 3 cells trên cùng hàng/cột
        return ((dx == 0 && dy >= 1 && dy <= 3) || (dy == 0 && dx >= 1 && dx <= 3));
    }

    @Override
    public boolean attack(Player self, Player target) {
        String dir = getDirection(self, target);
        if (!dir.isEmpty()) {
            try {
                hero.attack(dir);
                System.out.println("🗡️ Attacking with melee (" + hero.getInventory().getMelee().getId() + ") at: " + dir);
                return true;
            } catch (IOException e) {
                System.err.println("⚠️ Melee attack failed: " + e.getMessage());
            }
        }
        return false;
    }

    private String getDirection(Player from, Player to) {
        if (from.getX() == to.getX()) return to.getY() < from.getY() ? "d" : "u";
        if (from.getY() == to.getY()) return to.getX() < from.getX() ? "l" : "r";
        return "";
    }
}
