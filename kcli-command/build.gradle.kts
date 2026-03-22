plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":kcli-view"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
