package org.opennms.newts.cassandra;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Named;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ShutdownFuture;
import com.datastax.driver.core.exceptions.DriverException;


public class CassandraSession {

    private final Session m_session;

    @Inject
    public CassandraSession(@Named("cassandra.keyspace") String keyspace, @Named("cassandra.hostname") String hostname, @Named("cassandra.port") int port) {
        checkNotNull(keyspace, "keyspace argument");
        checkNotNull(hostname, "hostname argument");
        checkArgument(port > 0 && port < 65535, "not a valid port number: %d", port);

        Cluster cluster = Cluster.builder().withPort(port).addContactPoint(hostname).build();
        m_session = cluster.connect(keyspace);

    }

    public ResultSet execute(String statement) {
        try                           {  return m_session.execute(statement);  }
        catch (DriverException excep) {  throw new CassandraException(excep);  }
    }

    public Future<Void> shutdown() {
        final ShutdownFuture future = m_session.shutdown();

        return new Future<Void>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                return future.get();
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return future.get(timeout, unit);
            }
        };

    }

}