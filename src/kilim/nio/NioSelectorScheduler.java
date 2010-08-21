/* Copyright (c) 2006, Sriram Srinivasan
 *
 * You may distribute this software under the terms of the license 
 * specified in the file "License"
 */

package kilim.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import kilim.Mailbox;
import kilim.NotPausable;
import kilim.Pausable;
import kilim.RingQueue;
import kilim.Scheduler;
import kilim.Task;
import kilim.WorkerThread;


/**
 * This class wraps a selector and runs it in a separate thread.
 * 
 * It runs one or more ListenTasks (bound to their respective ports), which in
 * turn spawn as many session tasks (see {@link #listen(int, Class, Scheduler)})
 * as the number of new http connections. The supplied scheduler is used to
 * execute the tasks. It is possible, although not typical, to run tasks in the
 * NioSelectorScheduler itself, as it too is a scheduler.
 * 
 * Usage is as follows:
 * 
 * <pre>
 *  NioSelectorScheduler nss = new NioSelectorScheduler();
 *  nss.listen(8080, MySessionTask.class, Scheduler.getDefaultScheduler();
 *  
 *  class MySessionTask extends SessionTask {
 *  ...
 *  }
 * </pre>
 * 
 * @see SessionTask
 */
public class NioSelectorScheduler extends Scheduler {
    // TODO: Fix hardcoding
    public static int LISTEN_BACKLOG = 1000;

    /*
     * The thread in which the selector runs. This thread schedule for OP_ACCEPT
     * events.
     */
    public SelectorThread bossThread;
    /**
     * NIo workers,schedule for read/write events.
     */
    public SelectorThread[] workers;

    private final AtomicInteger sets = new AtomicInteger();

    static final int DEFAULT_WORKER_COUNT = Runtime.getRuntime().availableProcessors() * 2;


    public NioSelectorScheduler() throws IOException {
        this(DEFAULT_WORKER_COUNT);
    }


    /**
     * @throws IOException
     */
    public NioSelectorScheduler(int workerCount) throws IOException {
        if (workerCount < 0) {
            workerCount = DEFAULT_WORKER_COUNT;
        }
        this.bossThread = this.newSelectorThread("boss");
        this.workers = new SelectorThread[workerCount];
        for (int i = 0; i < workerCount; i++) {
            this.workers[i] = this.newSelectorThread("worker-" + i);
        }
    }


    private SelectorThread newSelectorThread(String namePostFix) throws IOException {
        Selector sel = Selector.open();
        SelectorThread result = new SelectorThread(namePostFix, sel, this);
        result.start();
        Task t = new RegistrationTask(result.registrationMbx, sel, result);
        t.setScheduler(this);
        t.start();
        return result;
    }


    public void listen(int port, Class<? extends SessionTask> sockTaskClass, Scheduler sockTaskScheduler)
            throws IOException {
        Task t = new ListenTask(port, this, sockTaskClass);
        t.setScheduler(this);
        t.preferredResumeThread = this.bossThread;
        t.start();
    }


    @Override
    public void schedule(Task t) throws NotPausable {

        SelectorThread reactor = (SelectorThread) t.preferredResumeThread;
        // Set reactor if absent
        if (reactor == null) {
            synchronized (t) {
                if ((reactor = (SelectorThread) t.preferredResumeThread) == null) {
                    reactor = this.nextReactor();
                    t.preferredResumeThread = reactor;
                    t.setScheduler(this);
                    if (t instanceof SessionTask) {
                        ((SessionTask) t).getEndPoint().sockEvMbx = reactor.registrationMbx;
                    }
                }
            }
        }

        // wakeup reactor if current thread is not reactor,otherwise run now.
        if (Thread.currentThread() != reactor) {
            // add to runnable tasks
            reactor.addRunnableTask(t);
            reactor.wakeup();
        }
        else {
            t._runExecute(reactor);
        }
    }


    private SelectorThread nextReactor() {
        SelectorThread reactor;
        if (this.workers.length > 0) {
            reactor = this.workers[this.sets.incrementAndGet() % this.workers.length];
        }
        else {
            reactor = this.bossThread;
        }
        return reactor;
    }


    @Override
    public void shutdown() {
        super.shutdown();
        this.bossThread.sel.wakeup();
        for (SelectorThread worker : this.workers) {
            worker.wakeup();

        }
    }

    public static class SelectorThread extends WorkerThread {
        NioSelectorScheduler _scheduler;
        /**
         * SessionTask registers its endpoint with the selector by sending a
         * SockEvent message on this mailbox.
         */
        public Mailbox<SockEvent> registrationMbx = new Mailbox<SockEvent>(1000);

        public Selector sel;

        static final long DEFAULT_WAIT = 500;

        long wait = DEFAULT_WAIT;

        private final AtomicBoolean wakenup = new AtomicBoolean(false);


        @Override
        public synchronized void addRunnableTask(Task t) {
            this.tasks.put(t);
            this.wakeup();
        }


        public void wakeup() {
            if (this.wakenup.compareAndSet(false, true)) {
                this.sel.wakeup();
            }
        }


