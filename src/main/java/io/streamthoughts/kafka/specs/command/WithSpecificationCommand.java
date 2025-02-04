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

import io.streamthoughts.kafka.specs.CLIUtils;
import io.streamthoughts.kafka.specs.change.ChangeResult;
import io.streamthoughts.kafka.specs.Printer;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.processor.V1SpecFileProcessor;
import io.vavr.Lazy;
import org.apache.kafka.clients.admin.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.Collection;

public abstract class WithSpecificationCommand<T extends Change<?>> extends BaseCommand {

    @ArgGroup(multiplicity = "1")
    SpecFileOptionsMixin specOptions;

    @Spec
    private CommandSpec spec;

    @CommandLine.Mixin
    SetOptionsMixin labelsOption;

    private final Lazy<V1SpecsObject> object = Lazy.of(() -> V1SpecFileProcessor
         .create(JikkouConfig.get())
         .apply(specOptions.parse(labelsOption))
         .specs()
    );


    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call(final AdminClient adminClient) {
        loadSpecsObject(); // ensure specification is valid.
        if (!execOptions.yes && !isDryRun()) {
            CLIUtils.askToProceed(spec);
        }
        final Collection<ChangeResult<T>> results = executeCommand(adminClient);
        Printer.print(results, execOptions.verbose, isDryRun());
        return CommandLine.ExitCode.OK;
    }

    public abstract Collection<ChangeResult<T>> executeCommand(final AdminClient adminClient);

    public boolean isDryRun() {
        return execOptions.dryRun;
    }

    public V1SpecsObject loadSpecsObject() {
        return object.get();
    }
}
