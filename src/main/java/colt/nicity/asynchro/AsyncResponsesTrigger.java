/*
 * AsyncResponsesTrigger.java.java
 *
 * Created on 03-12-2010 11:29:50 PM
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

import colt.nicity.core.process.IAsyncResponse;
import colt.nicity.core.lang.IOut;

/**
 *
 * @author Administrator
 * @param <T>
 */
public class AsyncResponsesTrigger<T> {

    private IAsyncResponse<T> trigger;
    private T triggerResponse;
    private int pending = 0;
    private int errors = 0;
    private boolean closed;

    /**
     * 
     * @param _triggerResponse
     * @param _trigger
     */
    public AsyncResponsesTrigger(T _triggerResponse, IAsyncResponse<T> _trigger) {
        trigger = _trigger;
        triggerResponse = _triggerResponse;
    }

    /**
     *
     * @param <R>
     * @param _enqueue
     * @return
     */
    synchronized public <R> IAsyncResponse<R> enqueue(
        final IAsyncResponse<R> _enqueue) {
        if (closed)
            throw new RuntimeException("Can't add to a closed trigger");
        pending++;
        return new IAsyncResponse<R>() {

            @Override
            public void response(IOut _, R _response) {
                try {
                    _enqueue.response(_, _response);
                    trigger(_);
                }
                catch (Throwable _t) {
                    error(_, _t);
                }
            }

            @Override
            public void error(IOut _, Throwable _t) {
                _enqueue.error(_, _t);
                AsyncResponsesTrigger.this.error(_, _t);
            }
        };
    }

    /**
     *
     * @return
     */
    synchronized public boolean errors() {
        return errors != 0;
    }

    /**
     *
     * @param _
     */
    synchronized public void close(IOut _) {
        if (closed)
            return;
        closed = true;
        trigger(_);
    }

    synchronized private void trigger(IOut _) {
        pending--;
        if (pending < 0 && errors == 0) {
            trigger.response(_, triggerResponse);
        }
    }

    synchronized private void error(IOut _, Throwable _t) {
        errors++;
        trigger.error(_, _t);
    }
}
