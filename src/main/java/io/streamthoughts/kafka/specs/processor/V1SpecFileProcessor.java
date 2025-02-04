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
package io.streamthoughts.kafka.specs.processor;

import io.streamthoughts.kafka.specs.config.JikkouConfig;
import io.streamthoughts.kafka.specs.config.JikkouParams;
import io.streamthoughts.kafka.specs.error.ConfigException;
import io.streamthoughts.kafka.specs.error.JikkouException;
import io.streamthoughts.kafka.specs.extensions.ExtensionRegistry;
import io.streamthoughts.kafka.specs.extensions.ReflectiveExtensionScanner;
import io.streamthoughts.kafka.specs.model.V1SpecFile;
import io.streamthoughts.kafka.specs.model.V1SpecsObject;
import io.streamthoughts.kafka.specs.transforms.ApplyConfigMapsTransformation;
import io.streamthoughts.kafka.specs.transforms.Transformation;
import io.streamthoughts.kafka.specs.validations.NoDuplicateRolesAllowedValidation;
import io.streamthoughts.kafka.specs.validations.NoDuplicateTopicsAllowedValidation;
import io.streamthoughts.kafka.specs.validations.NoDuplicateUsersAllowedValidation;
import io.streamthoughts.kafka.specs.validations.QuotasEntityValidation;
import io.streamthoughts.kafka.specs.validations.Validation;
import io.streamthoughts.kafka.specs.validations.ValidationException;
import io.vavr.Lazy;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

public final class V1SpecFileProcessor implements Processor<V1SpecFileProcessor> {

    private static final Logger LOG = LoggerFactory.getLogger(V1SpecFileProcessor.class);

    private List<Lazy<Transformation>> transformations = List.of();
    private List<Lazy<Validation>> validations = List.of();

    private final ExtensionRegistry registry = new ExtensionRegistry();

    public static V1SpecFileProcessor create(final @NotNull JikkouConfig config) {
        var processor = new V1SpecFileProcessor()
                .withTransformation(Lazy.of(ApplyConfigMapsTransformation::new))
                .withValidation(Lazy.of(NoDuplicateTopicsAllowedValidation::new))
                .withValidation(Lazy.of(NoDuplicateUsersAllowedValidation::new))
                .withValidation(Lazy.of(NoDuplicateRolesAllowedValidation::new))
                .withValidation(Lazy.of(QuotasEntityValidation::new));
        processor.configure(config);
        return processor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final @NotNull JikkouConfig config) throws ConfigException {
        LOG.info("Configuring");
        final java.util.List<String> extensionPaths = JikkouParams.EXTENSION_PATHS
                .getOption(config)
                .getOrElse(Collections.emptyList());

        if (!extensionPaths.isEmpty()) {
            new ReflectiveExtensionScanner(registry).scan(extensionPaths);
        }

        JikkouParams.TRANSFORMATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> Lazy.of(() -> {
                    var extension = (Transformation) registry.getExtensionForClass(tuple._1());
                    extension.configure(tuple._2().withFallback(config));
                    return extension;
                }))
                .forEach(this::withTransformation);

        JikkouParams.VALIDATIONS_CONFIG.get(config)
                .stream()
                .peek(tuple -> LOG.info("Added {} with values:\n\t{}", tuple._1(), tuple._2().toPrettyString()))
                .map(tuple -> Lazy.of(() -> {
                    var extension = (Validation) registry.getExtensionForClass(tuple._1());
                    extension.configure(tuple._2().withFallback(config));
                    return extension;
                }))
                .forEach(this::withValidation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V1SpecFileProcessor withTransformation(@NotNull final Lazy<Transformation> transformation) {
        transformations = transformations.append(transformation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V1SpecFileProcessor withValidation(@NotNull final Lazy<Validation> validation) {
        validations = validations.append(validation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull V1SpecFile apply(@NotNull final V1SpecFile file) {

        // (1) run all transformations onto the given V1SpecsObject
        final V1SpecsObject v1SpecsObject = transformations
                .map(Lazy::get)
                .foldLeft(file.specs(), (specsObject, transformation) -> transformation.transform(specsObject));

        // (2) run all validations and get all ValidationExceptions
        final java.util.List<ValidationException> errors = validations
                .map(Lazy::get)
                .map(validation -> Try.run(() -> validation.validate(v1SpecsObject)))
                .map(Try::failed)
                .flatMap(Try::toOption)
                .map(throwable -> Match(throwable).of(
                        Case($(instanceOf(ValidationException.class)), t -> (ValidationException) t),
                        Case($(), t -> { throw new JikkouException(t); })
                ))
                .toJavaList();

        // (3) may throw all validation errors
        if (!errors.isEmpty()) {
            throw new ValidationException(errors).withSuffixMessage("Validation rule violations:");
        }

        return new V1SpecFile(file.metadata(), v1SpecsObject);
    }
}
