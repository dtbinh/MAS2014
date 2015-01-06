package zajacmic.tree;

import zajacmic.game.Action;

public class Edge {
    public Action a;
    public final AbstractNode to;

    public Edge(Action a, AbstractNode to) {
        this.a = a;
        this.to = to;
    }

}