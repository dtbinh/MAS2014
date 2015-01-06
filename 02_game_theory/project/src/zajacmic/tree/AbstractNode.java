package zajacmic.tree;

import zajacmic.game.InformationSet;

public abstract class AbstractNode {
    private static int nextFreeId = 0;

    /** id of this node */
    public final int id;

    public boolean isLeaf;

    // null for leaf or bandit node.
    public InformationSet set;

    public AbstractNode(InformationSet set) {
        this.set = set;
        this.id = nextFreeId;
        nextFreeId++;
    }
    
    abstract public AbstractNode findNode(int id);
}
