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
- **GET** `/trainer/get/record/latest` - Get the latest exercise record for a specific client, equipment, and exercise

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