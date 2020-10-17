package players.mctsag;

import core.GameState;
import players.Player;
import players.optimisers.ParameterizedPlayer;
import utils.ElapsedCpuTimer;
import utils.Types;

import java.util.Random;

public class MCTSAGPlayer extends ParameterizedPlayer {

    /**
     * Random generator.
     */
    private Random m_rnd;

    /**
     * All actions available.
     */
    public Types.ACTIONS[] actions;

    /**
     * Params for this MCTS
     */
    public MCTSAGParams params;

    private SingleTreeNode curr;

    public MCTSAGPlayer(long seed, int id) {
        this(seed, id, new MCTSAGParams());
    }

    public MCTSAGPlayer(long seed, int id, MCTSAGParams params) {
        super(seed, id, params);
        reset(seed, id);

        this.curr = null;

        this.actions = Types.ACTIONS.all().toArray(new Types.ACTIONS[0]);
    }

    @Override
    public void reset(long seed, int playerID) {
        super.reset(seed, playerID);
        m_rnd = new Random(seed);

        this.params = (MCTSAGParams) getParameters();
        if (this.params == null) {
            this.params = new MCTSAGParams();
            super.setParameters(this.params);
        }
        this.curr = null;
    }

    @Override
    public Types.ACTIONS act(GameState gs) {

        // TODO update gs
        if (gs.getGameMode().equals(Types.GAME_MODE.TEAM_RADIO)){
            int[] msg = gs.getMessage();
        }

        ElapsedCpuTimer ect = new ElapsedCpuTimer();
        ect.setMaxTimeMillis(params.num_time);

        // Number of actions available
        int num_actions = actions.length;

        // Root of the tree
        if (this.curr == null)  {
            this.curr = new SingleTreeNode(params, m_rnd, num_actions, actions);
        } else {
            this.curr.updateTree();
        }

        this.curr.setRootGameState(gs);

        //Determine the action using MCTS...
        this.curr.mctsSearch(ect);

        //Determine the best action to take and return it.
        int action = this.curr.mostVisitedAction();

        this.curr = this.curr.getChildren()[action];

        // TODO update message memory

        //... and return it.
        return actions[action];
    }

    @Override
    public int[] getMessage() {
        // default message
        int[] message = new int[Types.MESSAGE_LENGTH];
        message[0] = 1;
        return message;
    }

    @Override
    public Player copy() {
        return new MCTSAGPlayer(seed, playerID, params);
    }
}