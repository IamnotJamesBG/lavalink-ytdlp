plugins {
    `java-library`
}

group = "com.github.gustavowidman"
version = findProperty("version") as String
val archivesBaseName = "ytdlp"

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    implementation(libs.logger)
    implementation(libs.commonsIo)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation(libs.lavaplayer)
    implementation(libs.lavasearch)

    testImplementation(libs.lavaplayer)
    testImplementation(libs.logger.impl)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val jar: Jar by tasks
val build: Task by tasks
val clean: Task by tasks

val sourcesJar = task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allJava)
}

build.apply {
    dependsOn(jar)
    dependsOn(sourcesJar)

    jar.mustRunAfter(clean)
    sourcesJar.mustRunAfter(jar)
}

tasks.withType<Test> {
    useJUnitPlatform()
}