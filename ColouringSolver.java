import java.util.*;
import java.util.function.*;
import java.io.*;

// Solves the graph colouring problem with backtracking.

public class ColouringSolver {

    // Counts for recursive calls and forward checking cutoffs that have occurred in the search.
    private static int callCount = 0, fcCount = 0;
    /** Accessor method for call count.
     * @return The count of how many recursive calls have taken place.
     */
    public static int getCallCount() { return callCount; }
    /** Accessor method for forward checking cutoff count.
     * @return How many forward checking cutoffs have taken place.
     */
    public static int getFcCount() { return fcCount; }
    
    // A comparator used to sort variable indices in order of increasing domain size.
    private static class DomainSizeComparator<E> implements Comparator<Integer> {
        // The list of domains of each variable.
        private List<Set<E>> remainingDomain;
        public DomainSizeComparator(List<Set<E>> remainingDomain) {
            this.remainingDomain = remainingDomain;
        }
        public int compare(Integer i1, Integer i2) {
            int c1 = remainingDomain.get(i1).size();
            int c2 = remainingDomain.get(i2).size();
            return c1 < c2 ? -1 : (c1 > c2 ? +1 : 0); 
        }
    }
    
    /**
     * Solve a colouring problem of {@code n} variables numbered 0, ..., {@code n} - 1.
     * @param n Total number of variables to assign.
     * @param neighbours Function returning the list of neighbours of each variable.
     * @param domain Function returning the domain of possible values of each variable.
     */
    public static <E> List<E> solve(
        int n,
        Function<Integer, List<Integer>> neighbours,
        Function<Integer, List<E>> domain    
    ) {
        List<Set<E>> remainingDomain = new ArrayList<Set<E>>();
        List<Map<E, Integer>> removed = new ArrayList<Map<E, Integer>>();
        List<List<Integer>> neighbourL = new ArrayList<List<Integer>>();
        for(int i = 0; i < n; i++) {
            remainingDomain.add(new HashSet<E>(domain.apply(i)));
            removed.add(new HashMap<E, Integer>());
            neighbourL.add(neighbours.apply(i));
        }
        ArrayList<E> solution = new ArrayList<E>(n);
        PriorityQueue<Integer> queue = new PriorityQueue<Integer>(new DomainSizeComparator(remainingDomain));
        for(int i = 0; i < n; i++) { 
            solution.add(null);
            queue.offer(i);
        }
        callCount = 0; fcCount = 0;
        return solve(0, new boolean[n], queue, neighbourL, remainingDomain, removed, solution);
    }
    
    // Solve the colouring problem with backtracking recursion with forward checking.
    private static <E> List<E> solve(
        int level,                      // The current level of recursion.
        boolean[] assigned,             // Which variables have already been assigned in the recursion.
        PriorityQueue<Integer> queue,   // Queue of variable indices remaining to be assigned.
        List<List<Integer>> neighbourL, // List of neighbours of each variable.
        List<Set<E>> remainingDomain,   // List of remaining values of each variable.
        List<Map<E, Integer>> removed,  // List of maps telling which level each value was removed for each variable.
        ArrayList<E> solution           // The solution list containing the variable values.
    ) {
        callCount++;
        // When no more unassigned variables remain, solution is completed successfully.
        if(queue.size() == 0) { return solution; }
        // Choose the next variable k to assign its value.
        int k = queue.poll();
        assigned[k] = true;
        // Loop through the possible remaining values for variable k.
        for(E elem: new ArrayList<E>(remainingDomain.get(k))) {
            // Try that value for variable k.
            solution.set(k, elem); 
            // How far to undo forward checking removals, if there is a forward checking cutoff.
            int upto = -1;
            // Loop through all neighbours of this variable.
            for(int i: neighbourL.get(k)) {
                // If variable i has already been assigned, skip it.
                if(assigned[i]) { continue; }
                // Check if the assigned value elem is in the domain of variable i.
                Set<E> remaining = remainingDomain.get(i);
                if(remaining.contains(elem)) {
                    // The last possible value of variable i causes a forward checking cutoff.
                    if(remaining.size() == 1) {
                        upto = i; fcCount++; break;
                    }
                    // Mark the value elem as having been removed at this level.
                    remaining.remove(elem);
                    removed.get(i).put(elem, level);
                }
            }
            // Try to assign the rest of the variables recursively.
            if(upto == -1 && solve(level + 1, assigned, queue, neighbourL, remainingDomain, removed, solution) != null) {
                return solution;
            }
            // Undo the element removals from domains of neighbour variables.
            for(int i: neighbourL.get(k)) {
                if(i == upto) { break; }
                // If the element was removed from domain of variable i at this level, restore it.
                Map<E, Integer> rmap = removed.get(i);
                if(rmap.getOrDefault(elem, -1) == level) {
                    rmap.remove(elem);
                    remainingDomain.get(i).add(elem);
                }
            }
        }
        // Variable k becomes unassigned again.
        queue.offer(k);
        assigned[k] = false;
        return null;
    }
    
    public static void readDimacsProblem(String filename, int colors) throws IOException {
        List<List<Integer>> neighbours = new ArrayList<List<Integer>>();
        int vars = 0;
        List<Integer> colorDom = new ArrayList<Integer>();
        for(int c = 0; c < colors; c++) { 
            colorDom.add(c + 1);
        }
        Scanner s = new Scanner(new File(filename));
        while(s.hasNextLine()) {
            String line = s.nextLine();
            if(line.charAt(0) == 'c') { continue; }
            else if(line.charAt(0) == 'p') {
                String[] info = line.split("\\s+");
                vars = Integer.parseInt(info[2]);
                for(int i = 0; i < vars; i++) {
                    neighbours.add(new LinkedList<Integer>());
                }
            }
            else if(line.charAt(0) == 'e') {
                String[] info = line.split("\\s+");
                int src = Integer.parseInt(info[1]) - 1;
                int tgt = Integer.parseInt(info[2]) - 1;
                neighbours.get(src).add(tgt);
            }
        }
        System.out.println("Read graph colouring problem with " + vars + " nodes."); 
        long startTime = System.currentTimeMillis();
        List<Integer> solution = solve(vars,
            n -> neighbours.get(n),
            n -> colorDom
        );
        long endTime = System.currentTimeMillis();
        System.out.println("Solved in " + (endTime - startTime) + " ms with " + getCallCount() 
        + " recursive calls and " + getFcCount() + " forward check cutoffs.");
        if(solution != null) {
            for(int i = 0; i < solution.size(); i++) {
                System.out.print(i + "->" + solution.get(i) + " ");
                if(i % 10 == 9) { System.out.println(""); }
            }
        }
        else {
            System.out.println("No solution found.");
        }
    }
}
