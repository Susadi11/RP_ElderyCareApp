# API Test Instructions

## Test the API directly with curl:

```bash
curl -X GET "http://192.168.1.7:8000/api/reminders/user/patient_001?status_filter=active"
```

## Expected Response Format:
The backend should return something like:
```json
{
  "status": "success",
  "message": "Retrieved X reminders",
  "data": [
    {
      "_id": "reminder_xxx",
      "user_id": "patient_001",
      "title": "Take Medicine",
      "scheduled_time": "2026-01-05T08:00:00",
      "priority": "high",
      "category": "medication",
      "status": "active",
      ...
    }
  ]
}
```

## Run this command and paste the output here:
