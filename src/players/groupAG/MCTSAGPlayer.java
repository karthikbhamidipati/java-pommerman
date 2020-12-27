package players.groupAG;

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

    private SingleTreeNode root;

    public MCTSAGPlayer(long seed, int id) {
        this(seed, id, new MCTSAGParams());
    }

    public MCTSAGPlayer(long seed, int id, MCTSAGParams params) {
        super(seed, id, params);
        reset(seed, id);

        this.root = null;

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
        // setting the root to null when resetting for a fresh start to collect statistics
        this.root = null;
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

        /**
         * Root of the tree
         * If root is null, initialize the root
         * else update the tree by discounting the statistics(totValue & nVisits) and updating the max depth of all the nodes in the tree
         *
         */
        if (this.root == null)  {
            this.root = new SingleTreeNode(params, m_rnd, num_actions, actions);
        } else {
            this.root.updateTree();
        }

        this.root.setRootGameState(gs);

        //Determine the action using MCTS...
        this.root.mctsSearch(ect);

        //Determine the best action to take and return it.
        int action = this.root.mostVisitedAction();

        // Updating the root with the subtree of the action chosen
        this.root = this.root.getChildren()[action];

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