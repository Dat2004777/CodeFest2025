package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;

public class MeleeCombatStrategy extends WeaponCombatStrategy {

    public MeleeCombatStrategy(Hero hero) {
        super(hero);
    }

    @Override
    public boolean isUsable() {
        Weapon melee = hero.getInventory().getMelee();
        return melee != null;
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        Weapon melee = hero.getInventory().getMelee();
        if (melee == null) return false;

        int[] range = melee.getRange(); // SDK mới cung cấp range: [min, max]
        int sx = self.getX(), sy = self.getY();
        int tx = target.getX(), ty = target.getY();

        int dx = Math.abs(sx - tx);
        int dy = Math.abs(sy - ty);

        // Chỉ xét nếu cùng hàng hoặc cùng cột
        if (dx * dy != 0) return false;

        int dist = dx + dy;
        return dist >= range[0] && dist <= range[1];
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
