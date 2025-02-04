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
package io.streamthoughts.kafka.specs.validations;

import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.config.JikkouParams;
import io.streamthoughts.kafka.specs.model.V1TopicObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TopicMinReplicationFactorValidationTest {

    TopicMinReplicationFactorValidation validation;

    @BeforeEach
    public void before() {
        validation = new TopicMinReplicationFactorValidation();
        validation.configure(JikkouConfig.create(
                Map.of(JikkouParams.VALIDATION_TOPIC_MIN_REPLICATION_FACTOR_CONFIG.path(), 1)
        ));
    }

    @Test
    public void should_throw_exception_when_min_replication_is_not_valid() {
        Assertions.assertThrows(ValidationException.class, () ->
                validation.validateTopic(new V1TopicObject("test", 1, (short) 0)));
    }

    @Test
    public void should_not_throw_exception_given_topic_with_no_replication_factor() {
        Assertions.assertDoesNotThrow(() ->
                validation.validateTopic(new V1TopicObject("test", 1, (short) -1)));
    }

    @Test
    public void should_not_throw_exception_given_topic_valid_replication_factor() {
        Assertions.assertDoesNotThrow(() ->
                validation.validateTopic(new V1TopicObject("test", 1, (short) 1)));
    }

}