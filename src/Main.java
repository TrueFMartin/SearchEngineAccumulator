import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.Logger.Level.DEBUG;

public class Main {

    public static void main(String[] args) {
//        Logger LOGGER = Logger.getGlobal();
        Random rand = new Random(5);
//        int numLoops = 5 + rand.nextInt(1000000);
        int numLoops = 10;
        int mod = numLoops / 100;
        long numComplete = 0;
//        System.out.println("Num Loops: " + numLoops);
//        String bars = "-".repeat(100);
//        System.out.println("|" + bars + "|");
//        System.out.print("|");
        int SIZE_FACTOR = 1000;
        long timeSum = 0;
        for (int c = 0; c < numLoops; c++) {
//            if (c % mod == 0) {
//                System.out.print("-");
//            }
            final int NUM_DESIRED_RESULTS = 1 + rand.nextInt(Math.min(SIZE_FACTOR * 1, 1_000));
            int numPostings = 1 + rand.nextInt(SIZE_FACTOR * 3);
            numComplete += numPostings;
            int numDocs = numPostings - rand.nextInt(numPostings);
            int maxWeight = 1+ rand.nextInt(SIZE_FACTOR*3);
//            System.out.println("-------------------------------------");

            // Used to simulate postings
            ArrayList<CompareType> postings = new ArrayList<>(numPostings + 1);

            HashMap<Integer, Integer> docMap = new HashMap<>(numDocs * 3);
            PriorityQueue<Integer> queue = new PriorityQueue<>(NUM_DESIRED_RESULTS + 2, new CustomCompare(docMap));
            // Fill postings with samples
            for (int i = 0; i < numPostings; i++) {
                postings.add(i, new CompareType(rand.nextInt(maxWeight), rand.nextInt(numDocs) ));
            }
            HashMap<Integer, Boolean> isDocQueued = new HashMap<>(NUM_DESIRED_RESULTS * 3);
//            System.out.println("Starting");
            var startTime = System.nanoTime();
            // Insert all postings into docMap / queue
            for (int i = 0; i < postings.size(); i++) {
                CompareType posting = postings.get(i);

                int previousWeight = docMap.getOrDefault(posting.docID, -1);
                int docWeight = posting.weight;
                // If its in the map already, update the weight in docMap
                if (previousWeight != -1) {
                    docWeight += previousWeight;
                    docMap.put(posting.docID, docWeight);
                    // This means this document was already in the queue
                    if (isDocQueued.get(posting.docID)) {
                        // Remove and re-add to re-sort queue.
                        queue.remove(posting.docID);
                        queue.add(posting.docID);
                        continue;
//                        if (!queue.remove(posting.docID)) {
//                            LOGGER.log(Level.WARNING,"Tried to remove a posting that is not in queue, but should be. Posting:", posting);
//                        }
                    }
                } else { // Else, it's a new docID so add it to the hashtable
                    docMap.put(posting.docID, posting.weight);
                }

                if (queue.size() >= NUM_DESIRED_RESULTS) {
                    if(docMap.get(queue.peek()) > docWeight){
                        // If this doc is NOT already in 'isDocQueued' map
                        if (docWeight == posting.weight)
                            isDocQueued.put(posting.docID, false);
                        continue;
                    }
                    // LOOK AT HISTORY BEFORE CHANGING line 58
                    queue.add(posting.docID);
                    int docID = queue.poll();
                    // If the posting that was just added was added to the front of the queue,
                    if (docID == posting.docID) {
                        isDocQueued.put(docID, false);
                    } else { // Some other posting was removed from queue
                        isDocQueued.put(docID, false);
                        isDocQueued.put(posting.docID, true);
                    }
                } else {
                    queue.add(posting.docID);
                    isDocQueued.put(posting.docID, true);
                }
            }

            Integer[] qResults = new Integer[queue.size()];
            int i = 0;
            int j = queue.size() - 1;
            while (!queue.isEmpty()) {
                qResults[i++] = queue.poll();
            }
            timeSum += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);HashMap<Integer, Integer> postingMap = new HashMap<>();

            for (CompareType posting : postings) {
                int prevWeight = postingMap.getOrDefault(posting.docID, -1);
                if (prevWeight == -1) {
                    postingMap.put(posting.docID, posting.weight);
                } else {
                    postingMap.put(posting.docID, posting.weight + prevWeight);
                }
            }
            Set<Map.Entry<Integer, Integer>> x = postingMap.entrySet();
            postings.clear();
            x.forEach(entry -> postings.add(new CompareType(entry.getValue(), entry.getKey())));
            postings.sort(CompareType::compareTo);


//            System.out.printf("That took: %d ms%n",
//                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
//            System.out.printf("With numDocs: %d, numPostings: %d, numResults: %d%n", numDocs, numPostings, NUM_DESIRED_RESULTS);

            i = postings.size() - 1;

            while (j >= 0) {
                CompareType posting = null;
                if (i >= 0) {
                    posting = postings.get(i--);
//                    System.out.println("Postings: ID: " + posting.docID + ", W:  " + posting.weight);
                }
                int docID = qResults[j--];
                int weight = postingMap.get(docID);
                if (posting.weight != weight) {
                    // FIXME figure out error
                    throw new RuntimeException("numDocs: " + numDocs + " numPostings: " + numPostings + " i: " + i + " j: " + j);
                }
//                System.out.println("Queue   : ID:" + docID + ", W: " + docMap.get(docID));
//                System.out.println("\t-----------------");
            }
        }
        System.out.println(timeSum/numLoops + "ms --- " + numComplete);
    }
}