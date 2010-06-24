/*
 * AsyncSingleton.java.java
 *
 * Created on 03-12-2010 11:31:07 PM
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
import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Administrator
 * @param <R>
 */
abstract public class AsyncSingleton<R> {

    /**
     *
     * @param _
     * @param _created
     */
    abstract public void create(IOut _, IAsyncResponse<R> _created);
    private R signleton = null;
    final private List<IAsyncResponse<R>> waiting = new Vector<IAsyncResponse<R>>();

    /**
     *
     */
    public void clear() {
        synchronized (waiting) {
            signleton = null;
        }
    }

    /**
     *
     * @param _
     * @param _got
     */
    public void get(IOut _, IAsyncResponse<R> _got) {
        synchronized (waiting) {
            if (signleton != null) {
                _got.response(_, signleton);
                return;
            }
            if (waiting.size() > 0) {
                waiting.add(_got);
                return;
            }
            else {
                waiting.add(_got);
            }
        }
        create(_, new IAsyncResponse<R>() {

            @Override
            public void response(IOut _, R _response) {
                signleton = _response;
                for (IAsyncResponse n : allWaiting()) {
                    n.response(_, _response);
                }
            }

            @Override
            public void error(IOut _, Throwable _t) {
                for (IAsyncResponse n : allWaiting()) {
                    n.error(_, _t);
                }
            }
        });
    }

    private IAsyncResponse<R>[] allWaiting() {
        IAsyncResponse<R>[] notify = null;
        synchronized (waiting) {
            notify = (IAsyncResponse<R>[])waiting.toArray(new IAsyncResponse[waiting.size()]);
            waiting.clear();
        }
        return notify;
    }
}
