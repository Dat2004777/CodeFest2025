package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;

public class ShotgunCombatStrategy extends WeaponCombatStrategy {

    public ShotgunCombatStrategy(Hero hero) {
        super(hero);
    }

    @Override
    public boolean isUsable() {
        Weapon gun = hero.getInventory().getGun();
        return gun != null && "SHOTGUN".equalsIgnoreCase(gun.getId());
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        int dx = Math.abs(self.getX() - target.getX());
        int dy = Math.abs(self.getY() - target.getY());
        return dx + dy == 1; // phải là ô kế bên
    }

    @Override
    public boolean attack(Player self, Player target) {
        String dir = getDirection(self, target);
        if (!dir.isEmpty()) {
            try {
                hero.shoot(dir);
                System.out.println("💥 Shotgun blast at: " + dir);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Shotgun attack failed: " + e.getMessage());
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
