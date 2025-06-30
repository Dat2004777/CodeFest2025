//package controllers;
//
//import jsclub.codefest.sdk.Hero;
//import jsclub.codefest.sdk.base.Node;
//import jsclub.codefest.sdk.algorithm.PathUtils;
//import jsclub.codefest.sdk.model.Element;
//import jsclub.codefest.sdk.model.ElementType;
//import jsclub.codefest.sdk.model.GameMap;
//import jsclub.codefest.sdk.model.equipments.Armor;
//import jsclub.codefest.sdk.model.players.Player;
//import utils.DodgeUtils;
//
//import java.io.IOException;
//import java.util.List;
//
//public class HelmetSearcher {
//    private final Hero hero;
//
//    public HelmetSearcher(Hero hero) {
//        this.hero = hero;
//    }
//
//    public boolean searchAndPickup(GameMap map, Player player) {
//        List<Element> elements = map.();
//        int mapSize = map.getMapSize();
//        int safeZone = map.getSafeZone();
//
//        Element closest = null;
//        int minDist = Integer.MAX_VALUE;
//
//        for (Element e : elements) {
//            if (e.getType() != ElementType.HELMET) continue;
//
//            Node node = new Node(e.getX(), e.getY());
//            if (!PathUtils.checkInsideSafeArea(node, safeZone, mapSize)) continue;
//
//            int dist = Math.abs(e.getX() - player.getX()) + Math.abs(e.getY() - player.getY());
//            if (dist < minDist) {
//                minDist = dist;
//                closest = e;
//            }
//        }
//
//        if (closest == null) {
//            System.out.println("🪖 No helmet in safe zone.");
//            return false;
//        }
//
//        if (player.getX() == closest.getX() && player.getY() == closest.getY()) {
//            try {
//                hero.pickupItem();
//                System.out.println("✅ Picked up helmet at: (" + closest.getX() + "," + closest.getY() + ")");
//                return true;
//            } catch (IOException e) {
//                System.err.println("❌ Failed to pick up helmet: " + e.getMessage());
//            }
//        } else {
//            return moveTo(closest.getX(), closest.getY(), player, map);
//        }
//
//        return false;
//    }
//
//    private boolean moveTo(int tx, int ty, Player player, GameMap map) {
//        Node from = new Node(player.getX(), player.getY());
//        Node to = new Node(tx, ty);
//        List<Node> avoid = DodgeUtils.getUnwalkableNodes(map);
//
//        String path = PathUtils.getShortestPath(map, avoid, from, to, false);
//        if (path != null && !path.isEmpty()) {
//            try {
//                hero.move(path);
//                System.out.println("➡️ Moving to helmet at (" + tx + "," + ty + ") via: " + path);
//                return true;
//            } catch (IOException e) {
//                System.err.println("❌ Failed to move to helmet: " + e.getMessage());
//            }
//        } else {
//            System.out.println("⚠️ No valid path to helmet.");
//        }
//
//        return false;
//    }
//}
