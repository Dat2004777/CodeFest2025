package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.healing_items.HealingItem;

import java.util.List;

public class HealingItemSearcher extends ItemSearcher<HealingItem> {

    public HealingItemSearcher(Hero hero) {
        super(hero);
    }

    @Override
    protected List<HealingItem> getCandidateItems(GameMap map) {
        return map.getListHealingItems();
    }

    @Override
    protected String getItemName() {
        return "healing item";
    }
}
