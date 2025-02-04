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
package io.streamthoughts.kafka.specs.change;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;

import java.util.Objects;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AclChange implements Change<AccessControlPolicy> {

    private final OperationType operation;

    private final AccessControlPolicy policy;

    static AclChange delete(final AccessControlPolicy policy) {
        return new AclChange(OperationType.DELETE, policy);
    }

    static AclChange add(final AccessControlPolicy policy) {
        return new AclChange(OperationType.ADD, policy);
    }

    static AclChange none(final AccessControlPolicy policy) {
        return new AclChange(OperationType.NONE, policy);
    }

    /**
     * Creates a new {@link AclChange} instance.
     *
     * @param operation the {@link OperationType}.
     * @param policy    the {@link AccessControlPolicy}.
     */
    private AclChange(final OperationType operation,
                      final AccessControlPolicy policy) {
        this.operation = Objects.requireNonNull(operation, "'operation' should not be null");
        this.policy = Objects.requireNonNull(policy, "'policy' should not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationType getOperation() {
        return operation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessControlPolicy getKey() {
        return policy;
    }

    @JsonProperty
    @JsonUnwrapped
    public AccessControlPolicy getAccessControlPolicy() {
        return policy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclChange aclChange = (AclChange) o;
        return operation == aclChange.operation && Objects.equals(policy, aclChange.policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(operation, policy);
    }
}
