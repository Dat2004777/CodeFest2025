package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.weapon.Weapon;


import java.util.List;

public class MeleeSearcher extends ItemSearcher<Weapon> {

    public MeleeSearcher(Hero hero) {
        super(hero);
    }

    @Override
    protected List<Weapon> getCandidateItems(GameMap map) {
        return map.getAllMelee();
    }

    @Override
    protected String getItemName() {
        return "melee";
    }
}
