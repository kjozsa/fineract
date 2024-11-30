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

    doFirst {
        println("Preprocessing Avro schemas from $avroSourceDir to $avroPreProcessedDir")
        file(avroPreProcessedDir).mkdirs()
    }

    doLast {
        copy {
            from(avroSourceDir)
            into(avroPreProcessedDir)
            filter { line: String ->
                line.replace("\"bigdecimal\"", file("$projectDir/src/main/resources/avro-templates/bigdecimal.avsc").readText(Charsets.UTF_8))
            }
        }
        println("Preprocessing complete. Files in target directory: ${file(avroPreProcessedDir).list()?.joinToString()}")
    }
}

tasks.register<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    dependsOn("preprocessAvroSchemas")
    inputs.dir(avroPreProcessedDir)
    outputs.dir(avroGeneratedSourcesDir)

    source(avroPreProcessedDir)
    setOutputDir(file(avroGeneratedSourcesDir))

    doFirst {
        println("Generating Avro Java classes from $avroPreProcessedDir to $avroGeneratedSourcesDir")
        file(avroGeneratedSourcesDir).mkdirs()
    }

    doLast {
        println("Generation complete. Generated files: ${file(avroGeneratedSourcesDir).walk().filter { it.isFile }.joinToString()}")
    }
}

avro {
    templateDirectory = "$projectDir/src/main/resources/avro-generator-templates/"
}

sourceSets {
    main {
        java {
            srcDir(avroGeneratedSourcesDir)
        }
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

gradle.projectsEvaluated {
    tasks.named("generateAvroJava") {
        outputs.dir(avroGeneratedSourcesDir)
        outputs.cacheIf { false }
    }
}

tasks.named("processResources") {
    dependsOn("generateAvroJava")
    mustRunAfter("generateAvroJava")
}

tasks.named("compileJava") {
    dependsOn("generateAvroJava")
    mustRunAfter("generateAvroJava")
    inputs.dir(avroPreProcessedDir)
    outputs.dir(avroGeneratedSourcesDir)
    doFirst {
        // Ensure the directory exists
        file(avroGeneratedSourcesDir).mkdirs()
    }
}

tasks.named("jar") {
    dependsOn("compileJava")
    mustRunAfter("compileJava")
    inputs.dir(avroGeneratedSourcesDir)
}

allprojects {
    tasks.all {
        outputs.upToDateWhen { false }
    }
}

tasks.register("cleanGeneratedSources") {
    delete(avroPreProcessedDir, avroGeneratedSourcesDir)
}

tasks.named("clean") {
    dependsOn("cleanGeneratedSources")
}

tasks.named("licenseMain") {
    dependsOn(tasks.named("generateAvroJava"))
    dependsOn(tasks.named("compileJava"))
    mustRunAfter("compileJava")
}

tasks.withType<JavaCompile>().configureEach {
    options.isIncremental = false
}

// Custom task to ensure Avro generation is complete
tasks.register("ensureAvroGenerated") {
    dependsOn("generateAvroJava")
    
    doFirst {
        println("Verifying Avro generation...")
        val outputDir = layout.buildDirectory.dir("generated-src/avro/main").get().asFile
        println("Output directory exists: ${outputDir.exists()}")
        println("Output directory contents: ${outputDir.walk().filter { it.isFile }.joinToString()}")
    }
    
    doLast {
        val outputDir = layout.buildDirectory.dir("generated-src/avro/main").get().asFile
        if (!outputDir.exists() || outputDir.list()?.isEmpty() != false) {
            throw GradleException("Avro sources were not generated properly in $outputDir")
        }
        
        // Verify specific expected files exist
        val expectedFile = layout.buildDirectory.file("generated-src/avro/main/org/apache/fineract/avro/BulkMessageItemV1.java").get().asFile
        if (!expectedFile.exists()) {
            println("Failed to find $expectedFile")
            println("Directory contents:")
            outputDir.walk().forEach { println(it) }
            throw GradleException("Critical Avro file BulkMessageItemV1.java was not generated")
        }
        println("Avro generation verification complete - all required files present")
    }
}

tasks.named("compileJava") {
    dependsOn("ensureAvroGenerated")
    mustRunAfter("ensureAvroGenerated")
    inputs.dir(avroPreProcessedDir)
    outputs.dir(layout.buildDirectory.dir("generated-src/avro/main"))
    outputs.cacheIf { false }
}

tasks.named("jar") {
    dependsOn("compileJava")
    mustRunAfter("compileJava")
    inputs.dir(layout.buildDirectory.dir("generated-src/avro/main"))
    outputs.cacheIf { false }
}

// Disable all task caching
allprojects {
    tasks.all {
        outputs.cacheIf { false }
    }
}
