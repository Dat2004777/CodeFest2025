package managers.combat.weapon;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.players.Player;

/**
 * Chiến lược chiến đấu cho từng loại vũ khí.
 */
public abstract class WeaponCombatStrategy {
    protected final Hero hero;

    public WeaponCombatStrategy(Hero hero) {
        this.hero = hero;
    }

    /**
     * Điều kiện để vũ khí này có thể dùng được.
     */
    public abstract boolean isUsable();

    /**
     * Khoảng cách có thể dùng được vũ khí này (ví dụ: dist == 1, dist == 5, v.v.)
     */
    public abstract boolean isInRange(Player self, Player target);

    /**
     * Gọi hành động tấn công thật sự (shoot, attack, throwItem, useSpecial).
     */
    public abstract boolean attack(Player self, Player target);
}
