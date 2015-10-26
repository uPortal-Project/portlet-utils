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
package org.jasig.apache.http.impl.conn;

import java.lang.Thread.State;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of PoolingClientConnectionManager that spawns a monitoring thread to facilitate timely shutdown. 
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public class CleanShutdownPoolingClientConnectionManager extends PoolingClientConnectionManager {
    private final Lock shutdownLock = new ReentrantLock();
    private final AtomicBoolean shutdownComplete = new AtomicBoolean(false);
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private long shutdownThreadKillTime = TimeUnit.SECONDS.toMillis(45);
    private long shutdownThreadMaxTime = TimeUnit.SECONDS.toMillis(30);
    private long shutdownThreadMaxWaitTime = TimeUnit.SECONDS.toMillis(1);
    private long shutdownThreadPollRate = 5;
    
    
    public CleanShutdownPoolingClientConnectionManager() {
        super();
    }
    public CleanShutdownPoolingClientConnectionManager(SchemeRegistry schreg, DnsResolver dnsResolver) {
        super(schreg, dnsResolver);
    }
    public CleanShutdownPoolingClientConnectionManager(SchemeRegistry schemeRegistry, long timeToLive, TimeUnit tunit,
            DnsResolver dnsResolver) {
        super(schemeRegistry, timeToLive, tunit, dnsResolver);
    }
    public CleanShutdownPoolingClientConnectionManager(SchemeRegistry schemeRegistry, long timeToLive, TimeUnit tunit) {
        super(schemeRegistry, timeToLive, tunit);
    }
    public CleanShutdownPoolingClientConnectionManager(SchemeRegistry schreg) {
        super(schreg);
    }
    /**
     * Hard limit time for shutting down the connection manager.
     * Once hit the shutdown thread is killed via {@link Thread#stop()}.
     * Defaults to 45s (45000ms)
     *
     * @param shutdownThreadKillTime Hard limit for shutting down the connection manager
     */
    public void setShutdownThreadKillTime(int shutdownThreadKillTime) {
        this.shutdownThreadKillTime = shutdownThreadKillTime;
    }
    /**
     * Limit after which the shutdown thread is repeatedly interrupted.
     * Defaults to 30s (30000ms)
     *
     * @param shutdownThreadMaxTime Limit after which the shutdown thread is interrupted
     */
    public void setShutdownThreadMaxTime(int shutdownThreadMaxTime) {
        this.shutdownThreadMaxTime = shutdownThreadMaxTime;
    }
    /**
     * Limit of time the shutdown thread is allowed to stay in {@link State#BLOCKED}, {@link State#WAITING}, or
     * {@link State#TIMED_WAITING}. Once hit the thread is interrupted and the timer is reset.
     * Defaults to 1s (1000ms)
     *
     * @param shutdownThreadMaxWaitTime Limit of time the shutdown thread is allowed to stay in {@link State#BLOCKED}, {@link State#WAITING}, or
     * {@link State#TIMED_WAITING}
     */
    public void setShutdownThreadMaxWaitTime(int shutdownThreadMaxWaitTime) {
        this.shutdownThreadMaxWaitTime = shutdownThreadMaxWaitTime;
    }
    /**
     * Rate at which the state of the shutdown thread is polled and/or interrupted.
     * Defaults to 5ms
     *
     * @param shutdownThreadPollRate Rate at which the state of the shutdown thread is polled and/or interrupted
     */
    public void setShutdownThreadPollRate(int shutdownThreadPollRate) {
        this.shutdownThreadPollRate = shutdownThreadPollRate;
    }

    @Override
    public void shutdown() {
        if (shutdownComplete.get() || !this.shutdownLock.tryLock()) {
            //Already shutdown or shutdown in progress
            return;
        }
        
        try {
            //Create Thread to call shutdown
            final Thread shutdownThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("PoolingClientConnectionManager shutdown started");
                        CleanShutdownPoolingClientConnectionManager.super.shutdown();
                    }
                    finally {
                        shutdownComplete.set(true);
                        logger.info("PoolingClientConnectionManager shutdown complete");
                    }
                }
            });
            shutdownThread.setName("PoolingClientConnectionManager Shutdown Monitor");
            shutdownThread.setDaemon(true);
            
            //start shutdown thread
            shutdownThread.start();
         
            //track initial shutdown start time and time spent by the shutdown thread waiting or blocked
            final long shutdownStart = System.nanoTime();
            long waitStart = shutdownStart;
            
            //Monitor the shutdown thread
            while (!shutdownComplete.get()) {
                final long now = System.nanoTime();
                final long shutdownTime = TimeUnit.NANOSECONDS.toMillis(now - shutdownStart);
                
                //if time spent shutting down is greater than kill time forcibly stop the shutdown thread
                if (shutdownTime > this.shutdownThreadKillTime) {
                    final String stackTrace = getStackTrace(shutdownThread);
                    logger.error("Shutdown thread " + shutdownThread.getName() + " has been stopping for " + shutdownTime + "ms, killing it. THIS IS BAD. \n" + stackTrace);
                    shutdownThread.stop();
                    
                    //break out of the monitoring loop
                    break;
                }
                //if time spent shutting down is greater than max time immediately interrupt the thread
                else if (shutdownTime > this.shutdownThreadMaxTime) {
                    logger.warn("Shutdown thread " + shutdownThread.getName() + " has been stopping for " + shutdownTime + "ms, interrupting immediately");
                    shutdownThread.interrupt();
                }
                //otherwise check the state of the thread
                else {
                    //If the thread is blocked or waiting and has been for longer than the max wait time
                    //interrupt the thread. If not in blocked or waiting state update the wait-start time
                    final State state = shutdownThread.getState();
                    switch (state) {
                        case BLOCKED:
                        case TIMED_WAITING:
                        case WAITING: {
                            final long waitTime = TimeUnit.NANOSECONDS.toMillis(now - waitStart);
                            if (waitTime > shutdownThreadMaxWaitTime) {
                                logger.info("Shutdown thread " + shutdownThread.getName() + " has been waiting for " + waitTime + "ms, interrupting");
                                shutdownThread.interrupt();
                            }
                            else {
                                break;
                            }
                        }
                        
                        default: {
                            waitStart = now;
                            break;
                        }
                    }
                }
                
                //Sleep between state checks, don't want to overload anything
                try {
                    Thread.sleep(shutdownThreadPollRate);
                }
                catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        finally {
            this.shutdownLock.unlock();
        }
    }
    
    private static String getStackTrace(Thread t) {
        final StringBuilder traceBuilder = new StringBuilder();
        
        final StackTraceElement[] trace = t.getStackTrace();
        for (final StackTraceElement element : trace) {
            traceBuilder.append("\tat ").append(element).append("\n");
        }
        
        return traceBuilder.toString();
    }
}
