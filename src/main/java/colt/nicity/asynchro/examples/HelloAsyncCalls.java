/*
 * HelloAsyncCalls.java.java
 *
 * Created on 03-12-2010 11:33:18 PM
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
package colt.nicity.asynchro.examples;

import colt.nicity.asynchro.AsyncCall;
import colt.nicity.asynchro.AsyncResponses;
import colt.nicity.asynchro.CallStack;
import colt.nicity.core.process.IAsyncResponse;
import colt.nicity.core.lang.IOut;
import colt.nicity.core.lang.SysOut;

/**
 *
 * @author Administrator
 */
public class HelloAsyncCalls {

    /**
     *
     * @param _args
     */
    static public void main(String[] _args) {
        get(new SysOut(), 10, new IAsyncResponse<double[]>() {

            @Override
            public void response(IOut _, double[] _response) {
                System.out.println("Response");
                for (int i = 0; i < _response.length; i++) {
                    System.out.println(i + "):" + _response[i]);
                }
            }

            @Override
            public void error(IOut _, Throwable _t) {
                System.out.print("Error:" + _t);
            }
        });
    }
    static private CallStack callStack = new CallStack();

    /**
     *
     * @param _
     * @param _count
     * @param _got
     */
    static public void get(IOut _, int _count,
        final IAsyncResponse<double[]> _got) {
        double[] responses = new double[_count];
        AsyncResponses<Integer, Double, double[]> calls = new AsyncResponses<Integer, Double, double[]>(
            callStack, responses) {

            @Override
            public void response(IOut _, AsyncCall<Integer> _call,
                Double _response, IAsyncResponse _doneProcessingResponse) {
                _.out("Storing..." + _call.hashObject());
                getCallsResponses()[_call.hashObject()] = _response;
                _doneProcessingResponse.response(_, _response);
            }

            @Override
            public void response(IOut _, double[] _response) {
                _got.response(_, getCallsResponses());
            }

            @Override
            public void error(IOut _, Throwable _t) {
                _got.error(_, _t);
            }
        };
        for (int i = 0; i < responses.length; i++) {
            calls.enqueue(_, new AsyncCall<Integer>(i) {

                @Override
                public void invoke(IOut _) {
                    getRandom(_, response());
                }
            });
        }
        calls.close(_);
    }

    // a method that sleeps for a random duration and then produces a random number
    /**
     *
     * @param _
     * @param _response
     */
    static public void getRandom(final IOut _, final IAsyncResponse _response) {
        new Thread() {

            @Override
            public void run() {
                try {
                    _.out("Sleeping");
                    Thread.sleep(1000 + (long) (Math.random() * 3000));
                }
                catch (Exception x) {
                }
                _.out("Producing...");
                _response.response(_, Math.random());
            }
        }.start();
    }
}
