package utils;

import jsclub.codefest.sdk.base.Node;
import jsclub.codefest.sdk.model.GameMap;
import jsclub.codefest.sdk.model.players.Player;

import java.util.List;

public class EnemyUtils {

    /**
     * Tính khoảng cách ngắn nhất từ player hiện tại tới một enemy còn sống.
     *
     * @param map Bản đồ game hiện tại
     * @param self Player hiện tại (bot)
     * @return khoảng cách Manhattan tới enemy gần nhất, hoặc Integer.MAX_VALUE nếu không có
     */
    public static int getClosestEnemyDistance(GameMap map, Player self) {
        List<Player> enemies = map.getOtherPlayerInfo();

        return enemies.stream()
                .filter(enemy -> enemy.getHealth() > 0)
                .mapToInt(enemy -> Math.abs(enemy.getX() - self.getX()) + Math.abs(enemy.getY() - self.getY()))
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    /**
     * Lấy enemy gần nhất (không chỉ khoảng cách).
     */
    public static Player getClosestEnemy(GameMap map, Player self) {
        List<Player> enemies = map.getOtherPlayerInfo();

        return enemies.stream()
                .filter(enemy -> enemy.getHealth() > 0)
                .min((a, b) -> {
                    int distA = Math.abs(a.getX() - self.getX()) + Math.abs(a.getY() - self.getY());
                    int distB = Math.abs(b.getX() - self.getX()) + Math.abs(b.getY() - self.getY());
                    return Integer.compare(distA, distB);
                }).orElse(null);
    }
}
