//package controllers;
//
//import jsclub.codefest.sdk.Hero;
//import jsclub.codefest.sdk.model.players.Player;
//import jsclub.codefest.sdk.model.equipments.HealingItem;
//
//import java.util.Comparator;
//import java.util.List;
//
//public class healingManager {
//    private final Hero hero;
//    private static final float HP_THRESHOLD = 50f;
//
//    public healingManager(Hero hero) {
//        this.hero = hero;
//    }
//
//    public boolean shouldHeal(Player player) {
//        return player.getHealth() < HP_THRESHOLD && !hero.getInventory().getListHealingItem().isEmpty();
//    }
//
//    public void useBestHealingItem() throws IOException {
//        List<HealingItem> items = hero.getInventory().getListHealingItem();
//
//        if (items == null || items.isEmpty()) return;
//
//        // Dùng luôn item đầu tiên (ưu tiên hàng đầu túi)
//        hero.useItem(get);
//    }
//
//
//}
