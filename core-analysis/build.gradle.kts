plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":proto"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("com.google.truth:truth:1.4.4")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
