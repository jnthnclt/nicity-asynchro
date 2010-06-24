/*
 * AsyncCall.java.java
 *
 * Created on 03-12-2010 11:29:32 PM
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
import colt.nicity.core.lang.ASetObject;

/**
 *
 * @author Administrator
 * @param <E>
 */
abstract public class AsyncCall<E> extends ASetObject<E> implements ICall {

    private IAsyncResponse response;
    private E key;

    /**
     *
     * @param _key
     */
    public AsyncCall(E _key) {
        key = _key;
    }

    @Override
    public E hashObject() {
        return key;
    }

    /**
     *
     * @param _response
     */
    public void setCalledWhenDoneOrError(IAsyncResponse _response) {
        response = _response;
    }

    /**
     *
     * @return
     */
    final public IAsyncResponse response() {
        return response;
    }
}
