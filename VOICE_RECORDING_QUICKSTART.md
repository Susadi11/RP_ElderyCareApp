# 🎤 Quick Start - Voice Recording Feature

## ✨ What's New?

Your Elder Care App now supports **voice recording** for creating reminders! Just speak your reminder, and the AI will:
- Transcribe your speech using Whisper AI
- Extract time, category, and details
- Create the reminder automatically

---

## 🚀 How to Use (3 Easy Steps)

### Step 1: Open Reminder Screen
Launch the app and navigate to the Reminders tab.

### Step 2: Tap Microphone Button
Click the **🎤 microphone icon** in the top-right corner of the screen.

### Step 3: Record Your Reminder
1. Grant microphone permission (first time only)
2. Tap the **red record button**
3. Speak clearly: *"Remind me to take my medicine at 8 AM every morning"*
4. Tap **stop** when done
5. Tap **Create Reminder**

---

## 🎯 Voice Command Examples

### ✅ Good Examples

```
"Remind me to take my blood pressure medicine at 8 AM daily"
"Doctor appointment next Tuesday at 2 PM"
"Take a walk every evening at 6 PM"
"Lunch reminder at noon every day"
"Brush teeth every night at bedtime"
```

### 📋 What to Include

1. **Action**: What to do (take medicine, go to doctor, etc.)
2. **Time**: When to do it (8 AM, 2:30 PM, evening, etc.)
3. **Frequency**: How often (daily, every morning, weekly, etc.)

---

## 🔧 Setup (For Developers)

### Prerequisites
✅ Backend running on `http://localhost:8000`  
✅ Whisper AI installed on backend  
✅ Android device or emulator  

### Configuration

1. **Update IP Address** (for real devices):
   ```kotlin
   // File: ReminderApiService.kt, Line 17
   private val baseUrl = "http://YOUR_IP:8000/api/reminders"
   ```

2. **Permissions** (already added):
   - ✅ `RECORD_AUDIO`
   - ✅ `WRITE_EXTERNAL_STORAGE`

### Build and Run

```bash
# Clean and build
./gradlew clean assembleDebug

# Install on device
./gradlew installDebug
```

---

## 📱 UI Features

### Recording States

| State | Appearance | Action |
|-------|-----------|--------|
| **Ready** | Blue mic icon | Tap to start recording |
| **Recording** | Pulsing red dot + timer | Tap to stop |
| **Complete** | Green checkmark | Play, Delete, or Submit |

### Buttons

- **🎤 Record**: Start audio recording
- **⏹️ Stop**: Stop recording
- **▶️ Play**: Preview your recording
- **🗑️ Delete**: Delete and re-record
- **📤 Submit**: Create reminder from audio

---

## 🐛 Troubleshooting

### "Permission Denied"
**Problem**: Microphone permission not granted  
**Solution**: Settings → Apps → Elder Care → Permissions → Enable Microphone

### "Recording Failed"
**Problem**: Another app is using the microphone  
**Solution**: Close other apps (camera, voice recorder, etc.)

### "Upload Failed"
**Problem**: Cannot connect to backend  
**Solution**: 
1. Check backend is running: `http://YOUR_IP:8000`
2. Verify IP address in `ReminderApiService.kt`
3. Ensure device and computer are on same WiFi

### "No transcription"
**Problem**: Audio quality too low or silent  
**Solution**: 
1. Speak louder and closer to microphone
2. Reduce background noise
3. Check microphone is working in other apps

---

## 🎓 Tips for Best Results

### For Clear Transcription
1. **Speak clearly** - Don't rush
2. **Quiet environment** - Reduce background noise
3. **Close to mic** - Hold phone 6-8 inches away
4. **Natural pace** - Speak naturally, not too fast/slow

### For Better Reminder Parsing
1. **Be specific**: Include exact times ("8 AM" not "morning")
2. **Use keywords**: medicine, appointment, meal, etc.
3. **State frequency**: daily, weekly, every morning, etc.
4. **Keep it short**: 10-30 seconds is ideal

---

## 🧪 Testing

### Test Recording
1. Open voice recorder dialog
2. Tap record
3. Say: "Test recording one two three"
4. Stop recording
5. Tap play - verify you hear your voice

### Test Full Flow
1. Record: "Remind me to take medicine at 8 AM daily"
2. Submit
3. Check reminder appears in list
4. Verify:
   - Title: "Take Medication (Daily)"
   - Time: 08:00
   - Category: medication
   - Recurrence: daily

---

## 📊 Backend Requirements

Your backend must support:

### Endpoint
```
POST /api/reminders/create-from-audio
Content-Type: multipart/form-data
```

### Parameters
- `file`: Audio file (3GPP, WAV, MP3, etc.)
- `user_id`: Patient ID
- `priority`: Optional (low, medium, high, critical)
- `caregiver_ids`: Optional (comma-separated)

### Response
```json
{
  "status": "success",
  "reminder": { ... },
  "transcription": "Remind me to take medicine at 8 AM",
  "audio_file": "reminder_audio_20260104.3gp"
}
```

---

## 💡 FAQ

**Q: How long can I record?**  
A: No hard limit, but keep it under 30 seconds for best results.

**Q: What audio format is used?**  
A: 3GPP (.3gp) with AMR-NB codec - compatible with most backends.

**Q: Can I edit the transcription?**  
A: Not yet - feature coming soon! For now, re-record if needed.

**Q: Does it work offline?**  
A: Recording works offline, but requires internet to create the reminder.

**Q: What languages are supported?**  
A: Depends on your backend's Whisper configuration. Default is English.

**Q: Is my voice data stored?**  
A: Audio files are deleted after transcription. Only text is stored.

---

## 🎯 Next Steps

After setting up voice recording:

1. ✅ Test with different voice commands
2. ✅ Try various reminder types (medication, meals, etc.)
3. ✅ Check dashboard to see created reminders
4. ✅ Test alarm triggering for voice-created reminders
5. ✅ Collect user feedback

---

## 📚 Related Documentation

- [VOICE_RECORDING_GUIDE.md](VOICE_RECORDING_GUIDE.md) - Complete technical guide
- [REMINDER_SYSTEM_GUIDE.md](REMINDER_SYSTEM_GUIDE.md) - Reminder system overview
- [ALARM_SYSTEM_GUIDE.md](ALARM_SYSTEM_GUIDE.md) - Alarm functionality
- [TEST_API.md](TEST_API.md) - API testing instructions

---

## ✅ Success Checklist

- [ ] Backend is running with audio endpoint
- [ ] Microphone permission granted
- [ ] Can record and hear playback
- [ ] Can create reminder from voice
- [ ] Reminder appears in list
- [ ] Transcription is accurate
- [ ] Alarm triggers at scheduled time

---

**Last Updated**: January 4, 2026  
**Status**: ✅ Ready to Use  
**Platform**: Android (iOS/Web coming soon)

**Need help?** Check troubleshooting section or review the full technical guide.
