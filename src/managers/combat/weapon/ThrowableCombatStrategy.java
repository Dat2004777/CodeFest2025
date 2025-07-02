package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;
import java.util.List;

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

        boolean aligned = (dx == 0 && dy >= 1 && dy <= range) || (dy == 0 && dx >= 1 && dx <= range);

        return aligned && isPathClear(self, target);
    }

    @Override
    public boolean attack(Player self, Player target) {
        Weapon throwable = hero.getInventory().getThrowable();
        if (throwable == null) return false;

        String dir = getDirection(self, target);
        int range = extractThrowableRange(throwable);

        if (!dir.isEmpty() && isPathClear(self, target)) {
            try {
                hero.throwItem(dir, range);
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
            default: return 4;
        }
    }

    private boolean isPathClear(Player from, Player to) {
        GameMap map = hero.getGameMap();
        List<Obstacle> obstacles = map.getListObstacles();

        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();

        if (x1 == x2) {
            // Ném theo chiều dọc
            int start = Math.min(y1, y2) + 1;
            int end = Math.max(y1, y2);
            for (int y = start; y < end; y++) {
                if (isObstacleAt(x1, y, obstacles)) return false;
            }
        } else if (y1 == y2) {
            // Ném theo chiều ngang
            int start = Math.min(x1, x2) + 1;
            int end = Math.max(x1, x2);
            for (int x = start; x < end; x++) {
                if (isObstacleAt(x, y1, obstacles)) return false;
            }
        }

        return true;
    }

    private boolean isObstacleAt(int x, int y, List<Obstacle> obstacles) {
        for (Obstacle o : obstacles) {
            if (o.getX() == x && o.getY() == y) {
                String id = o.getId().toUpperCase();
                if (id.contains("WALL") || id.contains("ROCK") || id.contains("BLOCK") || id.contains("STATUE")
                        || id.contains("TRAP") || id.contains("INDESTRUCTIBLE") || id.contains("CHEST")
                        || id.contains("DRAGON_EGG")) {
                    return true;
                }
            }
        }
        return false;
    }
}
