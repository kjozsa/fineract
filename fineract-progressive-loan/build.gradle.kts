/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
description = "Fineract Progressive Loan"

apply(plugin = "io.freefair.lombok")

dependencies {
    implementation(project(":fineract-accounting"))
    implementation(project(":fineract-charge"))
    implementation(project(":fineract-core"))
    implementation(project(":fineract-loan"))
    implementation(project(":fineract-avro-schemas"))

    implementation("com.github.spotbugs:spotbugs-annotations")
    implementation("com.google.code.gson:gson")
    implementation("com.google.guava:guava")
    implementation("com.jayway.jsonpath:json-path")
    implementation("com.squareup.retrofit2:converter-gson")
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.glassfish.jersey.media:jersey-media-multipart")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.mapstruct:mapstruct")
    annotationProcessor("org.mapstruct:mapstruct-processor")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.hibernate")
    }
    implementation("org.eclipse.persistence:org.eclipse.persistence.jpa") {
        exclude(group = "org.eclipse.persistence", module = "jakarta.persistence")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}