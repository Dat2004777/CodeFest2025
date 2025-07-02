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
            int x = item.getX(), y = item.getY();

            // ❌ Bỏ qua item nếu bị người chơi khác đứng lên
            if (isOccupiedByOtherPlayer(map, x, y)) continue;

            if (isOverlappingItemTile(map, item.getX(), item.getY())) continue; // 🔥 Né ô có 2 item

            // ✅ Chỉ xét item trong vùng an toàn
            if (!PathUtils.checkInsideSafeArea(new Node(x, y), safeZone, mapSize)) continue;

            int dist = Math.abs(x - player.getX()) + Math.abs(y - player.getY());
            if (dist < minDist) {
                minDist = dist;
                closest = item;
            }
        }

        return closest;
    }

    private boolean isOccupiedByOtherPlayer(GameMap map, int x, int y) {
        for (Player p : map.getOtherPlayerInfo()) {
            if (p.getX() == x && p.getY() == y && p.getHealth() > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlappingItemTile(GameMap map, int x, int y) {
        int count = 0;

        // Đếm số lượng item nằm tại ô (x, y)
        count += map.getAllGun().stream().filter(i -> i.getX() == x && i.getY() == y).count();
        count += map.getAllThrowable().stream().filter(i -> i.getX() == x && i.getY() == y).count();
        count += map.getAllSpecial().stream().filter(i -> i.getX() == x && i.getY() == y).count();
        count += map.getListHealingItems().stream().filter(i -> i.getX() == x && i.getY() == y).count();
        count += map.getListArmors().stream().filter(i -> i.getX() == x && i.getY() == y).count();

        // Nếu có 2 item trở lên → là chồng lên nhau
        return count >= 2;
    }

    protected abstract List<T> getCandidateItems(GameMap map);

    protected abstract String getItemName();
}
