package zajacmic.utils;

import java.util.HashMap;

public enum CellEnum {
    OBSTACLE('#'),
    START('S'),
    DESTINATION('D'),
    GOLD('G'),
    DANGEROUS('E'),
    EMPTY('-');

    private static HashMap<Character, CellEnum> map = new HashMap<>();
    public final char c;

    static {
        for (CellEnum cell : CellEnum.values()) {
            map.put(cell.c, cell);
        }
    }

    private CellEnum(char c) {
        this.c = c;
    }

    public static CellEnum assignCell(char c) {
        return map.get(c);
    }
}
