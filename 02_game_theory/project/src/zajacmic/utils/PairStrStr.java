package zajacmic.utils;

public class PairStrStr {
    public final String i,j;

    public PairStrStr(String i, String j) {
	    this.i=i;
        this.j=j;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;

        if (i!=null){
            result = prime*result + i.hashCode();
        } else {
            result = prime*result;
        }

        if (j!=null){
            result = prime*result + j.hashCode();
        } else {
            result = prime*result;
        }

        return result;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (this == obj) return true;

        if (PairStrStr.class != obj.getClass()) return false;

        PairStrStr other = (PairStrStr) obj;

        if (i == null) {
            if (other.i != null) return false;
        } else if (!i.equals(other.i)) return false;

        if (j == null) {
            if (other.j != null) return false;
        } else if (!j.equals(other.j)) return false;


        return true;
    }

    public String toString() {
        return "(" + i + ", " + j + ")";
    }



}