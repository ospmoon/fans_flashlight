plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "osp.moon.funsflashlight"
    compileSdk = 36

    defaultConfig {
        applicationId = "osp.moon.funsflashlight"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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

    //navigation for android:name="androidx.navigation.fragment.NavHostFragment"
    implementation(libs.navigation.fragment.ktx)

    implementation(libs.preference)
}