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
        Weapon throwable = hero.getInventory().getThrowable();
        return throwable != null && throwable.getUseCount() > 0;
    }

    @Override
    public boolean isInRange(Player self, Player target) {
        Weapon throwable = hero.getInventory().getThrowable();
        if (throwable == null) return false;

        int[] range = throwable.getRange();
        int dx = Math.abs(self.getX() - target.getX());
        int dy = Math.abs(self.getY() - target.getY());

        return (dx == 0 && dy >= range[0] && dy <= range[1]) ||
                (dy == 0 && dx >= range[0] && dx <= range[1]);
    }

    @Override
    public boolean attack(Player self, Player target) {
        Weapon throwable = hero.getInventory().getThrowable();
        if (throwable == null) return false;

        String dir = getDirection(self, target);

        if (!dir.isEmpty()) {
            try {
                hero.throwItem(dir);
                System.out.println("🧨 Throwing " + throwable.getId() + " at: " + dir);
                return true;
            } catch (IOException e) {
                System.err.println("⚠️ Throwable attack failed: " + e.getMessage());
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
