# Database & Firebase Setup Guide

This document explains how to configure SQL Server and Firebase for the Synthex backend.

## Default: Mock Mode

By default, the backend runs in **mock mode** — no SQL Server or Firebase is needed. All data is stored in-memory and will be lost when the server restarts. This is perfect for development and testing.

---

## SQL Server Setup

### 1. Create the Database

Open SQL Server Management Studio (SSMS) or `sqlcmd`:

```sql
CREATE DATABASE SynthexDB;
```

### 2. Run the Schema Script

```powershell
sqlcmd -S localhost -d SynthexDB -i db/sqlserver/001_create_synthex_tables.sql
```

Or open `db/sqlserver/001_create_synthex_tables.sql` in SSMS and execute it.

### 3. Set Environment Variables

In PowerShell, before running the backend:

```powershell
$env:SYNTHEX_USE_REAL_LOCAL_DB = "true"
$env:SYNTHEX_SQL_URL = "jdbc:sqlserver://localhost:1433;databaseName=SynthexDB;encrypt=true;trustServerCertificate=true"
$env:SYNTHEX_SQL_USER = "sa"
$env:SYNTHEX_SQL_PASSWORD = "YourPasswordHere"
```

> **IMPORTANT:** Never commit your SQL password. Always use environment variables.

### 4. Verify

Start the backend and check the console output:

```
[Synthex] SqlServerLocalDBRepository initialized.
```

If it falls back:

```
[Synthex] SQL Server unavailable. Falling back to MockLocalDBRepository.
```

Check your connection string, username, and password.

---

## Firebase / Firestore Setup

### 1. Get Your Service Account Key

1. Go to [Firebase Console](https://console.firebase.google.com/) → Your Project → Project Settings → Service Accounts.
2. Click "Generate new private key".
3. Save the file as `serviceAccountKey.json` in the project root (or anywhere you prefer).

> **IMPORTANT:** This file is already in `.gitignore`. Never commit it.

### 2. Set Environment Variables

```powershell
$env:SYNTHEX_USE_REAL_CLOUD_DB = "true"
$env:SYNTHEX_FIREBASE_SERVICE_ACCOUNT_PATH = "serviceAccountKey.json"
```

Or use an absolute path:

```powershell
$env:SYNTHEX_FIREBASE_SERVICE_ACCOUNT_PATH = "C:\path\to\serviceAccountKey.json"
```

### 3. Verify

Start the backend and check the console output:

```
[FirebaseCloudDB] Firestore initialized successfully.
[Synthex] FirebaseCloudDB initialized.
```

If it falls back:

```
[Synthex] Firebase unavailable. Falling back to MockCloudDB.
```

Check that the file path is correct and the key is valid.

## Exact Run Order for Testing

To test the real setup from scratch, follow these steps in order:

1. Create DB (`CREATE DATABASE SynthexDB;`)
2. Run schema script (`SQLQuery1.sql` then `001_create_synthex_tables.sql`)
3. Set env vars (`setup-env.ps1`)
4. Run backend (`.\mvnw.cmd exec:java`)
5. Run tests (`.\scripts\test-db-firebase.ps1`)

---

## Firestore Collections

The backend uses these Firestore collections:

| Collection       | Document ID    | Fields                                    |
|------------------|----------------|-------------------------------------------|
| `leaderboard`    | userID         | userId, username, score, lastUpdated       |
| `friendRequests` | auto-generated | From, To, Status, timestamp                |
| `friends`        | auto-generated | UserID, FriendID, Since                    |

---

## Environment Variables Reference

| Variable                              | Default                         | Description                      |
|---------------------------------------|----------------------------------|----------------------------------|
| `SYNTHEX_USE_REAL_LOCAL_DB`           | `false`                          | Enable SQL Server                |
| `SYNTHEX_SQL_URL`                     | `jdbc:sqlserver://localhost:1433;...` | JDBC connection URL          |
| `SYNTHEX_SQL_USER`                    | `sa`                             | SQL Server username              |
| `SYNTHEX_SQL_PASSWORD`               | (empty)                          | SQL Server password              |
| `SYNTHEX_USE_REAL_CLOUD_DB`          | `false`                          | Enable Firebase/Firestore        |
| `SYNTHEX_FIREBASE_SERVICE_ACCOUNT_PATH` | `serviceAccountKey.json`      | Path to Firebase service key     |

---

## Falling Back to Mock Mode

To run in full mock mode (no external services needed):

```powershell
# Either don't set any env vars, or explicitly:
$env:SYNTHEX_USE_REAL_LOCAL_DB = "false"
$env:SYNTHEX_USE_REAL_CLOUD_DB = "false"
```

The backend will use in-memory storage for everything.
