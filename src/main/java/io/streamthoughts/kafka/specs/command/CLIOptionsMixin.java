/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.command;

import picocli.CommandLine.Option;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CLIOptionsMixin {

    @Option(names = "--bootstrap-servers",
            defaultValue = "",
            description = "A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.")
    public String bootstrapServer;

    @Option(names = "--command-config",
            description = "A property file containing configs to be passed to Admin Client."
    )
    public File clientCommandConfig;

    @Option(names = "--command-property",
            description = "A property file containing configs to be passed to Admin Client."
    )
    public Map<String, String> clientCommandProperties = new HashMap<>();

    @Option(names = "--config-file",
            defaultValue = "",
            description = "The configuration file.")
    public String configFile;
}
