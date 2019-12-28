import java.util.concurrent.*;
import java.util.*;

public class SortTournament {
    
   private static final int LEN = 1000000, RANGE = 1000000, TRIALS = 50;
   
   public static void main(String[] args) {
        ForkJoinPool fjp = new ForkJoinPool();
        long mergeTime = 0, quickTime = 0, arraysTime = 0, arraysParallelTime = 0;
        int[] am = new int[LEN];
        int[] aq = new int[LEN];
        int[] aa = new int[LEN];
        int[] ap = new int[LEN];
        System.out.println("Executing " + TRIALS + " trials of length " + LEN);
        for(int t = 0; t < TRIALS; t++) {
            Random rng = new Random(12345 + t);
            for(int i = 0; i < LEN; i++) {
                ap[i] = aa[i] = am[i] = aq[i] = rng.nextInt(RANGE);
            }
            long startTime = System.currentTimeMillis();
            fjp.invoke(new FJQuickSort(am, 0, LEN - 1));
            long endTime = System.currentTimeMillis();
            quickTime += (endTime - startTime);
            
            startTime = System.currentTimeMillis();
            fjp.invoke(new FJMergeSort(aq, new int[LEN], 0, LEN - 1));
            endTime = System.currentTimeMillis();
            mergeTime += (endTime - startTime);
            
            startTime = System.currentTimeMillis();
            Arrays.sort(aa);
            endTime = System.currentTimeMillis();
            arraysTime += (endTime - startTime);
            
            startTime = System.currentTimeMillis();
            Arrays.parallelSort(ap);
            endTime = System.currentTimeMillis();
            arraysParallelTime += (endTime - startTime);
            
            if(!Arrays.equals(am, aq)) {
                System.out.println("Error!"); break;
            }
        }
        System.out.println("Quicksort total: " + quickTime + " ms");
        System.out.println("Mergesort total: " + mergeTime + " ms");
        System.out.println("Arrays.sort total: " + arraysTime + " ms");
        System.out.println("Arrays.parallelSort total: " + arraysParallelTime + " ms");
    }
}
