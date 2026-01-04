# Quick Setup Guide - Smart Reminder System

## ⚡ Quick Start (5 Minutes)

### Step 1: Start Backend API
Your backend should be running on `http://localhost:8000`

```bash
# If using Python backend
cd your-backend-folder
python main.py  # or uvicorn main:app --reload
```

### Step 2: Configure API URL (Android Device Only)

If testing on a real Android device (not emulator), update the API URL:

**File:** `composeApp/src/commonMain/kotlin/com/rp_elderycareapp/data/reminder/ReminderApiService.kt`

```kotlin
// Line 9: Change this
private val baseUrl = "http://localhost:8000/api/reminders"

// To your computer's IP address
private val baseUrl = "http://192.168.1.XXX:8000/api/reminders"
```

**Find your IP:**
- Windows: `ipconfig` → Look for IPv4 Address
- Mac/Linux: `ifconfig` → Look for inet address

### Step 3: Build & Install

```bash
# Build the app
./gradlew build

# Install on Android device
./gradlew installDebug
```

### Step 4: Open the App

1. Open the app on your Android phone
2. Tap the **Reminder** icon in the bottom navigation
3. Tap the **+** button to create your first reminder

## 🎯 Test Flow

### Create First Reminder:
1. Tap **+** button (top right)
2. Enter title: "Take Morning Medicine"
3. Select category: **Medication**
4. Select priority: **High**
5. Tap **Create**

### Test Voice Command:
1. Tap **microphone** icon (top right)
2. Type: "Remind me to have lunch at 12 PM daily"
3. Tap **Create**

### Respond to Reminder:
1. Tap **Respond** on any reminder card
2. Choose **"Yes, Done"** or type a response
3. View the AI analysis result

### View Analytics:
1. Tap **"View Analytics"** floating button
2. Explore your cognitive health dashboard
3. Check recommendations

### Caregiver View (Optional):
1. Navigate to Caregiver Alerts (add button to navigate)
2. View patient alerts
3. Acknowledge or resolve alerts

## 🔧 Backend Requirements

Your backend must have these endpoints running:

- ✅ `POST /api/reminders/create`
- ✅ `GET /api/reminders/user/{user_id}`
- ✅ `POST /api/reminders/respond`
- ✅ `POST /api/reminders/snooze/{reminder_id}`
- ✅ `GET /api/reminders/analytics/dashboard/{user_id}`
- ✅ `GET /api/reminders/caregiver/alerts/{caregiver_id}`

Test backend with:
```bash
curl http://localhost:8000/api/reminders/user/patient_001
```

## 🐛 Quick Fixes

**Problem:** "Connection refused"
- ✅ Check backend is running
- ✅ Use IP address, not localhost on device
- ✅ Ensure firewall allows port 8000

**Problem:** "No reminders showing"
- ✅ Check API baseUrl is correct
- ✅ Create a reminder first
- ✅ Check backend logs for errors

**Problem:** Build errors
- ✅ Run: `./gradlew clean build`
- ✅ Sync Gradle files in IDE
- ✅ Check internet connection for dependencies

## 📱 Demo User IDs

Use these IDs for testing:
- **Patient ID:** `patient_001`
- **Caregiver ID:** `caregiver_001`

## 🚀 Features Ready to Use

✅ Create reminders (form & voice)  
✅ View active/completed reminders  
✅ Respond to reminders  
✅ AI-powered response analysis  
✅ Snooze functionality  
✅ Patient dashboard with analytics  
✅ Caregiver alert center  
✅ Cognitive health tracking  
✅ Smart scheduling recommendations  

## 📞 Need Help?

Check the detailed guide: `REMINDER_SYSTEM_GUIDE.md`

---

**Ready to test! Launch the app and navigate to the Reminder tab! 🎉**
