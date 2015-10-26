/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.apache.http.impl.conn.jmx;

import java.util.concurrent.TimeUnit;

import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.pool.PoolStats;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exposes statistics from a {@link PoolingClientConnectionManager}
 * 
 * @author Eric Dalquist
 */
public class ConnPoolControlData<T> implements ConnPoolControlDataMXBean {
    private final long POOL_STATS_REFRESH = TimeUnit.SECONDS.toNanos(1);
    
    private ConnPoolControl<T> connPoolControl;
    
    private volatile long lastLoaded = System.nanoTime();
    private volatile PoolStats poolStats;

    @Autowired
    public void setConnPoolControl(ConnPoolControl<T> connPoolControl) {
        this.connPoolControl = connPoolControl;
    }

    @Override
    public void setMaxTotal(int max) {
        connPoolControl.setMaxTotal(max);
    }

    @Override
    public int getMaxTotal() {
        return connPoolControl.getMaxTotal();
    }

    @Override
    public void setDefaultMaxPerRoute(int max) {
        connPoolControl.setDefaultMaxPerRoute(max);
    }

    @Override
    public int getDefaultMaxPerRoute() {
        return connPoolControl.getDefaultMaxPerRoute();
    }

    @Override
    public PoolStats getTotalStats() {
        return connPoolControl.getTotalStats();
    }

    @Override
    public int getLeased() {
        return getPoolStats().getLeased();
    }

    @Override
    public int getPending() {
        return getPoolStats().getPending();
    }

    @Override
    public int getAvailable() {
        return getPoolStats().getAvailable();
    }

    @Override
    public int getMax() {
        return getPoolStats().getMax();
    }
    
    private PoolStats getPoolStats() {
        PoolStats p = this.poolStats;
        
        final long now = System.nanoTime();
        if (now - lastLoaded >= POOL_STATS_REFRESH || p == null) {
            p = this.connPoolControl.getTotalStats();
            this.lastLoaded = now;
            this.poolStats = p;
        }
        
        return p;
    }
}
