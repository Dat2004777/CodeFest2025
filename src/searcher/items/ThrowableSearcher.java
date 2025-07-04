package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.weapon.Weapon;


import java.util.List;

public class ThrowableSearcher extends ItemSearcher<Weapon> {

    public ThrowableSearcher(Hero hero) {
        super(hero);
    }

    @Override
    protected List<Weapon> getCandidateItems(GameMap map) {
        return map.getAllThrowable().stream()
                .filter(w -> {
                    String id = w.getId().toUpperCase();
                    return !id.contains("SMOKE");
                })
                .toList();
    }

    @Override
    protected String getItemName() {
        return "throwable";
    }
}
