# 🎤 Elder Care App - Smart Reminder System

A Kotlin Multiplatform project for elderly care with intelligent reminder management and **voice recording capabilities**.

---

## ✨ NEW: Voice Recording Feature!

Create reminders by simply speaking! The app will:
- 🎙️ **Record your voice** using the microphone
- 🤖 **Transcribe with AI** (Whisper)
- 📝 **Extract details** (time, category, frequency)
- ✅ **Create reminder** automatically

### Quick Links
- **[Quick Start Guide](VOICE_RECORDING_QUICKSTART.md)** - Get started in 3 steps
- **[Complete Guide](VOICE_RECORDING_GUIDE.md)** - Full technical documentation
- **[Implementation Summary](VOICE_RECORDING_IMPLEMENTATION.md)** - Developer overview

---

## 🎯 Features

### Core Features
- 📱 **Smart Reminders** - Context-aware medication and appointment reminders
- 🔔 **Alarm System** - Full-screen alarms with sound and vibration
- 🎤 **Voice Recording** - Create reminders by speaking (NEW!)
- 💬 **Natural Language** - Type or speak commands naturally
- 📊 **Analytics Dashboard** - Track adherence and cognitive health
- 👥 **Caregiver Alerts** - Real-time notifications for caregivers
- 🔄 **WebSocket Updates** - Live reminder synchronization
- 🧠 **AI Analysis** - Cognitive health monitoring

### Voice Recording Features (NEW!)
- 🎙️ **Real-time recording** with duration display
- ▶️ **Playback preview** before submitting
- 🎨 **Beautiful UI** with animations
- 🔒 **Permission handling** for microphone
- 📤 **Automatic upload** to backend
- 🤖 **AI transcription** with Whisper
- 📝 **Smart parsing** of spoken commands

---

## 🚀 Quick Start

### Prerequisites
- ✅ Backend API running on `http://localhost:8000`
- ✅ Android Studio or IntelliJ IDEA
- ✅ Android device or emulator

### Setup (2 minutes)

1. **Update IP Address** (for real devices):
   ```kotlin
   // File: ReminderApiService.kt, Line 17
   private val baseUrl = "http://YOUR_IP:8000/api/reminders"
   ```

2. **Build and Run**:
   ```bash
   ./gradlew clean assembleDebug
   ./gradlew installDebug
   ```

3. **Use Voice Recording**:
   - Grant microphone permission when prompted
   - Tap 🎤 microphone icon in Reminders screen
   - Record your reminder
   - Submit to create automatically

---

## 🎤 Voice Command Examples

```
✅ "Remind me to take my blood pressure medicine at 8 AM every morning"
✅ "Doctor appointment next Tuesday at 2 PM"
✅ "Take a walk every evening at 6 PM"
✅ "Lunch reminder at noon every day"
✅ "Brush teeth every night at bedtime"
```

---

## 📁 Project Structure

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM), Server.

### Key Directories

- **[/composeApp](./composeApp/src)** - Shared Compose Multiplatform code
  - [commonMain](./composeApp/src/commonMain/kotlin) - Platform-agnostic code
  - [androidMain](./composeApp/src/androidMain/kotlin) - Android-specific code (including audio recording)
  - [iosMain](./composeApp/src/iosMain/kotlin) - iOS-specific code
  - [jvmMain](./composeApp/src/jvmMain/kotlin) - Desktop-specific code
  - [wasmJsMain](./composeApp/src/wasmJsMain/kotlin) - Web-specific code

- **[/iosApp](./iosApp/iosApp)** - iOS application entry point

- **[/server](./server/src/main/kotlin)** - Ktor server application

- **[/shared](./shared/src)** - Code shared between all targets

### Voice Recording Files (NEW!)

**Core Implementation:**
- `AudioRecorder.kt` (commonMain) - Platform interface
- `AudioRecorder.android.kt` (androidMain) - Android implementation
- `AudioRecorderDialog.kt` (commonMain) - Recording UI
- `rememberAudioRecorder.kt` (common + platforms) - Composable helper

**Modified Files:**
- `ReminderApiService.kt` - Added audio upload endpoint
- `ReminderModels.kt` - Added AudioReminderResponse
- `ReminderViewModel.kt` - Added createReminderFromAudio
- `ReminderScreen.kt` - Integrated recording dialog
- `AndroidManifest.xml` - Added audio permissions

---

## 📚 Documentation

