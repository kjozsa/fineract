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
// TODO: @vidakovic we should publish this lib to Maven Central; do in separate PR

plugins {
    java 
    id("com.github.davidmc24.gradle.plugin.avro-base") version "1.9.1"
}

description = "Fineract Avro Schemas"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.apache.avro:avro")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Werror")
}

val avroSourceDir = "$projectDir/src/main/avro"
val avroPreProcessedDir = layout.buildDirectory.dir("generated/avro/src/main/avro")
val avroGeneratedSourcesDir = layout.buildDirectory.dir("generated-src/avro/main")

tasks.register("preprocessAvroSchemas") {
    inputs.dir(avroSourceDir)
    outputs.dir(avroPreProcessedDir)

    doLast {
        copy {
            from(avroSourceDir)
            into(avroPreProcessedDir)
            filter { line: String ->
                line.replace("\"bigdecimal\"", file("$projectDir/src/main/resources/avro-templates/bigdecimal.avsc").readText(Charsets.UTF_8))
            }
        }
    }
}

tasks.register<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    dependsOn("preprocessAvroSchemas")
    inputs.dir(avroPreProcessedDir)
    outputs.dir(avroGeneratedSourcesDir)

    source(avroPreProcessedDir)
    setOutputDir(file(avroGeneratedSourcesDir))
}

avro {
    templateDirectory = "$projectDir/src/main/resources/avro-generator-templates/"
}

sourceSets {
    main {
        java.srcDir(avroGeneratedSourcesDir)
    }
}

spotless {
    json {
        target("**/*.avsc")
        simple()
    }
}

tasks.withType<Checkstyle> {
    exclude("**")
}

tasks.named("compileJava") {
    dependsOn("preprocessAvroSchemas", "generateAvroJava", "spotlessJsonApply")
}

tasks.register("cleanGeneratedSources") {
    delete(avroPreProcessedDir, avroGeneratedSourcesDir)
}

tasks.named("clean") {
    dependsOn("cleanGeneratedSources")
}

tasks.named("licenseMain") {
    dependsOn(tasks.named("generateAvroJava"))
}
