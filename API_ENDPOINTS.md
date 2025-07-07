# GF Data Server API Endpoints

## Authentication
All endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Health Check
- **GET** `/ping-data-server` - Check if the server is healthy

## Client Management
- **POST** `/trainer/new/client` - Create a new client
- **GET** `/trainer/get/clients` - Get all clients

## Exercise Records
- **POST** `/trainer/new/record` - Create a new exercise record
- **POST** `/trainer/get/record/latest` - Get the latest exercise record for a specific client, equipment, and exercise
- **POST** `/trainer/get/record/history` - Get all exercise records for a client, equipment, and exercise after a certain date

### /trainer/get/record/history
Get all exercise records for a client, for a given equipment type and exercise, after a certain date.

**POST** `/trainer/get/record/history`

**Request Body Example:**
```json
{
  "client": {
    "email": "jimbob@gmail.com"
  },
  "equipmentType": "NAUTILUS",
  "exerciseType": "BICEP_CURL",
  "afterDate": 1720310400000
}
```

- `afterDate` should be a Unix timestamp (milliseconds since epoch, UTC).

**Response Example:**
```json
{
  "message": "Successfully retrieved 3 exercise records for client Bob on NAUTILUS doing BICEP_CURL after 2025-07-06T00:00:00.000Z",
  "exerciseRecords": [
    {
      "id": 1,
      "client": { /* client object */ },
      "equipmentType": "NAUTILUS",
      "exercise": "BICEP_CURL",
      "resistance": 50,
      "seatSetting": 3,
      "padSetting": 2,
      "rightArm": 1,
      "leftArm": 1,
      "dateTime": "2025-07-06T10:30:00.000Z"
    }
    // ... more records
  ]
}
```

## Reference Data
- **GET** `/trainer/get/equipment-types` - Get all available equipment types
- **GET** `/trainer/get/exercise-types` - Get all available exercise types
- **GET** `/trainer/get/all` - Get all clients, equipment types, and exercise types in a single request

## Equipment Types
Available equipment types:
- NAUTILUS
- KINESIS
- KEISER
- DUMBELL
- BODY_WEIGHT
- ARX

## Exercise Types
Available exercise types:
- BICEP_CURL
- LEG_PRESS
- SQUAT
- CHEST_PRESS
- REVERSE_FLY
- OVERHEAD_PRESS
- OVERHEAD_PRESS_SQUAT
- BICEP_CURL_SQUAT

## Response Format
All endpoints return JSON responses with the following structure:

### For individual operations:
```json
{
  "message": "Success message",
  "trainerId": null,
  "equipmentType": null,
  "exerciseType": null,
  "client": { /* client object */ },
  "exerciseRecord": { /* exercise record object */ }
}
```

### For list operations:
```json
{
  "message": "Success message with counts",
  "clients": [ /* array of client objects */ ],
  "equipmentTypes": [ /* array of equipment enum values */ ],
  "exerciseTypes": [ /* array of exercise enum values */ ]
}
```

## Error Handling
- **401 Unauthorized** - Invalid or missing JWT token
- **404 Not Found** - Requested resource not found
- **500 Internal Server Error** - Server-side error 