import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        if (args.length == 1) {
            new WithArray().run();
            return;
        }
        Random rand = new Random(12);
        int numLoops = 20;
        long numComplete = 0;
        int SIZE_FACTOR = 50_000;
        long timeSum = 0;
        for (int c = 0; c < numLoops; c++) {
            final int NUM_DESIRED_RESULTS = 1 + rand.nextInt(Math.min(SIZE_FACTOR * 1, 1_000));
            int numPostings = 1 + rand.nextInt(SIZE_FACTOR * 3);
            numComplete += numPostings;
            int numDocs = numPostings - rand.nextInt(numPostings);
            int maxWeight = 1+ rand.nextInt(SIZE_FACTOR*3);

            // Used to simulate postings
            ArrayList<CompareType> postings = new ArrayList<>(numPostings + 1);

            HashMap<Integer, Integer> docMap = new HashMap<>(numDocs * 3);
            PriorityQueue<Integer> queue = new PriorityQueue<>(NUM_DESIRED_RESULTS + 2, new CustomCompare(docMap));
            // Fill postings with samples
            for (int i = 0; i < numPostings; i++) {
                postings.add(i, new CompareType(rand.nextInt(maxWeight), rand.nextInt(numDocs) ));
            }
            var startTime = System.nanoTime();

             // Insert all postings into docMap / queue
             for (int i = 0; i < postings.size(); i++) {
                CompareType posting = postings.get(i);

                int docsPreviousWeight = docMap.getOrDefault(posting.docID, -1);
                int docWeight = posting.weight;
                // If its in the map already, update the weight in docMap
                if (docsPreviousWeight != -1) {
                    docWeight += docsPreviousWeight;
                    docMap.put(posting.docID, docWeight);
                    // This means this document was already in the queue
                    if (queue.size() >= NUM_DESIRED_RESULTS) {
                        int leastWeight = docMap.get(queue.peek());
                        if(leastWeight <= docsPreviousWeight) {
                            // If item is in list, remove and re-add it in case the position has changed
                            if(queue.remove(posting.docID)) {
                                queue.add(posting.docID);
                                // Item was not in queue before, but was == in weight to least in queue, and now can enter
                            } else if (docWeight + docsPreviousWeight > leastWeight){
                                queue.add(posting.docID);
                                queue.poll();
                            }
                        } else { // else this document could not have been in queue before
                            // If the document now meets the requirements for the queue
                            if (docWeight + docsPreviousWeight >= leastWeight) {
                                queue.add(posting.docID);
                                queue.poll();
                            }
                        }
                        // Doc is in queue, but queue is not full
                    } else {
                        queue.remove(posting.docID);
                        queue.add(posting.docID);
                    }
                    continue;
                }
                // Else, it's a new docID so add it to the hashtable
                docMap.put(posting.docID, posting.weight);

                if (queue.size() >= NUM_DESIRED_RESULTS) {
                    if(docMap.get(queue.peek()) > docWeight){
                        continue;
                    }
                    queue.add(posting.docID);
                    queue.poll();
                    } else {
                    queue.add(posting.docID);
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

            i = postings.size() - 1;

            while (j >= 0) {
                CompareType posting = null;
                if (i >= 0) {
                    posting = postings.get(i--);
                }
                int docID = qResults[j--];
                int weight = postingMap.get(docID);
                if (posting.weight != weight) {
                    throw new RuntimeException("numDocs: " + numDocs + " numPostings: " + numPostings + " i: " + i + " j: " + j);
                }
            }
        }
        System.out.println(timeSum/numLoops + "ms --- " + numComplete);
    }
}