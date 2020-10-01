import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

/* Sort the given array using quick sort with recursive calls
 * executed in parallel, to demonstrate Java 7 ForkJoinTask. */

public class FJQuickSort extends RecursiveAction {

    private static final int PARCUTOFF = 500; // adjust to taste
    private static final int QSCUTOFF = 50;
    private static final int MAXTASKS = 100;
    private static final int SAMPLES = 5;
    private static final Semaphore taskSem = new Semaphore(MAXTASKS);
    
    private int low, high;
    private int[] a, b;
    private List<FJQuickSort> tasks; // tasks launched by this task
    
    // Sort the subarray (low..high) of array a.
    public FJQuickSort(int[] a, int low, int high) {
        this.a = a; this.low = low; this.high = high;
        this.tasks = new ArrayList<FJQuickSort>();
        this.b = new int[SAMPLES];
    }
    
    // The important method of every ForkJoinTask.
    public void compute() {
        try {
            taskSem.acquire();
            quickSort(low, high);
        } catch(InterruptedException e) { }
        finally {
            taskSem.release();
        }
    }
  
    // Recursive quicksort.
    private void quickSort(int low, int high) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        while(high - low > QSCUTOFF) {
            // Partition using a randomly sampled element as the pivot, but
            // so that the last element is not used as the pivot.
            for(int i = 0; i < SAMPLES; i++) {
                b[i] = a[rng.nextInt(high - low) + low];
            }
            Arrays.sort(b); // sort the samples
            int pivot = b[SAMPLES / 2]; // and use their median as pivot
            int mid = partition(low, high, pivot);
            // Sort the smaller partition with a new task, and use this
            // current task to sort the larger partition.
            if(mid - low < high - mid) {
                if((high - low) > PARCUTOFF && taskSem.tryAcquire()) {
                    FJQuickSort task = new FJQuickSort(a, low, mid);
                    tasks.add(task);
                    task.fork();
                    taskSem.release();
                }
                else {
                    quickSort(low, mid); // Just use ordinary recursion.
                }
                low = mid + 1; // tail recursion elimination
            }
            else {
                if((high - low) > PARCUTOFF && taskSem.tryAcquire()) {
                    FJQuickSort task = new FJQuickSort(a, mid + 1, high);
                    tasks.add(task);
                    task.fork();
                    taskSem.release();
                }
                else {
                    quickSort(mid + 1, high);
                }
                high = mid;
            }
        }
        // Finish up this subarray with insertion sort.
        if(high - low <= QSCUTOFF) { insertionSort(low, high); }
        // Wait for the other parallel tasks to finish before returning.
        for(FJQuickSort task: tasks) { task.join(); }
        tasks.clear(); // to help the garbage collection a bit
    }
    
    // Partition the subarray to small and big elements. The pivot
    // element may not be in the high location only, because such partition
    // will then make the high subarray to be empty. Returns -1 if the
    // subarray is already sorted.
    private int partition(int low, int high, int pivot) {
        int i = low - 1, j = high + 1;
        while(i < j) {
            do { i++; } while(a[i] < pivot);
            do { j--; } while(a[j] > pivot);
            if(i < j) {
                int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
            }
        }
        return j;
    }
    
    // Same as in FJMergeSort.
    private void insertionSort(int low, int high) {
        for(int i = low + 1; i <= high; i++) {
            int x = a[i];
            int j = i;
            while(j > low && a[j-1] > x) {
                a[j] = a[j-1];
                j--;
            }
            a[j] = x;
        }
    } 
}
