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
import java.sql.SQLException;

import org.jfleet.benchmark.shared.BenchmarkMeter;
import org.jfleet.benchmark.shared.CityBikeParser;
import org.jfleet.benchmark.shared.CityBikeReader;
import org.jfleet.benchmark.shared.TripEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * Simple application which only reads and parses all information and calculates the time spent reading preparing the information.
 */
public class Baseline {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Baseline.class);

    public static void main(String[] args) throws IOException, SQLException {
        CityBikeParser<TripEntity> parser = new CityBikeParser<>(() -> new TripEntity());
        CityBikeReader<TripEntity> reader = new CityBikeReader<>("/tmp", str -> parser.parse(str));
        BenchmarkMeter meter = new BenchmarkMeter(Baseline.class, "baseline", 0);
        meter.meter(() -> {
            return reader.forEachCsvInZip(trips -> {
                LOGGER.info("Counting records in stream " + trips.count() + " recods in zip");
            });
        });
    }

}
