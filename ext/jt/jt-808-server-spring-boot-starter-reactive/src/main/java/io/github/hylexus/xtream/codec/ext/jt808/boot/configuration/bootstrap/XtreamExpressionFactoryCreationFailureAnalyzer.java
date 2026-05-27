/*
 * Copyright 2024-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.bootstrap;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class XtreamExpressionFactoryCreationFailureAnalyzer
        extends AbstractFailureAnalyzer<XtreamExpressionFactoryCreationException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, XtreamExpressionFactoryCreationException exception) {
        final Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        return switch (exception.failureType) {
            case MISSING_CUSTOM_XTREAM_EXPRESSION_FACTORY_BEAN -> onCustomFactoryMissing(cause);
            case MISSING_MVEL_DEPENDENCY -> onDependencyMissing(cause, "Application failed to start because 'xtream.codec.expression.type' is set to 'mvel'", "org.mvel", "mvel2", "2.5.2.Final");
            case MISSING_AVIATOR_DEPENDENCY -> onDependencyMissing(cause, "Application failed to start because 'xtream.codec.expression.type' is set to 'aviator'", "com.googlecode.aviator", "aviator", "5.4.3");
            // noinspection UnnecessaryDefault
            default -> throw exception;
        };
    }

    private FailureAnalysis onDependencyMissing(Throwable cause, String reason, String groupId, String artifactId, String version) {
        final String description = """
                %s
                but the dependency '%s:%s:%s' is missing.""".formatted(reason, groupId, artifactId, version);
        final String action = """
                Please add the dependency to your project.
                
                Example:
                  // Gradle
                  api("%s:%s:%s")
                  // Maven
                  <dependency>
                      <groupId>%s</groupId>
                      <artifactId>%s</artifactId>
                      <version>%s</version>
                  </dependency>""".formatted(groupId, artifactId, version, groupId, artifactId, version);
        return new FailureAnalysis(description, action, cause);
    }


    private static @NonNull FailureAnalysis onCustomFactoryMissing(Throwable cause) {
        final String description = """
                Application failed to start because 'xtream.codec.expression.type' is set to 'custom',
                but no CUSTOM XtreamExpressionFactory bean was defined.""";
        final String action = """
                Please define a @Bean of type XtreamExpressionFactory in your configuration.
                
                Example:
                  @Bean
                  // @see io.github.hylexus.xtream.codec.ext.jt808.boot.configuration.XtreamExtJt808ServerAutoConfiguration#xtreamExpressionFactory
                  public XtreamExpressionFactory xtreamExpressionFactory() {
                      return new DefaultXtreamExpressionFactory(new MyCustomXtreamExpressionEngine());
                  }""";
        return new FailureAnalysis(description, action, cause);
    }
}
