/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.example.igniteoom;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSystemProperties;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataPageEvictionMode;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.processors.cache.persistence.wal.reader.StandaloneNoopCommunicationSpi;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.util.SocketUtils;

import java.net.InetAddress;
import java.util.Collections;

import static java.lang.String.format;
import static org.apache.ignite.events.EventType.EVT_CACHE_STARTED;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws InterruptedException {
        Ignite ignite = Ignition.getOrStart(igniteConfiguration());
        for (int i=0; i<10000; i++) {
            CacheConfiguration<Object, Object> configuration = new CacheConfiguration<>();
// change 100 to 10 and it works fine
            String name = "kvody" + (i % 100);
            configuration.setName(name);
            System.err.println(i + " " + name);
            IgniteCache<Object, Object> cache = ignite.getOrCreateCache(configuration);
            cache.put("" + i, "" + i);
        }
        Thread.sleep(600000);
        System.exit(0);
    }

    public static IgniteConfiguration igniteConfiguration() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName("oom");
        cfg.setIgniteHome(System.getProperty("java.io.tmpdir"));
        cfg.setPeerClassLoadingEnabled(true);
        cfg.setIncludeEventTypes(new int[] { EVT_CACHE_STARTED });
        System.setProperty(IgniteSystemProperties.IGNITE_QUIET, "true");
        System.setProperty(IgniteSystemProperties.IGNITE_UPDATE_NOTIFIER, "false");
        cfg.setGridLogger(new Slf4jLogger());
        cfg.setDiscoverySpi(tcpDiscoverySpi());
        cfg.setCommunicationSpi(new StandaloneNoopCommunicationSpi());
        cfg.setClientMode(false);
        cfg.setDataStreamerThreadPoolSize(2);
        cfg.setPublicThreadPoolSize(2);
        cfg.setSystemThreadPoolSize(2);
        cfg.setStripedPoolSize(2);
        cfg.setIgfsThreadPoolSize(2);
        cfg.setManagementThreadPoolSize(2);
        cfg.setServiceThreadPoolSize(2);
        cfg.setQueryThreadPoolSize(2);
        cfg.setPeerClassLoadingThreadPoolSize(2);
        cfg.setDataStorageConfiguration(dataStorageConfiguration());
        // setting ConnectorConfiguration to null removes these thread pools:
        // "grid-nio-worker-tcp-rest-x", "nio-acceptor-tcp-rest-#18%<instance_name>%"
        // and "session-timeout-worker-#13%<instance_name>%"
        cfg.setConnectorConfiguration(null);
        cfg.setClientConnectorConfiguration(null);
        // since this is not forming a cluster we'll minimize this value
        cfg.setRebalanceThreadPoolSize(1);

        return cfg;
    }

    private static int ignitePort = SocketUtils.findAvailableTcpPort();
    private static TcpDiscoverySpi tcpDiscoverySpi() {
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        tcpDiscoverySpi.setIpFinder(igniteIpFinder());
        tcpDiscoverySpi.setLocalPortRange(0);
        tcpDiscoverySpi.setLocalPort(ignitePort);
        String loopbackAddress = InetAddress.getLoopbackAddress().getHostAddress();
        tcpDiscoverySpi.setLocalAddress(loopbackAddress);
        return tcpDiscoverySpi;
    }

    private static TcpDiscoveryVmIpFinder igniteIpFinder() {
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(Collections.singleton(format("127.0.0.1:%s", ignitePort)));
        return ipFinder;
    }

    static DataRegionConfiguration defaultDataRegion() {
        DataRegionConfiguration dataRegionConfiguration = new DataRegionConfiguration();
        dataRegionConfiguration.setName("default");
        dataRegionConfiguration.setPersistenceEnabled(false);
        dataRegionConfiguration.setInitialSize(200 * 1024 * 1024);
// comment out setMaxSize(...) and it works fine
        dataRegionConfiguration.setMaxSize(200 * 1024 * 1024);
        dataRegionConfiguration.setMetricsEnabled(true);
        dataRegionConfiguration.setPageEvictionMode(DataPageEvictionMode.RANDOM_2_LRU);
        return dataRegionConfiguration;
    }

    static DataStorageConfiguration dataStorageConfiguration() {
        DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
        dataStorageConfiguration.setDefaultDataRegionConfiguration(defaultDataRegion());
        return dataStorageConfiguration;
    }
}
