/**
 * Copyright 2018 Jerónimo López Bezanilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jfleet.benchmark;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.jfleet.BulkInsert;
import org.jfleet.JFleetException;
import org.jfleet.mysql.LoadDataBulkInsert;
import org.jfleet.mysql.LoadDataBulkInsert.Configuration;
import org.jfleet.benchmark.shared.BenchmarkMeter;
import org.jfleet.benchmark.shared.CityBikeParser;
import org.jfleet.benchmark.shared.CityBikeReader;
import org.jfleet.benchmark.shared.ConnectionProvider;
import org.jfleet.benchmark.shared.TableHelper;
import org.jfleet.benchmark.shared.TripEntity;

/*
 * Persist all information in MySQL using LoadDataBulkInsert implementation.
 */
public class LoadDataInsert {

    public static void main(String[] args) throws IOException, SQLException {
        String properties = args[0];
        int size = Integer.parseInt(args[1]) * 1024 * 1024;
        Supplier<Connection> connectionSuplier = new ConnectionProvider(properties);
        try (Connection connection = connectionSuplier.get()) {
            TableHelper.createTable(connection);

            CityBikeParser<TripEntity> parser = new CityBikeParser<>(() -> new TripEntity());
            CityBikeReader<TripEntity> reader = new CityBikeReader<>("/tmp", str -> parser.parse(str));

            Configuration<TripEntity> cfg = new Configuration<>(TripEntity.class);
            cfg.batchSize(size);
            BulkInsert<TripEntity> bulkInsert = new LoadDataBulkInsert<>(cfg);

            BenchmarkMeter meter = new BenchmarkMeter(LoadDataInsert.class, properties, size);
            meter.meter(() -> {
                return reader.forEachCsvInZip(trips -> {
                    try {
                        bulkInsert.insertAll(connection, trips);
                    } catch (JFleetException | SQLException e) {
                        e.printStackTrace();
                    }
                });
            });
        }
    }

}
