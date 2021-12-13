import java.util.Dictionary;

public class Lan {
    int id;
    int dist;
    long seek;
    int nextHop;
    int childBitMap;
    int leafBitMap;
    Dictionary<Integer, Integer> isAnyRouterUsingMe;
    boolean nmrFlag;
    Dictionary<Integer, Integer> NMRDict;
    Dictionary<Integer, Integer> NMRRout;
}
