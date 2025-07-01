package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;

public class ThrowableCombatStrategy extends WeaponCombatStrategy {

    public ThrowableCombatStrategy(Hero hero) {
        super(hero);
    }

    @Override
    public boolean isUsable() {
        return hero.getInventory().getThrowable() != null;
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        Weapon throwable = hero.getInventory().getThrowable();
        if (throwable == null) return false;

        int dx = Math.abs(self.getX() - target.getX());
        int dy = Math.abs(self.getY() - target.getY());

        int range = extractThrowableRange(throwable);

        return ((dx == 0 && dy >= 1 && dy <= range) || (dy == 0 && dx >= 1 && dx <= range));
    }

    @Override
    public boolean attack(Player self, Player target) {
        Weapon throwable = hero.getInventory().getThrowable();
        if (throwable == null) return false;

        String dir = getDirection(self, target);
        int range = extractThrowableRange(throwable);

        if (!dir.isEmpty()) {
            try {
                hero.throwItem(dir, range); // Cần truyền cả direction và khoảng cách
                System.out.println("🧨 Throwing " + throwable.getId() + " at: " + dir + " (range: " + range + ")");
                return true;
            } catch (IOException e) {
                System.err.println("⚠️ Throwable failed: " + e.getMessage());
            }
        }

        return false;
    }

    private String getDirection(Player from, Player to) {
        if (from.getX() == to.getX()) return to.getY() < from.getY() ? "d" : "u";
        if (from.getY() == to.getY()) return to.getX() < from.getX() ? "l" : "r";
        return "";
    }

    private int extractThrowableRange(Weapon throwable) {
        switch (throwable.getId().toUpperCase()) {
            case "SMOKE": return 3;
            case "SEED": return 5;
            case "BANANA":
            case "METEORITE_FRAGMENT":
            case "CRYSTAL":
                return 6;
            default: return 4; // fallback
        }
    }
}