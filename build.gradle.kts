plugins {
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}