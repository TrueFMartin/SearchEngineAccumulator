import java.util.Comparator;

public class CompareType implements Comparable<CompareType> {
    public int weight;
    public int docID;
    CompareType(int weight, int docID){
        this.weight = weight;
        this.docID = docID;
    }

    @Override
    public int compareTo(CompareType o) {
        return Integer.compare(this.weight, o.weight);
    }

    public int compareToDoc(CompareType o) {
        return Integer.compare(this.docID, o.docID);
    }
    @Override
    public String toString() {
        return "DocID: " + docID + "\tW: " + this.weight + "\n";
    }

}
