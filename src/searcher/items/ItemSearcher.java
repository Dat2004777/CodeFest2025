package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.algorithm.PathUtils;
import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;
import utils.DodgeUtils;

import java.io.IOException;
import java.util.List;

public abstract class ItemSearcher<T extends Element> {
    protected final Hero hero;

    public ItemSearcher(Hero hero) {
        this.hero = hero;
    }

    public boolean searchAndPickup(GameMap map, Player player) {
        T closest = findClosestItem(map, player);
        if (closest == null) return false;

        int dist = Math.abs(closest.getX() - player.getX()) + Math.abs(closest.getY() - player.getY());

        if (dist == 0) {
            try {
                hero.pickupItem();
                System.out.println("✅ Picked up " + getItemName() + " at: " + closest.getX() + "," + closest.getY());
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to pick up " + getItemName() + ": " + e.getMessage());
            }
        } else {
            return moveTo(map, player);
        }

        return false;
    }

    public boolean moveTo(GameMap map, Player player) {
        T closest = findClosestItem(map, player);
        if (closest == null) return false;

        Node from = new Node(player.getX(), player.getY());
        Node to = new Node(closest.getX(), closest.getY());
        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);

        String path = PathUtils.getShortestPath(map, avoid, from, to, false);
        if (path != null && !path.isEmpty()) {
            try {
                hero.move(path);
                System.out.println("➡️ Moving to " + getItemName() + " at (" + to.getX() + "," + to.getY() + "): " + path);
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to move to " + getItemName() + ": " + e.getMessage());
            }
        }
        return false;
    }

    public int getClosestDistance(GameMap map, Player player) {
        T closest = findClosestItem(map, player);
        if (closest == null) return Integer.MAX_VALUE;
        return Math.abs(closest.getX() - player.getX()) + Math.abs(closest.getY() - player.getY());
    }

    private T findClosestItem(GameMap map, Player player) {
        List<T> items = getCandidateItems(map);
        if (items == null || items.isEmpty()) return null;

        int mapSize = map.getMapSize();
        int safeZone = map.getSafeZone();

        T closest = null;
        int minDist = Integer.MAX_VALUE;

        for (T item : items) {
            Node node = new Node(item.getX(), item.getY());
            if (!PathUtils.checkInsideSafeArea(node, safeZone, mapSize)) continue;

            int dist = Math.abs(item.getX() - player.getX()) + Math.abs(item.getY() - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = item;
            }
        }

        return closest;
    }

    protected abstract List<T> getCandidateItems(GameMap map);

    protected abstract String getItemName();
}
