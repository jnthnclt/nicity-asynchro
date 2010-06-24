/*
 * HelloAsyncSingleton.java.java
 *
 * Created on 01-13-2010 07:11:00 AM
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

import colt.nicity.asynchro.AsyncSingleton;
import colt.nicity.core.process.IAsyncResponse;
import colt.nicity.core.lang.IOut;
import colt.nicity.core.lang.SysOut;

/**
 *
 * @author Administrator
 */
public class HelloAsyncSingleton {
    static private AsyncSingleton<String> singleton;
    /**
     *
     * @param _args
     */
    static public  void main(String[] _args) {
        final SysOut _ = new SysOut();
        singleton = new AsyncSingleton<String>() {
            @Override
            public void create(IOut _, IAsyncResponse _created) {
                _.out("Creating Singleton...");
                _created.response(_, "Singleton");
            }
        };

        for(int i=0;i<5;i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        _.out("Sleeping");
                        Thread.sleep(1000+(long)(Math.random()*3000));
                    } catch(Exception x) {}
                    _.out("Getting...");
                    singleton.get(_, new IAsyncResponse<String>() {
                        @Override
                        public void response(IOut _, String _response) {
                            _.out("Got:"+_response);
                        }
                        public void canceled(IOut _, String _response) {
                            _.out("Canceled:"+_response);
                        }
                        @Override
                        public void error(IOut _, Throwable _t) {
                            _.out("Error:"+_t);
                        }
                    });
                }
            }.start();
        }
    }
}
