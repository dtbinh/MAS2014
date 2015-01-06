package zajacmic.game;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import zajacmic.tree.*;
import zajacmic.utils.PairStrStr;
import zajacmic.utils.SequenceComparator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ExtensiveForm {
    public static final String SEPARATOR = ",";

    public static final String PREFIX_AGENT = "S";
    public static final String PREFIX_BANDIT = "Q";

    public static final int AGENT_PLAYER = 0;
    public static final int BANDIT_PLAYER = 1;

    public List<String> sequencesAgent;
    public List<String> sequencesBandit;

    // sequence -> information set map - according to the last action of sequence
    private Map<String, InformationSet> seqToSetAgent;
    private Map<String, InformationSet> seqToSetBandit;

    // agent sequence x bandit sequence -> utilities
    private Map<PairStrStr, Integer> utilities;

    // root node of game
    private Node root;

    private static StringBuilder builder1;
    private static StringBuilder builder2;

    public ExtensiveForm(Node root) {
        this.root = root;

        seqToSetAgent = new HashMap<>();
        seqToSetBandit = new HashMap<>();
        utilities = new HashMap<>();

        builder1 = new StringBuilder(SEPARATOR);
        builder2 = new StringBuilder(SEPARATOR);

        initSequencesAndUtility();
    }


    public void printSequences() {

        int id = 1;

        System.out.println("AGENT:");
        for (String seq : sequencesAgent) {
            String seqName = PREFIX_AGENT + id;
            if(seq.length()!=1){
                System.out.println(seqName + ":" + seq.substring(1,seq.length()-1));
            } else {
                System.out.println(seqName + ":" + seq.substring(1,seq.length()));
            }
            id++;
        }

        id = 1;

        System.out.println("ATTACKER:");
        for (String seq : sequencesBandit) {
            String seqName = PREFIX_BANDIT + id;
            if(seq.length()!=1){
                System.out.println(seqName + ":" + seq.substring(1,seq.length()-1));
            } else {
                System.out.println(seqName + ":" + seq.substring(1,seq.length()));
            }
            id++;
        }

    }

    public void printUtilities() {
        String[][] table = new String[sequencesAgent.size()+1][sequencesBandit.size()+1];

        if (table.length == 0) {
            return;
        }

        table[0][0] = "AGENT/ATTACKER";

        for (int i = 0; i < sequencesAgent.size(); i++) {
            table[i + 1][0] = PREFIX_AGENT + (i + 1);
        }

        for (int i = 0; i < sequencesBandit.size(); i++) {
            table[0][i + 1] = PREFIX_BANDIT + (i + 1);
        }

        int i = 1;

        for (String seqAgent : sequencesAgent) {
            int j = 1;
            for (String seqBandit : sequencesBandit) {
                PairStrStr key = new PairStrStr(seqAgent, seqBandit);

                if (utilities.containsKey(key)) {
                    int utility = utilities.get(key);
                    table[i][j] = utility + "," + (-utility);
                } else {
                    table[i][j] = "";
                }

                j++;
            }

            i++;
        }

        // Determine width of cells.
        int[] widths = new int[table[0].length];
        for (String[] row : table) {
            for (int j = 0; j < widths.length; j++) {
                widths[j] = Math.max(widths[j], row[j].length());
            }
        }

        // Create print and row separator patterns.
        StringBuilder printBuf = new StringBuilder();
        StringBuilder separatorBuf = new StringBuilder();
        for (int j = 0; j < widths.length; j++) {
            for (int k = 0; k < widths[j]; k++) {
                separatorBuf.append("-");
            }
            separatorBuf.append("-");

            printBuf.append("%-" + widths[j] + "s|");
        }

        printBuf.append("\n");
        String printPattern = printBuf.toString();
        String separatorPattern = separatorBuf.toString();

        // Print table according to formatting pattern
        for (String[] row : table) {
            System.out.printf(printPattern, (Object[]) row);
            System.out.println(separatorPattern);
        }
    }

    public void printAgentRealizationPlans(IloCplex cplex, Map<String, IloNumVar> seqToRpVar) throws IloException {
        System.out.println("SOLUTION_AGENT:");
        int id = 1;
        for (String seq : sequencesAgent) {
            System.out.println(PREFIX_AGENT + id + ":" + cplex.getValue(seqToRpVar.get(seq)));
            id++;
        }
    }

    public void printBanditRealizationPlans(IloCplex cplex,Map<String, IloNumVar> seqToRpVar) throws IloException {
        System.out.println("SOLUTION_ATTACKER:");
        int id = 1;
        for (String seq : sequencesBandit) {
            System.out.println(PREFIX_BANDIT + id + ":" + cplex.getValue(seqToRpVar.get(seq)));
            id++;
        }
    }

    private final void initSequencesAndUtility() {
        Set<String> agentSeqSet = new HashSet<>();
        Set<String> banditSeqSet = new HashSet<>();

        agentSeqSet.add(SEPARATOR);
        banditSeqSet.add(SEPARATOR);

        initSequences(root, builder1, builder2, agentSeqSet, banditSeqSet, seqToSetAgent, seqToSetBandit);

        SequenceComparator comparator = new SequenceComparator();

        sequencesAgent = new LinkedList<>(agentSeqSet);
        sequencesBandit = new LinkedList<>(banditSeqSet);

        Collections.sort(sequencesAgent, comparator);
        Collections.sort(sequencesBandit, comparator);
    }

    private void initSequences(AbstractNode n, StringBuilder agentSeq, StringBuilder banditSeq, Set<String> agentSeqs, Set<String> banditSeqs,
                               Map<String,InformationSet> seqToSetAgent,Map<String,InformationSet> seqToSetBandit) {

        if (!n.isLeaf){

            Set<String> sequences = null;
            Map<String, InformationSet> seqToSet = null;
            StringBuilder seqBuilder = null;

            if (((Node)n).player == AGENT_PLAYER){
                sequences = agentSeqs;
                seqToSet = seqToSetAgent;
                seqBuilder = agentSeq;
            } else if (((Node)n).player == BANDIT_PLAYER){
                sequences = banditSeqs;
                seqToSet = seqToSetBandit;
                seqBuilder = banditSeq;
            }

            ((Node)n).set.sequence = seqBuilder.toString();

            for (Edge edge : ((Node)n).edges) {
                StringBuilder newSeqBuilder = new StringBuilder(seqBuilder);
                newSeqBuilder.append(edge.a.name + SEPARATOR);

                String nSeq = newSeqBuilder.toString();

                sequences.add(nSeq);
                seqToSet.put(nSeq, ((Node)n).set);

                AbstractNode succ = edge.to;

                if (((Node)n).player == AGENT_PLAYER) {
                    initSequences(succ,newSeqBuilder,banditSeq,agentSeqs,banditSeqs,seqToSetAgent,seqToSetBandit);
                } else if (((Node)n).player == BANDIT_PLAYER){
                    initSequences(succ,agentSeq,newSeqBuilder,agentSeqs,banditSeqs,seqToSetAgent,seqToSetBandit);
                }
            }
        } else{
            // end node
            LeafNode leaf = (LeafNode) n;
            utilities.put(new PairStrStr(agentSeq.toString(),banditSeq.toString()),leaf.utility);
            return;
        }
    }

    public List<InformationSet> getInformationSets(int player) {
        Stack<AbstractNode> stack = new Stack<>();
        Set<InformationSet> sets = new HashSet<>();


        stack.push(root);

        while (!stack.isEmpty()) {
            AbstractNode node = stack.pop();
            if (!node.isLeaf) {
                Node innerNode = (Node) node;

                if (innerNode.player == player) {
                    sets.add(innerNode.set);
                }

                for (Edge edge : innerNode.edges) {
                    stack.push(edge.to);
                }
            }
        }

        return new LinkedList<>(sets);
    }

    public void constraintsForAgentSequences(IloCplex cplex, Map<String, IloNumVar> banditSeqToRp) throws IloException {

        int nextFreeId = 0;

        IloNumVar zero = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE,IloNumVarType.Float,"v"+nextFreeId);

        nextFreeId++;

        for (String seqAgent : sequencesAgent) {
            IloNumVar var = null;

            InformationSet set = seqToSetAgent.get(seqAgent);

            if (set == null) {
                var = zero;
            } else {
                if (set.var == null) {
                    set.var = cplex.numVar(Double.MIN_VALUE,Double.MAX_VALUE,IloNumVarType.Float,"v"+nextFreeId);
                    nextFreeId++;
                }
                var = set.var;
            }

            IloNumExpr sum = cplex.constant(0);

            HashSet<InformationSet> alreadySummed = new HashSet<>();

            List<String> extensions = getExtensions(seqAgent,sequencesAgent);

            for (String extension : extensions) {
                InformationSet extensionSet = seqToSetAgent.get(extension);

                if (extensionSet.var == null) {
                    extensionSet.var = cplex.numVar(Double.MIN_VALUE,Double.MAX_VALUE,IloNumVarType.Float,"v"+nextFreeId);
                    nextFreeId++;
                }

                if (!alreadySummed.contains(extensionSet)) {
                    sum = cplex.sum(sum, extensionSet.var);
                    alreadySummed.add(extensionSet);
                }
            }

            IloNumExpr lhs = cplex.diff(var, sum);

            sum = cplex.constant(0);

            for (String seqBandit : sequencesBandit) {
                PairStrStr key = new PairStrStr(seqAgent, seqBandit);
                if (utilities.containsKey(key)) {
                    sum = cplex.sum(sum,cplex.prod(utilities.get(key),banditSeqToRp.get(seqBandit)));
                }
            }

            IloNumExpr rhs = sum;

            cplex.addGe(lhs, rhs);
        }

        cplex.addMinimize(zero);

    }

    private List<String> getExtensions(String sequence,List<String> sequences) {

        List<String> candidates = new LinkedList<>();
        Set<String> extensions = new HashSet<>();

        for (String seq : sequences) {
            if (seq.startsWith(sequence)) {
                candidates.add(seq);
            }
        }

        for (String candidate : candidates) {
            int sepPos = candidate.indexOf(SEPARATOR, sequence.length());
            if (sepPos != -1) {
                extensions.add(candidate.substring(0, sepPos + 1));
            }
        }

        return new LinkedList<>(extensions);
    }

    public IloNumVar agentObj(IloCplex cplex, Map<String, IloNumVar> agentSeqToRpVar, Map<String, Double> banditSeqToRp) throws IloException {

        IloNumExpr sum = cplex.constant(0);
        IloNumExpr prd;
        IloNumVar objective = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE,IloNumVarType.Float,"obj");

        for (String agentSeq : sequencesAgent) {
            IloNumExpr innerSum = cplex.constant(0);
            for (String banditSeq : sequencesBandit) {
                PairStrStr key = new PairStrStr(agentSeq, banditSeq);
                if (utilities.containsKey(key)) {
                    double hlp =  utilities.get(key)*banditSeqToRp.get(banditSeq);
                    innerSum = cplex.sum(innerSum,hlp);
                }
            }
            prd = cplex.prod(innerSum, agentSeqToRpVar.get(agentSeq));
            sum = cplex.sum(sum,prd);
        }

        cplex.addMaximize(objective);
        cplex.addEq(objective,sum);

        return objective;
    }

    public Map<String, IloNumVar> realPlanVars(IloCplex cplex, List<String> sequences) throws IloException {
        Map<String, IloNumVar> seqToRp = new HashMap<>();

        for (String s : sequences) {
            IloNumVar var = cplex.numVar(0, 1, IloNumVarType.Float, s);
            seqToRp.put(s, var);
        }

        return seqToRp;
    }

    public void realPlanConstr(int player, IloCplex cplex, Map<String, IloNumVar> seqToRpVar) throws IloException {
        cplex.addEq(1, seqToRpVar.get(SEPARATOR));

        for (InformationSet set : getInformationSets(player)) {

            IloNumVar setRpVar = seqToRpVar.get(set.sequence);
            IloNumExpr sum = cplex.constant(0);

            for (Edge edge : ((Node) set.nodes.get(0)).edges) {

                IloNumVar extRpVar = seqToRpVar.get(set.sequence+edge.a.toString()+SEPARATOR);

                float pr = edge.a.pr;
                if (pr >= 0) {
                    // add constrain
                    IloNumExpr prd = cplex.prod(pr, setRpVar);
                    cplex.addEq(extRpVar, prd);
                }

                // Sum of realization plans of extensions
                sum = cplex.sum(sum, extRpVar);
            }

            cplex.addEq(sum, setRpVar);
        }
    }


    public static Map<String, Double> mapVarsToValues(IloCplex cplex,Map<String, IloNumVar> vars) throws IloException {
        Map<String, Double> mapping = new HashMap<>();

        for (Map.Entry<String, IloNumVar> e : vars.entrySet()) {
            mapping.put(e.getKey(), cplex.getValue(e.getValue()));
        }

        return mapping;
    }
}
