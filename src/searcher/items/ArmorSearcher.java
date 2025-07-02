package searcher.items;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.Element;
import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.armors.Armor;
import jsclub.codefest.sdk.model.players.Player;

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

    public String isStandingOnArmorOrHelmet(GameMap map, Player player) {
        Element item = map.getElementByIndex(player.getX(), player.getY());

        ElementType type = item.getType();
        if (type == ElementType.ARMOR) {
            return "ARMOR";
        } else if (type == ElementType.HELMET) {
            return  "HELMET";
        }
        return "";
    }
}
