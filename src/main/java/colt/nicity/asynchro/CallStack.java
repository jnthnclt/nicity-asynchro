/*
 * CallStack.java.java
 *
 * Created on 03-12-2010 11:31:15 PM
 *
 * Copyright 2010 Jonathan Colt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package colt.nicity.asynchro;

import colt.nicity.core.process.ICall;
import colt.nicity.core.process.ICallQueue;
import colt.nicity.core.lang.IOut;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Administrator
 */
public class CallStack implements ICallQueue {

    final private List<Thread> threads = new Vector<Thread>();
    final private List enqueuedStack = new Vector();
    private int maxThreads = 1;
    private int maxEnqueued = -1;

    /**
     *
     */
    public CallStack() {
    }
    // < 1 means unlimited

    /**
     *
     * @param _maxThreads
     * @param _maxEnqueued
     */
    public CallStack(int _maxThreads, int _maxEnqueued) {
        maxThreads = _maxThreads;
        maxEnqueued = _maxEnqueued;
    }

    /**
     *
     */
    public void purgeCalls() {
        synchronized (enqueuedStack) {
            enqueuedStack.clear();
        }
    }

    @Override
    public void enqueueCall(IOut _, ICall _call) {
        synchronized (enqueuedStack) {
            while (true) {
                if (maxEnqueued < 1 || enqueuedStack.size() < maxEnqueued * 2) {
                    enqueuedStack.add(_);
                    enqueuedStack.add(_call);
                    break;
                }
                else {
                    try {
                        enqueuedStack.wait();
                    }
                    catch (InterruptedException ex) {
                        _.out(ex);
                    }
                }
            }

        }
        synchronized (threads) {
            if (maxThreads > 0 && threads.size() >= maxThreads)
                return;
            Thread invoke = new Thread() {

                @Override
                public void run() {
                    while (true) {
                        IOut out = null;
                        ICall call = null;
                        synchronized (enqueuedStack) {
                            if (enqueuedStack.size() > 1) {
                                out = (IOut) enqueuedStack.remove(0);
                                call = (ICall) enqueuedStack.remove(0);
                            }
                        }
                        if (call != null) {
                            try {
                                call.invoke(out);// don't want a uncaiught exception killing this thread
                            }
                            catch (Throwable _t) {
                                out.out(_t);//??
                            }
                        }
                        call = null;
                        synchronized (enqueuedStack) {
                            if (enqueuedStack.size() == 0) {
                                break;
                            }
                        }
                        synchronized (enqueuedStack) {
                            enqueuedStack.notifyAll();
                        }
                    }
                    synchronized (threads) {
                        threads.remove(this);
                    }
                }
            };
            threads.add(invoke);
            invoke.start();
        }
    }

    /**
     *
     * @return
     */
    public int getCount() {
        return enqueuedStack.size();
    }

    @Override
    public String toString() {
        return "Threads (" + threads.size() + ")  Enqued(" + getCount() + ")";
    }
}

