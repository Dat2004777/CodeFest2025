package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.weapon.Weapon;


import java.util.List;

public class SpecialSearcher extends ItemSearcher<Weapon> {

    public SpecialSearcher(Hero hero) {
        super(hero);
    }

    @Override
    protected List<Weapon> getCandidateItems(GameMap map) {
        return map.getAllSpecial();
    }

    @Override
    protected String getItemName() {
        return "special";
    }
}
