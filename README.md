# GR AI — Developer & Defensive Cybersecurity Mobile Suite

**Developed and Idea by:** GM Ripon  
**WhatsApp:** [+8801911527072](https://wa.me/8801911527072) (Tap in-app for direct WhatsApp chat or phone call)

---

## 🌟 Overview

**GR AI** is a complete, private, on-device & cloud AI mobile suite designed specifically for software engineering teams and cybersecurity practitioners. 

- **100% Free & Open-Source AI Model Integration:** Download, stream, or run free and open-source models (Hugging Face, GitHub Releases, Ollama local LAN, Gemini Flash/Pro).
- **Google Account & Google Drive Cloud Storage:** Real Google Account OAuth integration (`directed-strata-503219-e4`) with `drive.file` and `drive.appdata` scopes. Offload massive AI weights, custom team rules, RAG archives, and chat history to 15GB of free Google Drive cloud space without filling physical device storage.
- **Real User Live Chat with Voice & Text:** Live talking via speech recognition and text-to-speech engine alongside code-aware markdown streaming and visual input analysis.
- **Defensive Cybersecurity & DevSecOps Tools:** Offline vulnerability analyzer (CWE-798, CWE-89, CWE-79, CWE-22, CWE-502), cryptographic hasher, subnet mask calculator, base64 encoder, and OWASP Top 10 guides.
- **Custom Team Governance Rules:** Enforce team security, coding standards, and architectural rules across all prompts and code generation.

---

## 📱 Android Compatibility

- **Minimum SDK:** Android 7.0 Nougat (API 24)
- **Target / Compile SDK:** Android 16 / 17 (API 36 Extension 1)
- Compatible with all modern Android versions from Android 7.0 through Android 15, 16, and upcoming Android 17.

---

## 🚀 How to Import into Android Studio & Export APK

### 1. Clone from GitHub
```bash
git clone https://github.com/<YOUR_GITHUB_USERNAME>/GR_AI.git
cd GR_AI
```

### 2. Open in Android Studio
1. Open **Android Studio** (Koala, Ladybug, Meerkat, or newer).
2. Click **File > Open...** and select the root directory of the cloned project.
3. Android Studio will automatically recognize the Gradle build files and sync the dependencies.

### 3. Direct APK Export (Debug or Release)
- **Direct Debug APK:**
  - In Android Studio menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
  - Once finished, click **locate** in the notification popup to get the generated `.apk` file:
    `app/build/outputs/apk/debug/app-debug.apk`
- **Install Directly to Phone via USB / Wireless Debugging:**
  - Connect your Android device with Developer Options & USB Debugging enabled.
  - Press the green **Run (Play)** button in Android Studio.
- **Direct Command-Line Build:**
  ```bash
  gradle assembleDebug
  ```

---

## ☁️ Google Account & Google Drive Setup

1. **OAuth Project:** Configured under Google Cloud Project `directed-strata-503219-e4` (Client ID `489759037793-k53vkat7e5ujld1a9cjj3uj4iljruvlp.apps.googleusercontent.com`).
2. **Authorized Scopes:**
   - `https://www.googleapis.com/auth/drive.file`
   - `https://www.googleapis.com/auth/drive.appdata`
3. In the app:
   - Navigate to the **Settings** or **Models > Google Drive** tab.
   - Tap **Connect Google Account** with your Google email (e.g. `gmripon703@gmail.com`).
   - Enable **"Offload Large AI Files to Google Drive"** to preserve local phone flash storage.
   - Use one-tap buttons to backup **Team Rules**, **RAG Knowledge Base**, and **Chat Logs** directly to Google Drive.

---

## 💬 Developer & Contact Attribution

- **Developed and Idea by:** GM Ripon
- **WhatsApp Direct Link:** https://wa.me/8801911527072
- **Direct Phone Dial:** +8801911527072
- Built with Kotlin, Jetpack Compose, Material Design 3, Room Database, and OkHttp.
