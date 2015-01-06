package zajacmic.game;

import ilog.concert.IloNumVar;
import zajacmic.tree.AbstractNode;

import java.util.List;

public class InformationSet {

    public final List<AbstractNode> nodes;

    // Sequence leading to this information set.
    public String sequence = null;

    public IloNumVar var = null;

    public InformationSet(List<AbstractNode> nodes) {
	    this.nodes = nodes;
    }
}