        public SelectorThread(String namePostfix, Selector sel, NioSelectorScheduler scheduler) {
            super("KilimSelector-" + namePostfix, scheduler);
            this.sel = sel;
            this._scheduler = scheduler;
        }


        @Override
        public void run() {
            while (true) {
                int n;
                try {
                    if (this._scheduler.isShutdown()) {
                        Iterator<SelectionKey> it = this.sel.keys().iterator();
                        while (it.hasNext()) {
                            SelectionKey sk = it.next();
                            sk.cancel();
                            Object o = sk.attachment();
                            if (o instanceof SockEvent && ((SockEvent) o).ch instanceof ServerSocketChannel) {
                                // TODO FIX: Need a proper, orderly shutdown
                                // procedure for tasks. This closes down the
                                // task
                                // irrespective of the thread it may be running
                                // on. Terrible.
                                try {
                                    ((ServerSocketChannel) ((SockEvent) o).ch).close();
                                }
                                catch (IOException ignore) {
                                }
                            }
                        }
                        break;
                    }
                    RingQueue<Task> runnableTasks = new RingQueue<Task>(10);

                    runnableTasks = this.swapRunnables(runnableTasks);

                    while (runnableTasks.size() > 0) {
                        Task t = runnableTasks.get();
                        t._runExecute(null);
                        if (t instanceof SessionTask) {
                            SessionTask st = (SessionTask) t;
                            if (st.isDone()) {
                                st.close();
                            }
                        }
                    }

                    this.wakenup.set(false);

                    if (this.hasTasks()) {
                        n = this.sel.selectNow();
                    }
                    else {
                        n = this.sel.select(this.wait);
                    }
                    if (this.wakenup.get()) {
                        this.sel.wakeup();
                    }
                }
                catch (IOException ignore) {
                    n = 0;
                    ignore.printStackTrace();
                }
                if (n > 0) {
                    try {
                        Iterator<SelectionKey> it = this.sel.selectedKeys().iterator();
                        while (it.hasNext()) {
                            SelectionKey sk = it.next();
                            it.remove();
                            Object o = sk.attachment();
                            sk.interestOps(0);
                            if (o instanceof SockEvent) {
                                SockEvent ev = (SockEvent) o;
                                ev.replyTo.putnb(ev);
                            }
                            else if (o instanceof Task) {
                                Task t = (Task) o;
                                t.resume();
                            }
                        }
                    }
                    catch (CancelledKeyException e) {
                        // ignore
                    }
                    catch (Throwable e) {
                        System.err.println("dispatch event error:" + e.getMessage());
                        e.printStackTrace();
                    }
                }

            }

        }

    }


    public synchronized int numRunnables() {
        return this.runnableTasks.size();
    }

    public static class ListenTask extends SessionTask {
        Class<? extends SessionTask> sessionClass;
        ServerSocketChannel ssc;
        int port;
        NioSelectorScheduler selScheduler;


        public ListenTask(int port, NioSelectorScheduler selScheduler, Class<? extends SessionTask> sessionClass)
                throws IOException {
            this.port = port;
            this.sessionClass = sessionClass;
            this.ssc = ServerSocketChannel.open();
            this.selScheduler = selScheduler;
            this.ssc.socket().setReuseAddress(true);
            this.ssc.socket().bind(new InetSocketAddress(port), LISTEN_BACKLOG); //
            this.ssc.configureBlocking(false);
            this.setEndPoint(new EndPoint(selScheduler.bossThread.registrationMbx, this.ssc));
        }


        @Override
        public String toString() {
            return "ListenTask: " + this.port;
        }


        @Override
        public void execute() throws Pausable, Exception {
            int n = 0;
            while (true) {
                SocketChannel ch = this.ssc.accept();
                if (this.scheduler.isShutdown()) {
                    this.ssc.close();
                    break;
                }
                if (ch == null) {
                    this.endpoint.pauseUntilAcceptable();
                }
                else {
                    ch.socket().setTcpNoDelay(true);
                    ch.configureBlocking(false);
                    SessionTask task = this.sessionClass.newInstance();
                    task.setScheduler(this.selScheduler);
                    try {
                        SelectorThread reactor = this.selScheduler.nextReactor();
                        EndPoint ep = new EndPoint(reactor.registrationMbx, ch);
                        task.setEndPoint(ep);
                        task.preferredResumeThread = reactor;
                        n++;
                        // System.out.println("Num sessions created:" + n);
                        task.start();
                    }
                    catch (IOException ioe) {
                        ch.close();
                        System.err.println("Unable to start session:");
                        ioe.printStackTrace();
                    }
                }
            }
        }
    }

    public static class RegistrationTask extends Task {
        Mailbox<SockEvent> mbx;
        Selector selector;


        public RegistrationTask(Mailbox<SockEvent> ambx, Selector asel, SelectorThread reactor) {
            this.mbx = ambx;
            this.selector = asel;
            this.preferredResumeThread = reactor;
        }


        @Override
        public void execute() throws Pausable, Exception {
            while (true) {
                SockEvent ev = this.mbx.get();
                SelectionKey sk = ev.ch.register(this.selector, ev.interestOps);
                sk.attach(ev);
            }
        }
    }
}