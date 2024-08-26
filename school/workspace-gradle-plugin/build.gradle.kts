plugins { kotlin("jvm") version "2.0.0" }

group = properties["artifact.group"].toString()
version = properties["artifact.version"].toString()

repositories { mavenCentral() }

dependencies { testImplementation("org.jetbrains.kotlin:kotlin-test") }

tasks.test { useJUnitPlatform() }

//kotlin { jvmToolchain(22) }