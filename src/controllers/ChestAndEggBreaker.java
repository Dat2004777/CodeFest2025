package controllers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.obstacles.Obstacle;
import jsclub.codefest.sdk.model.players.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChestAndEggBreaker {
    private final Hero hero;

    public ChestAndEggBreaker(Hero hero) {
        this.hero = hero;
    }

    /**
     * Trả về true chỉ khi đã tấn công rương/trứng ở gần (không phải khi di chuyển).
     */
    public boolean breakIfAdjacent() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        List<Obstacle> chests = map.getListChests();
        List<Obstacle> targets = new ArrayList<>();

        for (Obstacle o : chests) {
            String id = o.getId();
            if (id.startsWith("CHEST") || id.startsWith("DRAGON_EGG")) {
                targets.add(o);
            }
        }

        if (targets.isEmpty()) return false;

        // Tìm mục tiêu gần nhất
        Obstacle closest = targets.stream()
                .min(Comparator.comparingInt(o ->
                        Math.abs(o.getX() - self.getX()) + Math.abs(o.getY() - self.getY())))
                .orElse(null);

        if (closest == null) return false;

        int dist = Math.abs(closest.getX() - self.getX()) + Math.abs(closest.getY() - self.getY());
        String direction = getDirection(self.getX(), self.getY(), closest.getX(), closest.getY());

        if (dist == 1 && !direction.isEmpty()) {
            try {
                hero.attack(direction);
                System.out.println("Attacking chest/egg at: " + direction);
                return true;
            } catch (IOException e) {
                System.err.println("Failed to attack chest/egg: " + e.getMessage());
            }
        }

        // Nếu không gần → KHÔNG return true (để các controller khác có thể được gọi)
        return false;
    }

    // Nếu bạn vẫn muốn di chuyển về phía chest/egg thì gọi hàm này riêng
    public void moveToChestOrEgg() {
        GameMap map = hero.getGameMap();
        Player self = map.getCurrentPlayer();
        List<Obstacle> chests = map.getListChests();
        List<Obstacle> targets = new ArrayList<>();

        for (Obstacle o : chests) {
            String id = o.getId();
            if (id.startsWith("CHEST") || id.startsWith("DRAGON_EGG")) {
                targets.add(o);
            }
        }

        if (targets.isEmpty()) return;

        Obstacle closest = targets.stream()
                .min(Comparator.comparingInt(o ->
                        Math.abs(o.getX() - self.getX()) + Math.abs(o.getY() - self.getY())))
                .orElse(null);

        if (closest == null) return;

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

    private String getDirection(int x1, int y1, int x2, int y2) {
        if (x1 == x2) return y2 < y1 ? "d" : "u";
        else if (y1 == y2) return x2 < x1 ? "l" : "r";
        return "";
    }
}

