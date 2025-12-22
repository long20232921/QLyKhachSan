plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.nhom6_de3_dacn"
    // SỬA LỖI: Đổi từ bản Preview sang bản ổn định 34
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nhom6_de3_dacn"
        minSdk = 24
        // SỬA LỖI: Đổi targetSdk về 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
    buildToolsVersion = "36.0.0"
}

dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.firebase:firebase-analytics")
    // Dùng BOM mới nhất theo file của bạn
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // Các thư viện từ Version Catalog (libs) của bạn
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)

    // Credential Manager (Bạn đã có, nhưng chưa dùng trong code hiện tại)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // --- QUAN TRỌNG: Thêm dòng này để code LoginActivity hoạt động ---
    // Code Java đang dùng GoogleSignInClient (Legacy) nên bắt buộc phải có thư viện này
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore:24.10.0")
// RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
// ... các thư viện khác
}