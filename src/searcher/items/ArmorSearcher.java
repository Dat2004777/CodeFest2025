package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.armors.Armor;

import java.util.List;

public class ArmorSearcher extends ItemSearcher<Armor> {

    public ArmorSearcher(Hero hero) {
        super(hero);
    }

    @Override
    protected List<Armor> getCandidateItems(GameMap map) {
        return map.getListArmors();
    }

    @Override
    protected String getItemName() {
        return "armor";
    }
}
