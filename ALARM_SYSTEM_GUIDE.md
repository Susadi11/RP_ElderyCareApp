# Reminder Alarm System

## Overview
The ElderCare App now features a comprehensive alarm system that triggers when reminders are due. The system plays alarm music, vibrates the device, shows notifications, and displays a full-screen alarm dialog with the reminder details.

## Features

### 🔔 **Alarm Triggering**
- **Real-time**: Alarms trigger instantly when reminders are due via WebSocket connection
- **Audio**: Plays system default alarm sound continuously until dismissed
- **Vibration**: Device vibrates in a pattern to alert the user
- **Notification**: Shows high-priority notification with reminder details
- **Full-Screen Dialog**: Displays a prominent alarm dialog with all reminder information

### 📱 **Platform Support**
- **Android**: Full support with MediaPlayer, NotificationManager, and Vibrator
- **iOS**: Notification-based alarms with system sounds
- **Desktop (JVM)**: Console-based alerts
- **Web (WasmJS)**: Browser notifications and alerts

### 🎨 **Alarm Dialog**
The alarm dialog features:
- **Pulsing Animation**: Eye-catching animated alarm icon
- **Large Text**: Easy-to-read text sized for elderly users
- **Reminder Details**: 
  - Title and description
  - Category badge
  - Priority badge (with color coding)
  - Scheduled time
- **Action Buttons**:
  - ✅ **Mark as Complete**: Completes the reminder
  - ⏰ **Snooze (15 min)**: Delays the reminder
  - ❌ **Dismiss**: Closes the alarm without completing

## Implementation

### Architecture

```
┌─────────────────────────────────────────────────────┐
│              ReminderWebSocketService               │
│  (Receives alarm events from backend server)        │
└─────────────────────┬───────────────────────────────┘
                      │ Emits Reminder
                      ↓
┌─────────────────────────────────────────────────────┐
│              ReminderViewModel                      │
│  • Listens for alarm events                         │
│  • Triggers PlatformAlarmManager                    │
│  • Shows AlarmDialog                                │
└─────────────────────┬───────────────────────────────┘
                      │ Triggers
                      ↓
┌─────────────────────────────────────────────────────┐
│           PlatformAlarmManager (expect/actual)      │
│                                                     │
│  ┌──────────────┬──────────────┬─────────────┐    │
│  │   Android    │     iOS      │   Desktop   │    │
│  │ AlarmService │ UNNotif...   │   Console   │    │
│  └──────────────┴──────────────┴─────────────┘    │
└─────────────────────────────────────────────────────┘
```

### Key Components

#### 1. **AlarmService (Android)**
Located: `composeApp/src/androidMain/kotlin/.../services/AlarmService.android.kt`

Features:
- MediaPlayer for alarm sounds
- Vibrator for device vibration
- NotificationManager for notifications
- Notification channel setup for Android O+

```kotlin
val alarmService = AlarmService(context)
alarmService.triggerAlarm(reminder)  // Start alarm
alarmService.dismissAlarm()          // Stop alarm
```

#### 2. **PlatformAlarmManager (expect/actual)**
Located: `composeApp/src/commonMain/kotlin/.../services/PlatformAlarmManager.kt`

Cross-platform interface for alarm functionality:
```kotlin
expect class PlatformAlarmManager {
    fun triggerAlarm(reminder: Reminder)
    fun stopAlarm()
    fun dismissAlarm()
    fun cleanup()
}
```

#### 3. **AlarmDialog (Compose)**
Located: `composeApp/src/commonMain/kotlin/.../components/reminder/AlarmDialog.kt`

Full-screen Compose dialog with:
- Animated pulsing alarm icon
- Reminder details card
- Large, accessible buttons
- Color-coded priority and category badges

#### 4. **ReminderViewModel**
Located: `composeApp/src/commonMain/kotlin/.../viewmodel/ReminderViewModels.kt`

Manages alarm lifecycle:
```kotlin
class ReminderViewModel(private val alarmManager: PlatformAlarmManager?)
```

### Usage in ReminderScreen

```kotlin
@Composable
fun ReminderScreen() {
    val alarmManager = rememberPlatformAlarmManager()
    val viewModel = remember { ReminderViewModel(alarmManager) }
    val activeAlarm by viewModel.activeAlarm.collectAsState()
    
    // ...
    
    if (activeAlarm != null) {
        AlarmDialog(
            reminder = activeAlarm!!,
            onDismiss = { viewModel.dismissAlarm() },
            onSnooze = { minutes -> /* ... */ },
            onComplete = { /* ... */ }
        )
    }
}
```

## Android Permissions

The following permissions are required in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Alarm Sound

The system uses the device's default alarm sound:
```kotlin
val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
```

To use a custom sound:
1. Add your audio file to `composeApp/src/androidMain/res/raw/`
2. Modify `AlarmService.kt`:
```kotlin
val alarmUri = Uri.parse("android.resource://${context.packageName}/raw/your_alarm_sound")
```

## Vibration Pattern

Current pattern: `[0, 1000, 500, 1000, 500, 1000]`
- 0ms delay
- 1000ms vibrate
- 500ms pause
- Repeats...

To customize, edit in `AlarmService.kt`:
```kotlin
val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
```

## Testing

### Test Alarm Triggering
1. Start the app and navigate to Reminder screen
2. Create a reminder with a time 1-2 minutes in the future
3. Wait for the reminder time
4. The alarm should trigger with:
   - Sound playing
   - Device vibrating
   - Notification showing
   - Full-screen dialog appearing

### Backend WebSocket
Ensure your backend is running and sending WebSocket alarm events:
```json
{
  "type": "reminder",
  "data": {
    "id": "reminder_123",
    "title": "Take Medicine",
    "description": "Take your morning medication",
    "scheduled_time": "2026-01-04T08:00:00",
    "priority": "high",
    "category": "medication"
  }
}
```

## Troubleshooting

### Alarm Not Sounding
1. Check device volume settings
2. Verify alarm sound URI is valid
3. Check logcat for MediaPlayer errors
4. Ensure VIBRATE permission is granted

### Notification Not Showing
1. Check notification permissions (Android 13+)
2. Verify notification channel is created
3. Check Do Not Disturb settings
4. Review logcat for NotificationManager errors

### WebSocket Not Connecting
1. Verify backend server is running
2. Check WebSocket URL in `ReminderWebSocketService.kt`
3. Ensure network connectivity
4. Review connection state logs

## Future Enhancements
- [ ] Custom alarm sounds per reminder
- [ ] Increasing alarm volume
- [ ] Smart snooze (adaptive based on user behavior)
- [ ] Voice dismissal
- [ ] Alarm history
- [ ] Multiple alarm sounds selection

## Related Files
- `AlarmService.android.kt` - Android alarm implementation
- `PlatformAlarmManager.kt` - Cross-platform interface
- `AlarmDialog.kt` - UI component
- `ReminderViewModel.kt` - Business logic
- `ReminderScreen.kt` - Screen integration
- `ReminderWebSocketService.kt` - Real-time communication
