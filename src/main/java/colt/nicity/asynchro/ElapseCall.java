/*
 * ElapseCall.java.java
 *
 * Created on 03-12-2010 11:32:27 PM
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
import colt.nicity.core.lang.IOut;
import colt.nicity.core.time.UTime;

/**
 *
 * @author Administrator
 */
public class ElapseCall {

    IOut out;
    long elapse;
    ICall call;

    /**
     *
     * @param _
     * @param _elapse
     * @param _call
     */
    public ElapseCall(IOut _, long _elapse, ICall _call) {
        out = _;
        elapse = _elapse;
        call = _call;
    }

    /**
     *
     * @param _elapse
     */
    public void changeElapse(long _elapse) {
        elapse = _elapse;
    }
    final private Object activeLock = new Object();
    private Trigger active;

    /**
     *
     */
    public void signal() {
        signal(System.currentTimeMillis());
    }

    /**
     *
     */
    public void cancel() {
        cancel(System.currentTimeMillis());
    }

    /**
     *
     * @return
     */
    public boolean active() {
        synchronized (activeLock) {
            return active != null;
        }
    }

    /**
     *
     */
    public void trigger() {
        synchronized (activeLock) {
            if (active != null) {
                if (!active.triggered) {
                    active.signal(0);
                }
                else {
                    active.retrigger();
                }
            }
            else {
                active = new Trigger();
                active.signal(0);
                active.start();
            }
        }
    }

    /**
     *
     * @param _time
     */
    public void signal(long _time) {
        synchronized (activeLock) {
            if (active != null) {
                if (!active.triggered) {
                    active.signal(elapse);
                }
                else {
                    active.retrigger();
                }
            }
            else {
                active = new Trigger();
                active.signal(elapse);
                active.start();
            }
        }
    }

    /**
     *
     * @param _time
     */
    public void cancel(long _time) {
        synchronized (activeLock) {
            if (active != null) {
                active.cancel();
            }
        }
    }

    @Override
    public String toString() {
        if (active == null) {
            return "No Trigger Pending";
        }
        else {
            return active.toString();
        }
    }

    class Trigger extends Thread {

        long sleep = 0;
        long triggerInMillis = 0;
        boolean triggered = false;

        Trigger() {
        }

        public void retrigger() {
            triggered = false;
        }

        public void signal(long _elapseMillis) {
            sleep = _elapseMillis;
            if (sleep == 0) {
                triggerInMillis = 0;
            }
            else if (triggerInMillis < sleep) {
                triggerInMillis = sleep;
            }
            triggered = false;//??
        }

        private void cancel() {
            triggered = true;
        }

        @Override
        public String toString() {
            return "Will Trigger in " + UTime.elapse(triggerInMillis) + " ";
        }

        @Override
        public void run() {
            while (!triggered) {
                try {
                    triggerInMillis -= sleep;
                    if (sleep > 0) {
                        Thread.sleep(sleep);
                    }
                }
                catch (Exception x) {
                }
                if (triggerInMillis == 0 || triggerInMillis < elapse) {
                    while (true) {
                        synchronized (activeLock) {
                            triggered = true;
                        }
                        try {
                            call.invoke(out);
                        }
                        catch (Throwable t) {
                            if (out != null) out.out(t);
                            else t.printStackTrace();
                        }
                        synchronized (activeLock) {
                            if (triggered) {
                                active = null;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
