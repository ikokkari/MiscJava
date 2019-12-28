/* An example program that demonstrates intercommunicating threads with the classic
 * producer-consumer example.
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

public class ProdConsDemo { 

    private static boolean running = true;
    private static ExecutorService es = Executors.newCachedThreadPool();
    private static final int WAITMIN = 5000;
    private static final int WAITMAX = 10000;
    private static final int MAXBUFFER = 5;

    // The methods of storage pool, used by clients of all kinds. Your problem is to implement Pool
    // so that the produces and consumers can use it safely so that none of them starves or shares
    // accidentally the same item.
    private static interface Pool {
        // Add the item x to this pool.
        public void addItem(int x) throws InterruptedException;
        // Take an item from the pool, blocking until there is an element.
        public int takeItem() throws InterruptedException;
    }

    // The abstract superclass for Producers and Consumers that use the pool.
    private static abstract class PoolClient {
        protected Pool pool;
        protected int id;
        
        // Demonstrating the Template Method design pattern.
        // Subclasses must override the next method according to what they do.
        protected abstract void doAction() throws InterruptedException;

        // In the constructor, create the background thread for this client.
        public PoolClient(int id, Pool pool) {
            this.id = id;
            this.pool = pool;
            es.submit(new Runnable() {
                    public void run() {
                        try {
                            while(running) {
                                Thread.sleep(ThreadLocalRandom.current().nextInt(WAITMAX - WAITMIN) + WAITMIN);
                                doAction(); // Template method overridden in subclasses
                            }
                        }
                        catch(InterruptedException e) { }
                        System.out.println("Terminating client " + PoolClient.this.id);
                    }
                });
        }
    }

    // The subclass for Producers.
    private static class Producer extends PoolClient {
        // A thread-safe integer with atomic update operations.
        private static final AtomicInteger timeStamp = new AtomicInteger(0);
        public Producer(int id, Pool pool) { super(id, pool); }

        public void doAction() throws InterruptedException {
            int x = timeStamp.getAndIncrement(); // the produced items are consecutive
            System.out.println("Producer " + id + " starts adding item " + x + " to the pool.");
            System.out.flush();
            pool.addItem(x);
            System.out.println("Producer " + id + " added the item " + x + " to the pool.");
            System.out.flush();
        }
    }

    // The subclass for Consumers.
    private static class Consumer extends PoolClient {
        public Consumer(int id, Pool pool) { super(id, pool); }

        public void doAction() throws InterruptedException {
            System.out.println("Consumer " + id + " starts acquiring an item from the pool.");
            System.out.flush();
            int item = pool.takeItem();
            System.out.println("Consumer " + id + " acquired the item " + item + " from the pool.");
            System.out.flush();
        }
    }

    // A solution for Pool using condition variables.
    private static class PoolUsingConditionVariables implements Pool {

        private Stack<Integer> contents = new Stack<Integer>();
        private Lock poolMutex = new ReentrantLock();
        private Condition itemExists = poolMutex.newCondition();
        private Condition spaceExists = poolMutex.newCondition();

        // Both methods addItem and takeItem are critical sections so that at most one thread
        // should be executing either one of them at the same time. Therefore we use a Lock object
        // poolMutex to ensure mutual exclusion.
        public void addItem(int x) throws InterruptedException {
            poolMutex.lock();
            try {
                while(contents.size() == MAXBUFFER) {
                    spaceExists.await();
                }
                contents.push(x);
                itemExists.signal(); // wake up one consumer thread waiting inside itemExists
            }
            finally { // make sure the mutex is unlocked no matter what
                poolMutex.unlock();
            }
        }

        public int takeItem() throws InterruptedException {
            poolMutex.lock();
            try {
                while(contents.isEmpty()) {
                    // As long as the stack is empty, go wait inside the itemExists condition variable.
                    itemExists.await();
                }
                // When we get here, an item is guaranteed to exist, so take it.
                int x = contents.pop();
                spaceExists.signal(); // wake up one producer thread waiting inside spaceExists
                return x;
            }
            finally {
                poolMutex.unlock();
            }
        }
    }

    // An alternative solution using only semaphores.
    private static class PoolUsingSemaphores implements Pool {
        private Stack<Integer> contents = new Stack<Integer>();
        private Semaphore itemCount = new Semaphore(0, true);
        private Semaphore spaceCount = new Semaphore(MAXBUFFER, true);
        private Semaphore poolMutex = new Semaphore(1); // Semaphore(1) works same as Lock...

        public void addItem(int x) throws InterruptedException {
            spaceCount.acquire();
            poolMutex.acquire();
            contents.push(x);
            itemCount.release(); // one more consumer can now take an item
            poolMutex.release();
        }

        // A fair (FIFO) semaphore is effectively a queue.
        public int takeItem() throws InterruptedException {
            itemCount.acquire(); // wait for an item to exist
            poolMutex.acquire();
            int x = contents.pop();
            spaceCount.release(); // one more producer can now add an item
            poolMutex.release();
            return x;
        }
    }

    // The simplest solution uses a blocking queue. The queue handles all the synchronization,
    // so both methods become one-liners. (That's what the blocking queues are there for.)
    private static class PoolUsingBlockingQueue implements Pool {
        private BlockingQueue<Integer> contents = new ArrayBlockingQueue<Integer>(MAXBUFFER);
        
        public void addItem(int x) throws InterruptedException {
            contents.put(x);
        }
        
        public int takeItem() throws InterruptedException {
            return contents.take();
        }
    }
    
    // Question: are the above solutions equivalent in that each waiting Consumer will eventually
    // get an item? That is, are all these solutions starvation-free for Consumers?

    // Time for the demonstration. Create the clients, given the pool.
    private static void createClients(int prod, int cons, Pool pool) {
        for(int i = 0; i < prod; i++) {
            new Producer(i, pool);
        }
        for(int i = 0; i < cons; i++) {
            new Consumer(prod + i, pool);
        }    
    }

    // Launch whichever demo you feel like trying out.
    public static void launchConditionDemo(int prod, int cons) {
        createClients(prod, cons, new PoolUsingConditionVariables());
    }

    public static void launchSemaphoreDemo(int prod, int cons) {
        createClients(prod, cons, new PoolUsingSemaphores());
    }
    
    public static void launchBlockingQueueDemo(int prod, int cons) {
        createClients(prod, cons, new PoolUsingBlockingQueue());
    }

    // Advanced exercise: Can you think of some other way to implement the pool?
    
    // Terminate the demo
    public static void terminate() {
        running = false;
        es.shutdownNow();      
        System.out.println("ProdCons demo terminated");
    }  
}
