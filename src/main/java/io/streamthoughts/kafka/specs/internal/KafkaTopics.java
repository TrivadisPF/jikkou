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
package io.streamthoughts.kafka.specs.internal;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class KafkaTopics {

    public static final String KAFKA_INTERNAL_TOPICS_PREFIX = "__";

    public static final Set<String> INTERNAL_TOPICS = Set.of(
            "__consumer_offsets",
            "_schemas",
            "__transaction_state",
            "connect-offsets",
            "connect-status",
            "connect-configs"
    );

    public static boolean isInternalTopics(@NotNull final String topic) {
        return INTERNAL_TOPICS.contains(topic) || topic.startsWith(KAFKA_INTERNAL_TOPICS_PREFIX);
    }
}
