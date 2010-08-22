/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim;

import java.util.concurrent.atomic.AtomicInteger;

public class WorkerThread extends Thread {
    protected volatile Task        runningTask;
    /**
     * A list of tasks that prefer to run only on this thread. This is used by kilim.ReentrantLock and Task to ensure
     * that lock.release() is done on the same thread as lock.acquire()
     */
    protected RingQueue<Task>      tasks      = new RingQueue<Task>(10);
    protected Scheduler            scheduler;
    static AtomicInteger gid        = new AtomicInteger();
    public int           numResumes = 0;

    public WorkerThread(Scheduler ascheduler) {
        super("KilimWorker-" + gid.incrementAndGet());
        this.scheduler = ascheduler;
    }
    
    public synchronized RingQueue<Task> swapRunnables(RingQueue<Task> emptyRunnables) {
        RingQueue<Task> ret = this.tasks;
        this.tasks = emptyRunnables;
        return ret;
    }

    
    public WorkerThread(String name,Scheduler ascheduler) {
        super(name);
        this.scheduler = ascheduler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Task t = this.getNextTask(this); // blocks until task available
                this.runningTask = t;
                t._runExecute(this,true);
                this.runningTask = null;
            }
        } catch (ShutdownException se) {
            // nothing to do.
        } catch (OutOfMemoryError ex) {
            System.err.println("Out of memory");
            System.exit(1);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.err.println(this.runningTask);
        }
        this.runningTask = null;
    }

    protected Task getNextTask(WorkerThread workerThread) throws ShutdownException {
        Task t = null;
        while (true) {
            if (this.scheduler.isShutdown()) {
                throw new ShutdownException();
            }

            t = this.getNextTask();
            if (t != null) {
                break;
            }

            // try loading from scheduler
            this.scheduler.loadNextTask(this);
            synchronized (this) { // ///////////////////////////////////////
                // Wait if still no task to execute.
                t = this.tasks.get();
                if (t != null) {
                    break;
                }

                this.scheduler.addWaitingThread(this);
                try {
                    this.wait();
                } catch (InterruptedException ignore) {
                } // shutdown indicator checked above
            } // //////////////////////////////////////////////////////////
        }
        assert t != null : "Returning null task";
        return t;
    }

    public Task getCurrentTask() {
        return this.runningTask;
    }

    public synchronized void addRunnableTask(Task t) {
        assert t.preferredResumeThread == null || t.preferredResumeThread == this : "Task given to wrong thread";
        this.tasks.put(t);
        this.notify();
    }

    public synchronized boolean hasTasks() {
        return this.tasks.size() > 0;
    }

    public synchronized Task getNextTask() {
        return this.tasks.get();
    }

    public synchronized void waitForMsgOrSignal() {
        try {
            if (this.tasks.size() == 0) {
                this.wait();
            }
        } catch (InterruptedException ignore) {
        }
    }
}
