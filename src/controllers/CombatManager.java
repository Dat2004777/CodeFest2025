package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public class CombatManager {
    private final Hero hero;

    public CombatManager(Hero hero) {
        this.hero = hero;
    }

    public boolean engageEnemy() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        List<Player> opponents = map.getOtherPlayerInfo();

        if (opponents.isEmpty()) {
            System.out.println("No other players found.");
            return false;
        }

        Player closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Player p : opponents) {
            if (p.getHealth() <= 0) continue;
            int dist = Math.abs(p.getX() - self.getX()) + Math.abs(p.getY() - self.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = p;
            }
        }

        if (closest == null) return false;

        String direction = getDirection(self.getX(), self.getY(), closest.getX(), closest.getY());

        if (minDist == 1 && !direction.isEmpty()) {
            try {
                if (hero.getInventory().getGun() != null) {
                    hero.shoot(direction);
                    System.out.println("Shooting at player: " + direction);
                } else {
                    hero.attack(direction);
                    System.out.println("Attacking player: " + direction);
                }
                return true;
            } catch (IOException e) {
                System.err.println("Combat failed: " + e.getMessage());
            }
        } else {
            Node from = new Node(self.getX(), self.getY());
            Node to = new Node(closest.getX(), closest.getY());
            List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

            String path = PathUtils.getShortestPath(map, avoid, from, to, false);

            if (path != null && !path.isEmpty()) {
                try {
                    hero.move(path);
                    System.out.println("Moving towards player: " + path);
                    return true;
                } catch (IOException e) {
                    System.err.println("Failed to move to player: " + e.getMessage());
                }
            }
        }

        return false;
    }

    private String getDirection(int x1, int y1, int x2, int y2) {
        if (x1 == x2) return y2 < y1 ? "d" : y2 > y1 ? "u" : "";
        if (y1 == y2) return x2 < x1 ? "l" : x2 > x1 ? "r" : "";
        return "";
    }

    public int getClosestEnemyDistance(GameMap map, Player self) {
        return map.getOtherPlayerInfo().stream()
                .filter(p -> p.getHealth() > 0)
                .mapToInt(p -> Math.abs(p.getX() - self.getX()) + Math.abs(p.getY() - self.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }
}
