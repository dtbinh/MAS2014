package zajacmic.game;

public class Action {

    public final String name;
    public Action from;

    /**
     * Positive - fixed probability
     * Zero - action is not applicable.
     * Negative - action has no fixed probability.
     */
    public final float pr;

    public Action(String name) {
        this(name, -1);
    }

    public Action(String name, Action from) {
        this(name, from.pr);
        this.from = from;
    }

    public Action(String name, float pr) {
        this.name = name;
        this.pr = pr;
    }

    public String toString() {
	    return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null) return false;

        if (getClass() != obj.getClass()) return false;

        Action other = (Action) obj;

        if (name == null && other.name!=null) {
            return false;
        } else if (!name.equals(other.name)){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        if (name==null){
            result = prime * result;
        } else {
            result = prime * result + name.hashCode();
        }

        return result;
    }
}
