/*
 * AsyncResponses.java.java
 *
 * Created on 03-12-2010 11:29:15 PM
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
import colt.nicity.core.process.IAsyncResponse;
import colt.nicity.core.process.ICallQueue;
import colt.nicity.core.lang.IOut;

/**
 *
 * @author Drone
 * @param <K> Type of key used to hash the AsyncCall<K>
 * @param <V> The response type
 * @param <R> The type used to aggregate the responses
 */
abstract public class AsyncResponses<K,V,R> implements IAsyncResponse<R> {
    /**
     *
     * @param _
     * @param _call
     * @param _response
     * @param _doneProcessingResponse
     */
    abstract public void response(IOut _,AsyncCall<K> _call,V _response,IAsyncResponse _doneProcessingResponse);
    
    private ICallQueue queue;
    private int pending = 1; // this is one so you must call close inorder to trigger
    private int errors = 0;
    private boolean closed = false;
    private R responses;
    /**
     *
     * @param _queue
     * @param _responses
     */
    public AsyncResponses(ICallQueue _queue,R _responses) {
        queue = _queue;
        responses = _responses;
    }
    /**
     *
     * @return
     */
    public R getCallsResponses() {
        return responses;
    }
    /**
     *
     * @return
     */
    public String tosString() {
        return "Pending:"+pending+" Errors:"+errors+" Closed:"+closed;
    }

    /**
     *
     * @param _
     * @param _call
     */
    public void enqueue(IOut _,final AsyncCall<K> _call) {
        synchronized(this) {
            if (closed) throw new RuntimeException("Trying to add to a closed call set"+this.getClass());
            pending++;
        }
        _call.setCalledWhenDoneOrError(new IAsyncResponse<V>() {
            AsyncCall<K> call = _call;
            @Override
            public void response(IOut _,final V _response) {
                final IAsyncResponse allDone = new IAsyncResponse<V>() {
                    @Override
                    public void response(IOut _, V _response) {
                        allDone(_);
                    }
                    @Override
                    public void error(IOut _, Throwable _t) {
                        _.out(_t);
                        synchronized(AsyncResponses.this) { errors++; }
                        allDone(_);
                    }
                };
                if (queue == null) AsyncResponses.this.response(_,call,_response,allDone);
                else queue.enqueueCall(_,new ICall() {
                    @Override
                    public void invoke(IOut _) {
                        AsyncResponses.this.response(_,call,_response,allDone);
                    }
                });
            }
            @Override
            public void error(IOut _, Throwable _t) {
                _.out(_t);
                synchronized(this) { errors++; }
                allDone(_);
            }
        });
        if (queue == null) _call.invoke(_);
        else queue.enqueueCall(_,_call);
    }
    /**
     *
     * @param _
     */
    public void close(IOut _) {
        synchronized(this) { closed = true; }
        allDone(_);
    }

    private void allDone(IOut _) {
        int localPending = -1;
        int localErrors = -1;
        synchronized(this) {
            pending--;
            localPending = pending;
            localErrors = errors;
        }
        if (localPending > 0) return;
        else if (localPending == 0 && localErrors == 0) response(_,getCallsResponses());
        else error(_,new RuntimeException("Pending("+localPending+") errors("+localErrors+")"));
    }
}
