import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    jacoco // плагин для покрытия
}

val serverBaseUrl: String = providers
    .gradleProperty("pawinder.serverBaseUrl")
    .orElse("https://pawinderserver-production.up.railway.app/")
    .get()

val validServerUrl = serverBaseUrl.matches(
    Regex("""https?://[A-Za-z0-9.-]+(:[0-9]+)?(/.*)?""")
)

if (!validServerUrl || !serverBaseUrl.endsWith("/")) {
    throw GradleException(
        "Invalid pawinder.serverBaseUrl: expected an HTTP(S) URL with a trailing slash"
    )
}

val gitSha = providers.exec {
    commandLine("git", "rev-parse", "--short=8", "HEAD")
}.standardOutput.asText.get().trim()

val gitDirty = providers.exec {
    commandLine("git", "status", "--porcelain")
}.standardOutput.asText.get().trim().isNotEmpty()


android {
    namespace = "com.example.pawinder_11"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pawinder_11"

        buildConfigField(
            "String",
            "SERVER_BASE_URL",
            "\"$serverBaseUrl\""

        )
        buildConfigField("String", "GIT_SHA", "\"$gitSha\"")
        buildConfigField("boolean", "GIT_DIRTY", "$gitDirty")

        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }


    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
        // покрытие
        debug {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    // Retrofit — для API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // Room — для БД
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    implementation("com.google.android.material:material:1.12.0")
}