### User Guides
- **[VOICE_RECORDING_QUICKSTART.md](VOICE_RECORDING_QUICKSTART.md)** ⭐ Start here!
- **[QUICK_START.md](QUICK_START.md)** - App setup and configuration
- **[REMINDER_SYSTEM_GUIDE.md](REMINDER_SYSTEM_GUIDE.md)** - Reminder system overview
- **[ALARM_SYSTEM_GUIDE.md](ALARM_SYSTEM_GUIDE.md)** - Alarm functionality

### Technical Documentation
- **[VOICE_RECORDING_GUIDE.md](VOICE_RECORDING_GUIDE.md)** - Complete implementation guide
- **[VOICE_RECORDING_IMPLEMENTATION.md](VOICE_RECORDING_IMPLEMENTATION.md)** - Implementation summary
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Project details
- **[TEST_API.md](TEST_API.md)** - API testing

---

## 🔧 Technology Stack

- **Kotlin Multiplatform** - Cross-platform development
- **Jetpack Compose** - Modern Android UI
- **Ktor Client** - HTTP networking
- **Kotlinx Serialization** - JSON parsing
- **MediaRecorder API** - Audio recording (Android)
- **Whisper AI** - Voice transcription (Backend)
- **WebSocket** - Real-time updates

---

## 📱 Platform Support

| Platform | Status | Voice Recording |
|----------|--------|----------------|
| **Android** | ✅ Full Support | ✅ Fully Functional |
| **iOS** | 🚧 In Progress | 🚧 Coming Soon |
| **Web** | 🚧 In Progress | 🚧 Coming Soon |
| **Desktop** | 🚧 In Progress | 🚧 Coming Soon |

---

## 🧪 Testing

### Test Backend Connection
```bash
curl http://localhost:8000/api/reminders/user/patient_001
```

### Test Audio Upload
```bash
curl -X POST "http://localhost:8000/api/reminders/create-from-audio" \
  -F "user_id=patient_001" \
  -F "priority=high" \
  -F "file=@audio.3gp"
```

### Run Application
```bash
# Android
./gradlew installDebug

# Web (development)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

---

## 🎨 Screenshots

### Voice Recording Flow
```
[Mic Button] → [Permission] → [Recording] → [Preview] → [Submit] → [Success!]
```

### Recording States
- **Ready** - Blue mic icon, ready to record
- **Recording** - Pulsing red indicator + timer
- **Complete** - Green checkmark, playback available

---

## 🐛 Troubleshooting

### Common Issues

**Permission Denied**
- Solution: Settings → Apps → Elder Care → Permissions → Microphone

**Upload Failed**
- Check backend is running on port 8000
- Verify IP address in ReminderApiService.kt
- Ensure same WiFi network

**Poor Transcription**
- Speak clearly in quiet environment
- Check microphone is working
- Reduce background noise

**See full troubleshooting**: [VOICE_RECORDING_GUIDE.md](VOICE_RECORDING_GUIDE.md#troubleshooting)

---

## 🤝 Contributing

### Development Requirements
- Kotlin 1.9.20+
- Compose Multiplatform 1.5.0+
- Gradle 8.0+
- Android Studio Hedgehog+

### Code Style
Follow the established patterns:
- Use expect/actual for platform-specific code
- Document public APIs with KDoc
- Write comprehensive error handling
- Add logging for debugging

---

## 📄 License

[Add your license information here]

---

## 🙏 Acknowledgments

- **OpenAI Whisper** - Voice transcription
- **Kotlin Multiplatform** - Cross-platform development
- **Jetpack Compose** - Modern UI framework
- **Ktor** - Networking library

---

## 📞 Support & Resources

### Documentation
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)
- [Kotlin/Wasm](https://kotl.in/wasm/)

### Community
- Slack: [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web)
- Issues: [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP)

---

## 🚀 What's Next?

### Planned Features
- [ ] iOS voice recording support
- [ ] Web browser voice recording
- [ ] Real-time transcription
- [ ] Multi-language support
- [ ] Offline transcription
- [ ] Voice command controls

### Get Started Now!

1. **Read**: [VOICE_RECORDING_QUICKSTART.md](VOICE_RECORDING_QUICKSTART.md)
2. **Setup**: Configure IP address and build
3. **Test**: Record your first voice reminder
4. **Explore**: Check out all features

---

**Version**: 1.0.0  
**Last Updated**: January 4, 2026  
**Status**: ✅ Production Ready (Android)

🎉 **Voice recording is ready to use!**
