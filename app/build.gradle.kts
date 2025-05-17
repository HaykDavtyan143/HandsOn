plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.haykdavtyan.handson"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.haykdavtyan.handson"
        minSdk = 32
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-firestore")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.firebaseui:firebase-ui-firestore:9.0.0")
    implementation("com.google.firebase:firebase-storage:20.2.1")
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.firebase:firebase-firestore:24.7.1")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.android.gms:play-services-base:18.2.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.1.1")
}
