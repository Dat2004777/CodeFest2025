import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.Hero;

import java.io.IOException;


public class Bot4 {
    private static final String SERVER_URL = "https://cf25-server.jsclub.dev";
    private static final String GAME_ID = "102027";
    private static final String PLAYER_NAME = "NguyenDatJ";
    private static final String SECRET_KEY = "sk-vCZV6LAFTLW8hz9S1XVRcQ:DUZ9GT9aiszq3Yckbr3gvBnhGEp5-tzPNKRRQE2OI6h-hYRc3Knlt3HPVuUMx-HE35D_PanzZkPoU9TW6-L1Cg";


    public static void main(String[] args) throws IOException {
        Hero hero = new Hero(GAME_ID, PLAYER_NAME, SECRET_KEY);
        Emitter.Listener onMapUpdate = new MapUpdateListener(hero);

        hero.setOnMapUpdate(onMapUpdate);
        hero.start(SERVER_URL);
    }
}