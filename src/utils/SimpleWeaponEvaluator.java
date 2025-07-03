package utils;

import java.util.Map;
import jsclub.codefest.sdk.model.Element;
public class SimpleWeaponEvaluator<T extends Element> implements WeaponEvaluator<T> {
    private final Map<String, Integer> priorityMap;

    public SimpleWeaponEvaluator(Map<String, Integer> priorityMap) {
        this.priorityMap = priorityMap;
    }

    @Override
    public int evaluate(T weapon) {
        return priorityMap.getOrDefault(weapon.getId(), 0);
    }
}

