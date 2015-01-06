package zajacmic.tree;

import zajacmic.game.InformationSet;
import java.util.List;

/** Normal node in game tree (not a leaf) */
public class Node extends AbstractNode {
    public final int player;
    public final List<Edge> edges;
    
    public Node(InformationSet set, int player, List<Edge> edges) {
        super(set);
        this.player = player;
        this.edges = edges;
        isLeaf=false;
    }

    @Override
    public AbstractNode findNode(int id){

        if (this.id == id) {
            return this;
        }

        for (Edge edge : this.edges) {
            AbstractNode child = edge.to.findNode(id);
            if (child != null) {
                return child;
            }
        }

        return null;
    }
}
