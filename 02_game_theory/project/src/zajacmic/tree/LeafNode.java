package zajacmic.tree;

import zajacmic.game.InformationSet;

/** leaf node (game ends here) */
public class LeafNode extends AbstractNode {
    public final int utility;

    public LeafNode(InformationSet set,int utility) {
        super(set);
        this.utility = utility;
        isLeaf=true;
    }

    @Override
    public AbstractNode findNode(int id){

        if (this.id == id) {
            return this;
        }

        // leaf node does not have children!
        return null;
    }

}
