package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.io.IOException;
import java.util.List;

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

        int sx = self.getX();
        int sy = self.getY();
        int tx = target.getX();
        int ty = target.getY();

        int range = extractGunRange(gun);

        GameMap map = hero.getGameMap();
        List<Obstacle> obstacles = map.getListObstacles();

        if (sx == tx) {
            int dy = Math.abs(sy - ty);
            if (dy > range) return false;

            int minY = Math.min(sy, ty);
            int maxY = Math.max(sy, ty);

            for (int y = minY + 1; y < maxY; y++) {
                if (isObstacleAt(sx, y, obstacles)) return false;
            }
            return true;
        }

        if (sy == ty) {
            int dx = Math.abs(sx - tx);
            if (dx > range) return false;

            int minX = Math.min(sx, tx);
            int maxX = Math.max(sx, tx);

            for (int x = minX + 1; x < maxX; x++) {
                if (isObstacleAt(x, sy, obstacles)) return false;
            }
            return true;
        }

        return false;
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

    private boolean isObstacleAt(int x, int y, List<Obstacle> obstacles) {
        for (Obstacle o : obstacles) {
            if (o.getX() == x && o.getY() == y) {
                String id = o.getId().toUpperCase();
                if (id.contains("WALL") || id.contains("ROCK") || id.contains("BLOCK") || id.contains("STATUE") ||
                        id.contains("TRAP") || id.contains("INDESTRUCTIBLE") || id.contains("CHEST") || id.contains("DRAGON_EGG")) {
                    return true;
                }
            }
        }
        return false;
    }

    private int extractGunRange(Weapon gun) {
        switch (gun.getId().toUpperCase()) {
            case "SCEPTER": return 12;
            case "CROSSBOW": return 5;
            case "RUBBER_GUN": return 6;
            default: return 3;
        }
    }
}
