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
- **GET** `/trainer/download/upload-template` - Download Excel template for bulk upload
- **POST** `/trainer/bulk/upload/records` - Bulk upload exercise records from an Excel file
- **POST** `/trainer/get/record/latest` - Get the latest exercise record for a specific client, equipment, and exercise
- **POST** `/trainer/get/record/history` - Get all exercise records for a client, equipment, and exercise after a certain date

### /trainer/download/upload-template
Download an Excel template file that conforms to the bulk upload format. The template includes column headers and an example row that will be automatically ignored during upload.

**GET** `/trainer/download/upload-template`

**Response:**
- Content-Type: `application/octet-stream`
- File name: `exercise_records_template.xlsx`
- The file will be downloaded automatically

**Template Contents:**
- Header row with column names
- Example row with sample data (automatically ignored during upload)
- The example row uses `client@example.com` as the email, which will be skipped

**Notes:**
- The example row is included to show the expected format
- Any row with an email containing "example" or "sample" will be automatically skipped during upload
- You can delete the example row before uploading, or leave it - it will be ignored either way

### /trainer/bulk/upload/records
Bulk upload exercise records from an Excel file (.xlsx or .xls format).

**POST** `/trainer/bulk/upload/records`

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `file` (Excel file)

**Excel File Format:**
The Excel file should have the following columns (header row is expected):
1. **Client Email** (required) - Email address of the client
2. **Equipment Type** (required) - One of: NAUTILUS, KINESIS, KEISER, DUMBELL, BODY_WEIGHT, ARX
3. **Exercise Type** (required) - One of: BICEP_CURL, LEG_PRESS, SQUAT, CHEST_PRESS, REVERSE_FLY, OVERHEAD_PRESS, OVERHEAD_PRESS_SQUAT, BICEP_CURL_SQUAT
4. **Resistance** (optional) - Integer value, defaults to 0
5. **Seat Setting** (optional) - Integer value, defaults to 0
6. **Pad Setting** (optional) - Integer value, defaults to 0
7. **Right Arm** (optional) - Integer value, defaults to 0
8. **Left Arm** (optional) - Integer value, defaults to 0
9. **Date Time** (optional) - Date/time of the exercise. If not provided, current timestamp will be used. Format: `yyyy-MM-dd HH:mm:ss` or `yyyy-MM-dd`

**Response Example:**
```json
{
  "message": "Bulk upload completed: 10 total records, 8 successful, 2 failed",
  "totalRecords": 10,
  "successfulRecords": 8,
  "failedRecords": 2,
  "errors": [
    "Row 3: Client with email invalid@email.com not found",
    "Row 7: Invalid equipment type: INVALID_TYPE"
  ]
}
```

**Notes:**
- The endpoint processes all rows and returns a summary of successful and failed records
- Invalid rows are skipped but errors are reported in the response
- Clients must exist in the database before their exercise records can be uploaded

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