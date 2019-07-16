package massim.simulation.game;

import massim.config.TeamConfig;
import massim.protocol.data.Position;
import massim.protocol.messages.scenario.Actions;
import massim.simulation.game.environment.Terrain;
import massim.util.RNG;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class GameStateTest {

    private static final JSONObject CONFIG = new JSONObject("{\n" +
            "      \"steps\" : 10,\n" +
            "      \"randomSeed\" : 17,\n" +
            "      \"randomFail\" : 1,\n" +
            "      \"entities\" : [{\"standard\" : 10}],\n" +
            "      \"grid\" : {\n" +
            "        \"height\" : 20,\n" +
            "        \"width\" : 20\n" +
            "      },\n" +
            "      \"attachLimit\" : 3,\n" +
            "      \"blockTypes\" : [3, 3],\n" +
            "      \"dispensers\" : [2, 3],\n" +
            "      \"tasks\" : {\n" +
            "        \"size\" : [2, 4],\n" +
            "        \"duration\" : [100, 200],\n" +
            "        \"probability\" : 1\n" +
            "      }\n" +
            "    }");

    private GameState state;

    @org.junit.Before
    public void setUp() throws Exception {
        RNG.initialize(17);

        var team = new TeamConfig("A");
        for (var i = 1; i <= 10; i++) team.addAgent("A" + i, "1");
        state = new GameState(CONFIG, Set.of(team));
    }

    @org.junit.Test
    public void handleRequestAction() {
        var blockTypes = state.getBlockTypes();
        var dispenserPos = Position.of(3, 3);
        Entity a1 = state.getEntityByName("A1");
        assert a1 != null;
        assert state.createDispenser(dispenserPos, blockTypes.iterator().next());
        assert state.teleport("A1", dispenserPos.moved("s", 2));

        // too far away -> fail
        assert state.handleRequestAction(a1, "n").equals(Actions.RESULT_F_TARGET);
        //move closer
        assert state.handleMoveAction(a1, "n").equals(Actions.RESULT_SUCCESS);
        // wrong param -> fail
        assert state.handleRequestAction(a1, "w").equals(Actions.RESULT_F_TARGET);
        // everything correct -> success
        assert state.handleRequestAction(a1, "n").equals(Actions.RESULT_SUCCESS);
        // repeat -> fail
        assert state.handleRequestAction(a1, "n").equals(Actions.RESULT_F_BLOCKED);
        // another try
        assert state.createDispenser(a1.getPosition().moved("e", 1), blockTypes.iterator().next());
        assert state.handleRequestAction(a1, "e").equals(Actions.RESULT_SUCCESS);
    }

    @org.junit.Test
    public void handleSubmitAction() {
        state.setTerrain(Position.of(15,15), Terrain.GOAL);
        assert(state.teleport("A1", Position.of(15,15)));
        String blockType = state.getBlockTypes().iterator().next();
        assert(state.createBlock(Position.of(15,16), blockType) != null);
        assert(state.createBlock(Position.of(14,16), blockType) != null);
        assert(state.createTask("testTask1", 10,
                Map.of(Position.of(0, 1), blockType, Position.of(-1, 1), blockType)) != null);
        assert(state.attach(Position.of(15,15), Position.of(15,16)));
        assert(state.attach(Position.of(15,16), Position.of(14,16)));
        assert(state.handleSubmitAction(state.getEntityByName("A1"), "testTask1").equals(Actions.RESULT_SUCCESS));
    }
}