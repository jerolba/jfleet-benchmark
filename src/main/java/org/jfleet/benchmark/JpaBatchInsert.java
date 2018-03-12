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
import java.util.Iterator;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jfleet.benchmark.shared.BenchmarkMeter;
import org.jfleet.benchmark.shared.CityBikeParser;
import org.jfleet.benchmark.shared.CityBikeReader;
import org.jfleet.benchmark.shared.DataSourceFactory;
import org.jfleet.benchmark.shared.EntityManagerFactoryFactory;
import org.jfleet.benchmark.shared.TableHelper;
import org.jfleet.benchmark.shared.TripEntityJpa;

/*
 * Persist all information in PostgreSQL or MySQL using JPA batched inserts.
 */
public class JpaBatchInsert {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaBatchInsert.class);

    public static void main(String[] args) throws IOException, SQLException {
        String properties = args[0];
        int batchSize = Integer.parseInt(args[1]);
        DataSourceFactory dsFactory = new DataSourceFactory(properties);
        DataSource dataSource = dsFactory.get();
        try (Connection conn = dataSource.getConnection()) {
            TableHelper.createTable(conn);
        }

        EntityManagerFactoryFactory factory = new EntityManagerFactoryFactory(dsFactory, TripEntityJpa.class) {
            @Override
            public Properties properties() {
                Properties properties = super.properties();
                properties.put("hibernate.jdbc.batch_size", batchSize);
                return properties;
            }
        };
        EntityManager entityManager = factory.newEntityManagerFactory().createEntityManager();

        CityBikeParser<TripEntityJpa> parser = new CityBikeParser<>(() -> new TripEntityJpa());
        CityBikeReader<TripEntityJpa> reader = new CityBikeReader<>("/tmp", str -> parser.parse(str));

        // Batch operations are not allowed with autoinsert id. We must generate it manually.
        int[] idSeq = new int[] { 1 };

        BenchmarkMeter meter = new BenchmarkMeter(JpaBatchInsert.class, properties, batchSize);
        meter.meter(() -> {
            long lines = reader.forEachCsvInZip(trips -> {
                entityManager.getTransaction().begin();
                int cont = 0;
                Iterator<TripEntityJpa> iterator = trips.iterator();
                while (iterator.hasNext()) {
                    TripEntityJpa trip = iterator.next();
                    trip.setId(idSeq[0]++);
                    entityManager.persist(trip);
                    cont++;
                    if (cont % batchSize == 0) {
                        LOGGER.info("Flushing. Total elements " + cont);
                        entityManager.flush();
                        entityManager.clear();
                    }
                }
                entityManager.flush();
                entityManager.clear();
                entityManager.getTransaction().commit();
            });
            entityManager.close();
            return lines;
        });
    }

}
