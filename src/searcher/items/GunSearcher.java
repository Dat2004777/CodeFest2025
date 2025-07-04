package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.util.List;

public class GunSearcher extends ItemSearcher<Weapon> {

    public GunSearcher(Hero hero) {
        super(hero);
    }

    @Override
    protected List<Weapon> getCandidateItems(GameMap map) {
        return map.getAllGun();
    }

    @Override
    protected String getItemName() {
        return "gun";
    }
}
