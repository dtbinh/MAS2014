package zajacmic;

import java.util.Map;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import zajacmic.game.ExtensiveForm;
import zajacmic.game.GameMap;
import zajacmic.tree.Node;

public class HW2main {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Write input file name as the parameter!");
            return;
        }

        System.out.println("[sequence]");

        GameMap map = GameMap.load(args[0]);
        Node root = map.createImperfectGameTree();

        ExtensiveForm game = new ExtensiveForm(root);

    //// BANDIT
        IloCplex cplexBandit = new IloCplex();
        cplexBandit.setOut(null);

        Map<String, IloNumVar> banditSeqToRpVar = game.realPlanVars(cplexBandit, game.sequencesBandit);

        game.realPlanConstr(game.BANDIT_PLAYER, cplexBandit, banditSeqToRpVar);

        game.constraintsForAgentSequences(cplexBandit,banditSeqToRpVar);

        boolean solved=cplexBandit.solve();

    //// END BANDIT

    //// AGENT
        IloCplex cplexAgent = new IloCplex();
        cplexAgent.setOut(null);

        Map<String, IloNumVar> agentSeqToRpVar = game.realPlanVars(cplexAgent, game.sequencesAgent);

        game.realPlanConstr(game.AGENT_PLAYER, cplexAgent, agentSeqToRpVar);

        Map<String,Double> mapVariables2Values = game.mapVarsToValues(cplexBandit, banditSeqToRpVar);
        IloNumVar agentObjVar = game.agentObj(cplexAgent, agentSeqToRpVar, mapVariables2Values);

        double agentGameValue = 0;

        solved &= cplexAgent.solve();
    //// END AGENT


    //// EXPORT LINEAR PROGRAMS
        cplexBandit.exportModel("zajacmic_bandit.lp");
        cplexAgent.exportModel("zajacmic_agent.lp");
    //// END EXPORT

    //// PRINT RESULTS
        game.printSequences();
        game.printUtilities();
        game.printAgentRealizationPlans(cplexAgent,agentSeqToRpVar);
        game.printBanditRealizationPlans(cplexBandit,banditSeqToRpVar);

        if (solved) {
            agentGameValue = cplexBandit.getValue(agentObjVar);
            System.out.println("SOLUTION_VALUE:" + agentGameValue);
        }
        else {
            System.out.println("Error: couldn't find a solution.");
        }

    //// END PRINT RESULTS
    }

}
