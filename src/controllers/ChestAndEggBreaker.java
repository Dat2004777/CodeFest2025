package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ChestAndEggBreaker {
    private final Hero hero;

    public ChestAndEggBreaker(Hero hero) {
        this.hero = hero;
    }

    public void breakNearbyChestOrEgg() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();

        List<Obstacle> targets = map.getListChests().stream()
                .filter(o -> o.getId().equals("CHEST") || o.getId().equals("DRAGON_EGG"))
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            System.out.println("No chests or dragon eggs found.");
            return;
        }

        Obstacle closest = targets.stream()
                .min(Comparator.comparingInt(o ->
                        Math.abs(o.getX() - self.getX()) + Math.abs(o.getY() - self.getY())))
                .orElse(null);

        if (closest == null) return;

        String direction = getDirection(self.getX(), self.getY(), closest.getX(), closest.getY());
        int dist = Math.abs(closest.getX() - self.getX()) + Math.abs(closest.getY() - self.getY());

        if (dist == 1 && !direction.isEmpty()) {
            try {
                hero.attack(direction);
                System.out.println("Attacking chest/egg at: " + direction);
            } catch (IOException e) {
                System.err.println("Failed to attack chest/egg: " + e.getMessage());
            }
        } else {
            Node from = new Node(self.getX(), self.getY());
            Node to = new Node(closest.getX(), closest.getY());
            String path = PathUtils.getShortestPath(map, List.of(), from, to, false);

            if (path != null && !path.isEmpty()) {
                try {
                    hero.move(path);
                    System.out.println("Moving to chest/egg: " + path);
                } catch (IOException e) {
                    System.err.println("Failed to move to chest/egg: " + e.getMessage());
                }
            }
        }
    }

    private String getDirection(int x1, int y1, int x2, int y2) {
        if (x1 == x2) return y2 < y1 ? "d" : "u";
        else if (y1 == y2) return x2 < x1 ? "r" : "l";
        return "";
    }
}
