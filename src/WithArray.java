import java.util.*;
import java.util.concurrent.TimeUnit;

public class WithArray {
    public void run() {
//        Logger LOGGER = Logger.getGlobal();
        Random rand = new Random(12);
//        int numLoops = 5 + rand.nextInt(1000000);
        int numLoops = 20;
        int mod = numLoops / 100;
        long numComplete = 0;

        int SIZE_FACTOR = 50_000;
        long timeSum = 0;
        for (int c = 0; c < numLoops; c++) {

            final int NUM_DESIRED_RESULTS = 1 + rand.nextInt(Math.min(SIZE_FACTOR * 1, 1_000));
            int numPostings = 1 + rand.nextInt(SIZE_FACTOR * 3);
            int numDocs = numPostings - rand.nextInt(numPostings);
            int maxWeight = 1 + rand.nextInt(SIZE_FACTOR * 3);

            ArrayList<CompareType> postings = new ArrayList<>();
            // Fill postings with samples
            for (int i = 0; i < numPostings; i++) {
                postings.add(i, new CompareType(rand.nextInt(maxWeight), rand.nextInt(numDocs) ));
            }
            numComplete += numPostings;
            // Holds postings that we have reviewed
            ArrayList<CompareType> list = new ArrayList<>();

            PriorityQueue<Integer> queue = new PriorityQueue<>(NUM_DESIRED_RESULTS  + 2, new CustomCompareArray(list));
            HashMap<Integer, Integer> mapToList = new HashMap<>(4 * 3);

            var startTime = System.nanoTime();
            for (int i = 0; i < postings.size(); i++) {
                CompareType posting = postings.get(i);

                int listLocation = mapToList.getOrDefault(posting.docID, -1);

                int docWeight = posting.weight;
                // If its in the map already, update the weight in docMap
                if (listLocation != -1) {
                    int docsPreviousWeight = list.get(listLocation).weight;
                    list.get(listLocation).weight += posting.weight;
                    if (queue.size() >= NUM_DESIRED_RESULTS) {
                        int leastWeight = list.get(queue.peek()).weight;
                        // This means this document was already in the queue
                        if(leastWeight <= docsPreviousWeight) {
                            // If item is in list, remove and re-add it in case the position has changed
                            if(queue.remove(listLocation)) {
                                queue.add(listLocation);
                            // Item was not in queue before, but was == in weight to least in queue, and now can enter
                            } else if (docWeight + docsPreviousWeight > leastWeight){
                                queue.add(listLocation);
                                queue.poll();
                            }
                        } else { // else this document could not have been in queue before
                            // If the document now meets the requirements for the queue
                            if (docWeight + docsPreviousWeight >= leastWeight) {
                                queue.add(listLocation);
                                queue.poll();
                            }
                        }
                    // Doc is in queue, but queue is not full
                    } else {
                        queue.remove(listLocation);
                        queue.add(listLocation);
                    }
                    continue;
                }
                //Else, it's a new docID so add it to the hashtable
                listLocation = list.size();
                mapToList.put(posting.docID, listLocation);
                list.add(new CompareType(posting.weight, posting.docID));

                // If queue is full,
                if (queue.size() >= NUM_DESIRED_RESULTS) {
                    // If weight is too low or matched with lowest, skip
                    if(list.get(queue.peek()).weight >= posting.weight){
                        continue;
                    }

                    queue.add(listLocation);
                    queue.poll();
                } else { // Else queue is not full, add posting
                    queue.add(listLocation);
                }
            }

            Integer[] qResults = new Integer[queue.size()];
            int i = 0;
            int j = queue.size() - 1;
            while (!queue.isEmpty()) {
                qResults[i++] = queue.poll();
            }
            timeSum += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            HashMap<Integer, Integer> postingMap = new HashMap<>();

            for (CompareType posting : postings) {
                int prevWeight = postingMap.getOrDefault(posting.docID, -1);
                if (prevWeight == -1) {
                    postingMap.put(posting.docID, posting.weight);
                } else {
                    postingMap.put(posting.docID, posting.weight + prevWeight);
                }
            }
            Set<Map.Entry<Integer, Integer>> x = postingMap.entrySet();
            ArrayList<CompareType> sortedPostings = new ArrayList<CompareType>();
            x.forEach(entry -> sortedPostings.add(new CompareType(entry.getValue(), entry.getKey())));
            sortedPostings.sort(CompareType::compareTo);

            i = sortedPostings.size() - 1;

            while (j >= 0) {
                CompareType posting = null;
                if (i >= 0) {
                    posting = sortedPostings.get(i--);
                }
                int location = qResults[j--];
                if (posting.weight != list.get(location).weight) {
                    // FIXME figure out error
                    throw new RuntimeException("numDocs: " + numDocs + " numPostings: " + numPostings + " i: " + i + " j: " + j);
                }
            }
            System.out.println(" ");
        }
        System.out.println(timeSum/numLoops + "ms --- " + numComplete);


    }

}
