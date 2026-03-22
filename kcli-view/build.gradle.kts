plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.jetbrains.markdown)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
