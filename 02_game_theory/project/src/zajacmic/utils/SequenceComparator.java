package zajacmic.utils;

import java.util.Comparator;

/** comparing two sequence strings according to their length */
public class SequenceComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        if (o1.length() > o2.length()){
            return 1;
        } else if (o1.length() == o2.length()) {
            return 0;
        }

        return -1;
    }
}
