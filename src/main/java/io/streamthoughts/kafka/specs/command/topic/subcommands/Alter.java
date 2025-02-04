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
package io.streamthoughts.kafka.specs.command.topic.subcommands;

import io.streamthoughts.kafka.specs.change.TopicChangeOptions;
import io.streamthoughts.kafka.specs.command.topic.TopicsCommand;
import io.streamthoughts.kafka.specs.operation.topics.AlterTopicOperation;
import io.streamthoughts.kafka.specs.operation.topics.TopicOperation;
import org.apache.kafka.clients.admin.AdminClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "alter",
        description = "Alter the topic configurations as describe in the specification file."
)
public class Alter extends TopicsCommand.Base {

    @CommandLine.Option(
            names = "--delete-orphans",
            defaultValue = "false",
            description = "Delete config entries overridden on the cluster but absent from the specification file"
    )
    Boolean deleteOrphans;

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicOperation getOperation(@NotNull final AdminClient client) {
        return new AlterTopicOperation(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicChangeOptions getOptions() {
        return new TopicChangeOptions().withDeleteConfigOrphans(deleteOrphans);
    }
}
