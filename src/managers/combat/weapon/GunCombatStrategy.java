package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;

public class GunCombatStrategy extends WeaponCombatStrategy {

    public GunCombatStrategy(Hero hero) {
        super(hero);
    }

    @Override
    public boolean isUsable() {
        return hero.getInventory().getGun() != null;
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        Weapon gun = hero.getInventory().getGun();
        if (gun == null) return false;

        int dx = Math.abs(self.getX() - target.getX());
        int dy = Math.abs(self.getY() - target.getY());

        int range = extractGunRange(gun);

        return ((dx == 0 && dy >= 1 && dy <= range) || (dy == 0 && dx >= 1 && dx <= range));
    }

    @Override
    public boolean attack(Player self, Player target) {
        String dir = getDirection(self, target);
        if (!dir.isEmpty()) {
            try {
                hero.shoot(dir);
                System.out.println("🔫 Shooting with " + hero.getInventory().getGun().getId() + " at: " + dir);
                return true;
            } catch (IOException e) {
                System.err.println("⚠️ Shooting failed: " + e.getMessage());
            }
        }
        return false;
    }

    private String getDirection(Player from, Player to) {
        if (from.getX() == to.getX()) return to.getY() < from.getY() ? "d" : "u";
        if (from.getY() == to.getY()) return to.getX() < from.getX() ? "l" : "r";
        return "";
    }

    private int extractGunRange(Weapon gun) {
        switch (gun.getId().toUpperCase()) {
            case "SCEPTER": return 12;
            case "CROSSBOW": return 8;
            case "RUBBER_GUN": return 6;
            case "SHOTGUN": return 2;
            default: return 3; // fallback an toàn
        }
    }
}
