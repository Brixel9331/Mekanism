# Development

Mekanism Fabric is developed for Minecraft 1.20.1 using Fabric.

## Prerequisites

* Gradle - Use the included Gradle 8.11 wrapper.
* JDK - Version 17 is required to build and run the project.

## Directory Structure

* /src - Source code
    - /additions
        - /java/mekanism/additions - Code for Mekanism: Additions
        - /resources - Resources for Mekanism: Additions
    - /api/java/mekanism/api - Our API
    - /datagen
        - /additions/java/mekanism/additions - Data generators for Mekanism: Additions
        - /defense/java/mekanism/defense - Data generators for Mekanism: Defense (Planned for V11, currently placeholder)
        - /generated
            - /mekanism - Generated resources for Mekanism
            - /mekanismadditions - Generated resources for Mekanism: Additions
            - /mekanismgenerators - Generated resources for Mekanism: Generators
            - /mekanismtools - Generated resources for Mekanism: Tools
        - /generators/java/mekanism/generators - Data generators for Mekanism: Generators
        - /main/java/mekanism - Data generators for Mekanism
        - /tools/java/mekanism/tools - Data generators for Mekanism: Tools
    - /defense (Planned for V11, currently placeholder)
        - /java/mekanism/defense - Code for Mekanism: Defense
        - /resources - Resources for Mekanism: Defense
    - /gameTest
      - /main/java/mekanism - Game Tests for Mekanism
    - /generators
        - /java/mekanism/generators - Code for Mekanism: Generators
        - /resources - Resources for Mekanism: Generators
    - /main
        - /java/mekanism - Code for Mekanism
        - /resources - Resources for Mekanism
    - /tools
        - /java/mekanism/tools - Code for Mekanism: Tools
        - /resources - Resources for Mekanism: Tools
    - /test/java/mekanism - Unit Tests
* /docs - Documentation / style information

## Packaging structure

Mod jars are written to `build/libs/`. Filenames include the Minecraft and mod versions.

### Mekanism.jar

* /assets - From /src/main/resources/assets and /src/datagen/generated/mekanism/assets
* /data - From /src/main/resources/data and /src/datagen/generated/mekanism/data
* /mekanism - .class files from /src/main/java and /src/api/java
* fabric.mod.json - Fabric metadata from /src/main/resources
* LICENSE
* logo.png

### MekanismAdditions.jar

* /assets - From /src/additions/resources/assets and /src/datagen/generated/mekanismadditions/assets
* /data - From /src/additions/resources/data and /src/datagen/generated/mekanismadditions/data
* /mekanism - .class files from /src/additions/java
* fabric.mod.json - Fabric metadata from /src/additions/resources
* LICENSE
* logo.png

### MekanismGenerators.jar

* /assets - From /src/generators/resources/assets and /src/datagen/generated/mekanismgenerators/assets
* /data - From /src/generators/resources/data and /src/datagen/generated/mekanismgenerators/data
* /mekanism - .class files from /src/generators/java
* fabric.mod.json - Fabric metadata from /src/generators/resources
* LICENSE
* logo.png

### MekanismTools.jar

* /assets - From /src/tools/resources/assets and /src/datagen/generated/mekanismtools/assets
* /data - From /src/tools/resources/data and /src/datagen/generated/mekanismtools/data
* /mekanism - .class files from /src/tools/java
* fabric.mod.json - Fabric metadata from /src/tools/resources
* LICENSE
* logo.png

### Source jars

The core mod has a corresponding `-sources.jar` containing its Java sources and API sources.

## Building

Use JDK 17 and run `./gradlew build` on macOS or Linux, or `gradlew.bat build` on Windows.
The mod jars and source jars are written to `build/libs/`. Use `./gradlew runClient`
or `./gradlew runServer` to launch the Fabric development environment.
