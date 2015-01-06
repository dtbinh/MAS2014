package zajacmic.game;

import zajacmic.tree.*;
import zajacmic.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

public class GameMap {

    /*First cell is in lower left corner.*/
    public final CellEnum[][] cells;
    public int startRow, startCol;

    private static final Action UP = new Action("UP");
    private static final Action DOWN = new Action("DOWN");
    private static final Action LEFT = new Action("LEFT");
    private static final Action RIGHT = new Action("RIGHT");
    private static final String BANDITS_ACTION = "BANDITS";
    private static final String UNSUCCESSFUL = "UNSUCCESSFUL";
    private static final String SUCCESSFUL = "SUCCESSFUL";

    /** number of bandits*/
    public final int nBandits;
    /** probability of attack */
    public final float prob;

    private Action unsuccessfulAction = null;
    private Action successfulAction = null;

    /** dangerous places where bandits can show up*/
    public ArrayList<PairIntInt> ambushes = new ArrayList<>();

    private GameMap(int nBandits, float prob,CellEnum[][] cells) {
        this.prob = prob;
        this.cells = cells;
        this.nBandits = nBandits;


        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] == CellEnum.DANGEROUS) {
                    ambushes.add(new PairIntInt(i, j));
                } else if (cells[i][j] == CellEnum.START) {
                    startRow = i;
                    startCol = j;
                }
            }
        }

        unsuccessfulAction = new Action(UNSUCCESSFUL,1-prob);
        successfulAction = new Action(SUCCESSFUL, prob);

    }

    private boolean isInsideMap(int row, int col) {
        return (row < cells.length && col < cells[0].length && row >= 0 && col >= 0 );
    }

    public static GameMap load(String filename) throws Exception {
	    BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));

        int nBandits = -1;
        float prob = -1;
        CellEnum[][] cells = null;

        try {
            int nRows = Integer.parseInt(reader.readLine());
            int nCols = Integer.parseInt(reader.readLine());

            cells = new CellEnum[nRows][nCols];

            for (int i = nRows - 1; i >= 0; i--) {
                String line = reader.readLine();
                for (int j = 0; j < line.length(); j++) {
                    cells[i][j] = CellEnum.assignCell(line.charAt(j));
                }
            }

            nBandits = Integer.parseInt(reader.readLine());
            prob = Float.parseFloat(reader.readLine());

            reader.close();


        }  catch (Exception e) {
                System.err.println(e);
        }
        return new GameMap(nBandits,prob,cells);
    }

    public Node createImperfectGameTree() throws IOException {
        Node root = createCompleteGame();
        convertToImperfectInformationGame(root);
        return root;
    }

    private Node createCompleteGame() {
        boolean[][] realBanditPositions = new boolean[cells.length][cells[0].length];
        boolean[][] visited = new boolean[cells.length][cells[0].length];
        visited[startRow][startCol] = true;

        // For each bandit positions, generate one game subtree.
        List<Edge> edges = new LinkedList<>();
        if (nBandits == 0) {
            AbstractNode subgame = createPerfectInformationGame(startRow, startCol, realBanditPositions, 0, visited);
            edges.add(new Edge(new Action(BANDITS_ACTION),subgame));
        } else {

            SubsetsGenerator generator = new SubsetsGenerator(ambushes.size());
            generator.reset(nBandits);

            while (generator.hasNext()) {
                List<PairIntInt> banditPositions = new LinkedList<>();

                BitSet set = generator.next();

                for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1)) {
                    banditPositions.add(ambushes.get(i));
                }

                for (PairIntInt bPos : banditPositions) {
                    realBanditPositions[bPos.i][bPos.j] = true;
                }

                AbstractNode subgame = createPerfectInformationGame(startRow, startCol, realBanditPositions, 0, visited);
                edges.add(new Edge(new Action(BANDITS_ACTION+banditPositions.toString()), subgame));

                for (PairIntInt bPos : banditPositions) {
                    realBanditPositions[bPos.i][bPos.j] = false;
                }
            }
        }

        return new Node(null, ExtensiveForm.BANDIT_PLAYER, edges);
    }

    private AbstractNode createPerfectInformationGame(int row, int col, boolean[][] actualBanditPositions, int golds, boolean[][] visited) {

        List<Edge> edges = new LinkedList<>();
        List<Action> actions = new LinkedList<>();

        if (isInsideMap(row - 1, col) && cells[row - 1][col] != CellEnum.OBSTACLE && !visited[row - 1][col]) {
            actions.add(DOWN);
        }

        if (isInsideMap(row + 1, col) && cells[row + 1][col] != CellEnum.OBSTACLE && !visited[row + 1][col]) {
            actions.add(UP);
        }

        if (isInsideMap(row, col - 1) && cells[row][col - 1] != CellEnum.OBSTACLE && !visited[row][col - 1]) {
            actions.add(LEFT);
        }

        if (isInsideMap(row, col + 1) && cells[row][col + 1] != CellEnum.OBSTACLE && !visited[row][col + 1]) {
            actions.add(RIGHT);
        }

        if (actions.isEmpty()) {
            return new LeafNode(null,0);
        }

        for (Action a : actions) {

            int newCol = col;
            int newRow = row;

            if (a.equals(DOWN)) {
                newRow -= 1;
            } else if (a.equals(UP)) {
                newRow += 1;
            } else if (a.equals(LEFT)) {
                newCol -= 1;
            } else if (a.equals(RIGHT)) {
                newCol += 1;
            }

            visited[newRow][newCol] = true;

            switch (cells[newRow][newCol]) {
                case GOLD:
                    edges.add(new Edge(a, createPerfectInformationGame(newRow, newCol, actualBanditPositions, golds+1, visited)));
                    break;
                case DESTINATION:
                    int utility = golds+10;
                    edges.add(new Edge(a, new LeafNode(null,utility)));
                    break;
                case DANGEROUS:
                    if (actualBanditPositions[newRow][newCol]) {
                        AbstractNode notSuccAttNode = createPerfectInformationGame(newRow, newCol, actualBanditPositions, golds, visited);
                        LeafNode succAttackLeaf = new LeafNode(null,0);

                        List<Edge> banditEdges = new LinkedList<>();
                        // add both succ and not succ attack nodes
                        banditEdges.add(new Edge(unsuccessfulAction, notSuccAttNode));
                        banditEdges.add(new Edge(successfulAction, succAttackLeaf));

                        Node banditNode = new Node(null, ExtensiveForm.BANDIT_PLAYER,banditEdges);
                        edges.add(new Edge(a, banditNode));

                    } else {
                        edges.add(new Edge(a, createPerfectInformationGame(newRow, newCol, actualBanditPositions, golds, visited)));
                    }
                    break;
                case EMPTY:
                    edges.add(new Edge(a, createPerfectInformationGame(newRow, newCol, actualBanditPositions, golds, visited)));
                    break;
                default:
                    System.err.println("This should never happened!");
            }

            visited[newRow][newCol] = false;
        }

        return new Node(null, ExtensiveForm.AGENT_PLAYER,edges);
    }

    private void convertToImperfectInformationGame(Node root) {

        // Initial set for bandits.
        InformationSet initBandtSet = new InformationSet(new LinkedList<>());
        // Init set for agent
        InformationSet initAgentSet = new InformationSet(new LinkedList<>());

        root.set = initBandtSet;
        initBandtSet.nodes.add(root);

        List<InformationSet> sets = new LinkedList<>();
        sets.add(initBandtSet);

        int nextFreeId = 0;

        for (Edge edge : root.edges) {
            initAgentSet.nodes.add(edge.to);
            edge.a = new Action(Integer.toString(nextFreeId), edge.a);

            nextFreeId++;
        }

        Stack<InformationSet> stack = new Stack<>();
        stack.push(initAgentSet);

        while (!stack.isEmpty()) {
            InformationSet set = stack.pop();

            if (!set.nodes.get(0).isLeaf) {

                HashMap<Action, List<AbstractNode>> nodesByAction = new HashMap<>();
                HashMap<Action, Action> actionMapping = new HashMap<>();

                sets.add(set);

                for (AbstractNode node : set.nodes) {
                    node.set = set;

                    Node n = (Node) node;

                    for (Edge edge : n.edges) {
                        Action action = edge.a;
                        if (!nodesByAction.containsKey(action)) {
                            nodesByAction.put(action,new LinkedList<>());
                            actionMapping.put(action,new Action(Integer.toString(nextFreeId),action));
                            nextFreeId++;
                        }

                        nodesByAction.get(action).add(edge.to);
                    }
                }


                // Nodes in one action group are candidates could be in the same information set
                for (Entry<Action, List<AbstractNode>> entry : nodesByAction.entrySet()) {

                    // Nodes of the agent after unsuccessfull bandit attack.
                    List<AbstractNode> notSuccBanditNodes = new LinkedList<>();

                    List<AbstractNode> actionGroup = entry.getValue();

                    // Agent nodes, which were direct successors.
                    List<AbstractNode> agentNodes = new LinkedList<>();

                    for (AbstractNode node : actionGroup) {
                        if (!node.isLeaf) {
                            Node n = (Node) node;

                            if(n.player == ExtensiveForm.BANDIT_PLAYER){

                                Edge badAttackEdge =  null;

                                if(n.edges.get(0).a.equals(unsuccessfulAction)){
                                    badAttackEdge =  n.edges.get(0);
                                } else {
                                    badAttackEdge = n.edges.get(1);
                                }

                                for (Edge edge : n.edges) {
                                    edge.a = new Action(Integer.toString(nextFreeId), edge.a);
                                    nextFreeId++;
                                }

                                notSuccBanditNodes.add(badAttackEdge.to);

                                // Bandit nodes have information set
                                InformationSet banditNodesSet = new InformationSet(new LinkedList<>());

                                n.set = banditNodesSet;
                                banditNodesSet.nodes.add(n);
                                sets.add(banditNodesSet);

                            } else if (n.player == ExtensiveForm.AGENT_PLAYER) {
                                agentNodes.add(n);
                            }
                        }
                    }

                    if (notSuccBanditNodes.size() > 0) {
                        stack.push(new InformationSet(notSuccBanditNodes));
                    }

                    if (agentNodes.size() > 0) {
                        stack.push(new InformationSet(agentNodes));
                    }


                }

                // Action mapping
                for (AbstractNode node : set.nodes) {
                    Node n = (Node) node;
                    for (Edge edge : n.edges) {
                        edge.a = actionMapping.get(edge.a);
                    }
                }
            }
        }
    }





}
