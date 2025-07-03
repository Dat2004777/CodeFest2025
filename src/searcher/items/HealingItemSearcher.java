package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.support_items.SupportItem;

import java.util.List;

public class HealingItemSearcher extends ItemSearcher<SupportItem> {

    public HealingItemSearcher(Hero hero) {
        super(hero);
    }

    @Override
    protected List<SupportItem> getCandidateItems(GameMap map) {
        return map.getListSupportItems();
    }

    @Override
    protected String getItemName() {
        return "healing item";
    }
}
