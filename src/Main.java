import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Main {

    final static int NUM_DESIRED_RESULTS = 3;
    public static void main(String[] args) {
        // Used to simulate postings
        ArrayList<CompareType> postings = new ArrayList<>();
        // Holds postings that we have reviewed
        ArrayList<CompareType> list = new ArrayList<>();

        PriorityQueue<Integer> queue = new PriorityQueue<>(NUM_DESIRED_RESULTS, new CustomCompare(list));
        HashMap<Integer, Integer> mapToList = new HashMap<>(4*3);
        // Fill postings with samples
        for (int i = 0; i < NUM_DESIRED_RESULTS + 1; i++) {
            postings.add(i, new CompareType(1+i, 1+i));
        }
        postings.add(new CompareType(33, 5));
        postings.add(new CompareType(9, 9));
        // Add a second sample posting for document 1, with high weight
        postings.add(new CompareType(10, 1));

        // Insert all postings into list / queue
        for (int i = 0; i < postings.size(); i++) {
            CompareType posting = postings.get(i);
            Integer listLocation = null;
            // If its in the map already, update the weight in list
            if((listLocation = mapToList.get(posting.docID)) != null) {
                int previousWeight = list.get(listLocation).weight;
                int currentLowestWeight = list.get(queue.peek()).weight;

                list.get(listLocation).weight += posting.weight;
                // If the new weight for this document is greater than the smallest weight in queue
                if (list.get(listLocation).weight > currentLowestWeight) {
                    // This means this document was already in the queue
                    if (previousWeight > currentLowestWeight) {
                        // Remove and re-add to re-sort queue.
                        if (!queue.remove(listLocation)) {
                            throw new RuntimeException("Tried to remove a posting that is not in queue, but should be. Posting: " + posting);
                        }
                    }
                }
            } else { // Else, it's a new docID so add it to the list and save the location it was added in the mapToList
                listLocation = list.size();
                mapToList.put(posting.docID, listLocation);
                list.add(posting);
            }

            queue.add(listLocation);
            if (queue.size() > NUM_DESIRED_RESULTS) {
                queue.poll();
            }
        }
        while(!queue.isEmpty()) {
            System.out.print(list.get(queue.poll()));
        }
    }
}