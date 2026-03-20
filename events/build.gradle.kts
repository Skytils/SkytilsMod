plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.egt.defaults)
    alias(libs.plugins.egt.loom)
}

dependencies {
    modCompileOnly(libs.flk)
    implementation("io.github.llamalad7:mixinextras-fabric:0.5.0-rc.1")
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.5.0-rc.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
