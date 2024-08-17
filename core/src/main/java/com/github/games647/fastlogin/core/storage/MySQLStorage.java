/*
 * SPDX-License-Identifier: MIT
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022 games647 and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.fastlogin.core.storage;

import com.github.games647.fastlogin.core.shared.FastLoginCore;
import com.zaxxer.hikari.HikariConfig;

public class MySQLStorage extends SQLStorage {

    public MySQLStorage(FastLoginCore<?, ?, ?> core, String driver, String host, int port, String database,
                        HikariConfig config,boolean useSSL) {
        super(core,
                buildJDBCUrl(driver, host, port, database),
                setParams(config, useSSL));
    }

    private static String buildJDBCUrl(String driver, String host, int port, String database) {
        String protocol = "mysql";
        if (driver.contains("mariadb")) {
            protocol = "mariadb";
        }

        return protocol + "://" + host + ':' + port + '/' + database;
    }

    private static HikariConfig setParams(HikariConfig config, boolean useSSL) {
        // Require SSL on the server if requested in config - this will also verify certificate
        // Those values are deprecated in favor of sslMode
        config.addDataSourceProperty("useSSL", useSSL);
        config.addDataSourceProperty("requireSSL", useSSL);

        // adding paranoid hides hostname, username, version and so
        // could be useful for hiding server details
        config.addDataSourceProperty("paranoid", true);

        // enable MySQL specific optimizations
        addPerformanceProperties(config);
        return config;
    }

    private static void addPerformanceProperties(HikariConfig config) {
        // disabled by default - will return the same prepared statement instance
        config.addDataSourceProperty("cachePrepStmts", true);
        // default prepStmtCacheSize 25 - amount of cached statements
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        // default prepStmtCacheSqlLimit 256 - length of SQL
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        // default false - available in newer versions caches the statements server-side
        config.addDataSourceProperty("useServerPrepStmts", true);
        // default false - prefer use of local values for autocommit and
        // transaction isolation (alwaysSendSetIsolation) should only be enabled if always use the set* methods
        // instead of raw SQL
        // https://forums.mysql.com/read.php?39,626495,626512
        config.addDataSourceProperty("useLocalSessionState", true);
        // rewrite batched statements to a single statement, adding them behind each other
        // only useful for addBatch statements and inserts
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        // cache result metadata
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        // cache results of show variables and collation per URL
        config.addDataSourceProperty("cacheServerConfiguration", true);
        // default false - set auto commit only if not matching
        config.addDataSourceProperty("elideSetAutoCommits", true);

        // default true - internal timers for idle calculation -> removes System.getCurrentTimeMillis call per query
        // Some platforms are slow on this and it could affect the throughput about 3% according to MySQL
        // performance gems presentation
        // In our case it can be useful to see the time in error messages
        // config.addDataSourceProperty("maintainTimeStats", false);
    }
}
