/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.operation;

import io.streamthoughts.kafka.specs.Description;
import io.streamthoughts.kafka.specs.resources.acl.AccessControlPolicy;
import io.streamthoughts.kafka.specs.change.AclChange;
import io.streamthoughts.kafka.specs.change.AclChanges;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.internal.DescriptionProvider;
import io.streamthoughts.kafka.specs.internal.FutureUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.resource.ResourcePattern;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CreateAclsOperation implements AclOperation {

    public static final DescriptionProvider<AccessControlPolicy> DESCRIPTION = (r) -> (Description.Create) () -> {
        return String.format("Create a new ACL (%s %s to %s %s:%s:%s)",
                r.permission(),
                r.principal(),
                r.operation(),
                r.resourceType(),
                r.patternType(),
                r.resourcePattern());
    };

    private final AclBindingConverter converter = new AclBindingConverter();

    private final AdminClient adminClient;

    /**
     * Creates a new {@link CreateAclsOperation} instance.
     *
     * @param adminClient   the {@link AdminClient}.
     */
    public CreateAclsOperation(@NotNull final AdminClient adminClient) {
        this.adminClient = Objects.requireNonNull(adminClient, "'adminClient should not be null'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Description getDescriptionFor(@NotNull final AclChange change) {
        return DESCRIPTION.getForResource(change.getAccessControlPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(final AclChange change) {
        return change.getOperation() == Change.OperationType.ADD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<AccessControlPolicy, CompletableFuture<Void>> apply(final @NotNull AclChanges changes) {
        List<AclBinding> bindings = changes
                .all()
                .stream()
                .map(AclChange::getAccessControlPolicy)
                .map(converter::toAclBinding)
                .collect(Collectors.toList());

        CreateAclsResult result = adminClient.createAcls(bindings);

        Map<AclBinding, KafkaFuture<Void>> values = result.values();

        return values.entrySet()
              .stream()
              .collect(Collectors.toMap(
                      e -> converter.fromAclBinding(e.getKey()),
                      e -> FutureUtils.toCompletableFuture(e.getValue()))
              );
    }

    private static class AclBindingConverter {

        AclBinding toAclBinding(final AccessControlPolicy rule) {
            return new AclBinding(
                    new ResourcePattern(rule.resourceType(), rule.resourcePattern(), rule.patternType()),
                    new AccessControlEntry(rule.principal(), rule.host(), rule.operation(), rule.permission())
            );
        }

        AccessControlPolicy fromAclBinding(final AclBinding binding) {
            String principal = binding.entry().principal();
            String[] principalTypeAndName = principal.split(":");
            ResourcePattern pattern = binding.pattern();
            return AccessControlPolicy.newBuilder()
                    .withResourcePattern(pattern.name())
                    .withPatternType(pattern.patternType())
                    .withResourceType(pattern.resourceType())
                    .withOperation(binding.entry().operation())
                    .withPermission(binding.entry().permissionType())
                    .withHost(binding.entry().host())
                    .withPrincipalName(principalTypeAndName[1])
                    .withPrincipalType(principalTypeAndName[0])
                    .build();
        }
    }
}
