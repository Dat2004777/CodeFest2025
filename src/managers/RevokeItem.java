package managers;

import jsclub.codefest.sdk.Hero;
import jsclub.codefest.sdk.model.armors.Armor;


import java.io.IOException;

public class RevokeItem {
    private final Hero hero;

    public RevokeItem(Hero hero) {
        this.hero = hero;
    }

    public void dropCurrentArmorIfAny() {
        Armor armor = hero.getInventory().getArmor();
        if (armor != null) {
            try {
                hero.revokeItem(armor.getId());
                System.out.println("🛡️ Dropped armor: " + armor.getId());
            } catch (IOException e) {
                System.err.println("❌ Failed to drop armor: " + e.getMessage());
            }
        }
    }

    public void dropCurrentHelmetIfAny() {
        Armor helmet = hero.getInventory().getHelmet();
        if (helmet != null) {
            try {
                hero.revokeItem(helmet.getId());
                System.out.println("⛑️ Dropped helmet: " + helmet.getId());
            } catch (IOException e) {
                System.err.println("❌ Failed to drop helmet: " + e.getMessage());
            }
        }
    }
}
