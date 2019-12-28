import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class PhilosopherTable {

    private static final int WAITMIN = 3000;
    private static final int WAITMAX = 6000;

    private boolean running = true;
    private ExecutorService es = Executors.newCachedThreadPool();
    private Semaphore canEat; // used below to avoid deadlock
    
    private Random rng = new Random() {
        private Lock mutex = new ReentrantLock();
        public int nextInt() {
            mutex.lock();
            int result = super.nextInt();
            mutex.unlock();
            return result;
        }
    };

    // A nested class representing individual forks that only one philosopher can
    // hold at any one time. The semaphore field tells if the fork is in use.
    private class Fork {
        private Semaphore sem = new Semaphore(1);
        public void pickUp() throws InterruptedException {
            sem.acquire(); // blocks until this fork becomes available
        }
        public void putDown() {
            sem.release(); // the fork is now available for another pickup
        }
    }

    private ArrayList<Fork> forks = new ArrayList<Fork>();
    private ArrayList<Philosopher> diners = new ArrayList<Philosopher>();
    
    public PhilosopherTable(int totalForks) {
        // When only n-1 philosophers can attempt to eat, there can be no deadlock.
        canEat = new Semaphore(totalForks - 1);
        for(int i = 0; i < totalForks; i++) {
            forks.add(new Fork());
        }
        // Just to be safe, don't create philosophers until all forks are ready.
        for(int i = 0; i < totalForks; i++) {
            diners.add(new Philosopher(i));    
        }        
    }
     
    /* This implementation of Philosopher prevents deadlock with a global semaphore that
     * allows at most n-1 philosophers to eat at the same time. This guarantees that one
     * philosopher is able to grab two forks and eat (proof: use the pigeonhole principle).
     * There still remains the possibility of starvation of some philosopher, though. Can
     * you write this class so that there also isn't a possibility of starvation?
     */     
    private class Philosopher {
        private int id;
        public Philosopher(int i) {
            this.id = i;
            es.submit(new Runnable() {
                public void run() {
                    try { 
                        while(running) {
                            System.out.println("Philosopher " + id + " is thinking deep thoughts.");
                            Thread.sleep(WAITMIN + rng.nextInt(WAITMAX - WAITMIN));
                            System.out.println("Philosopher " + id + " gets hungry.");
                            canEat.acquire();
                            int left = id > 0 ? id - 1: forks.size() - 1;
                            forks.get(left).pickUp();
                            System.out.println("Philosopher " + id + " picked up left fork " + left + ".");
                            int right = id < forks.size() - 1 ? id + 1: 0;
                            forks.get(right).pickUp();
                            System.out.println("Philosopher " + id + " picked up right fork " + right + ".");
                            System.out.println("Philosopher " + id + " is now happily eating.");
                            Thread.sleep(WAITMIN + rng.nextInt(WAITMAX - WAITMIN));
                            canEat.release();
                            forks.get(left).putDown();
                            forks.get(right).putDown();
                            System.out.println("Philosopher " + id + " is done eating and puts down his forks.");
                        }
                    }
                    catch(InterruptedException e) {
                        System.out.println("Philosopher " + id + " was interrupted during a wait.");
                    }
                    finally {                    
                        System.out.println("Philosopher " + id + " exits the table.");
                    }
                }
            });
        }
    }
     
    public void terminate() {
        running = false;
        es.shutdownNow(); // Interrupts all the tasks
        System.out.println("Philosopher table stopped.");
    }
}
