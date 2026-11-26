# How to View SkillSwap Database

## Quick Start (Without Opening SkillSwap App)

### Step 1: Run the Database Viewer Script
```powershell
.\ViewDatabase.bat
```

This will:
- Start the H2 Console web server
- Automatically open your browser to http://localhost:8082

### Step 2: Connect to Database
In the H2 Console browser window, enter these **exact** settings:

- **JDBC URL:** `jdbc:h2:~/skillswapdb;MODE=MySQL;AUTO_SERVER=TRUE`
- **User Name:** `sa`
- **Password:** (leave empty)

Then click **Connect**

---

## ✅ You're Done!
You can now browse all tables, run SQL queries, and export data.

---

## Common SQL Queries for Viewing Data

### View All Users
```sql
SELECT id, name, email, skills_offered, skills_wanted, avg_rating, total_ratings 
FROM users;
```

### View All Sessions
```sql
SELECT s.id, s.title, s.description, u.name as host_name, 
       s.scheduled_at, s.location, s.max_capacity
FROM sessions s
JOIN users u ON s.user_id = u.id
ORDER BY s.scheduled_at DESC;
```

### View All Bookings
```sql
SELECT b.id, b.status, 
       learner.name as learner_name,
       host.name as host_name,
       s.title as session_title,
       s.scheduled_at
FROM bookings b
JOIN users learner ON b.learner_id = learner.id
JOIN sessions s ON b.session_id = s.id
JOIN users host ON s.user_id = host.id
ORDER BY b.created_at DESC;
```

### View All Ratings
```sql
SELECT r.id, r.rating, r.review,
       rater.name as rater_name,
       rated.name as rated_user_name,
       CASE WHEN r.rated_by_learner THEN 'Learner→Tutor' ELSE 'Tutor→Learner' END as direction,
       b.id as booking_id
FROM ratings r
JOIN bookings b ON r.booking_id = b.id
JOIN users rater ON r.rater_id = rater.id
JOIN users rated ON r.rated_user_id = rated.id
ORDER BY r.created_at DESC;
```

### View Exchange Requests
```sql
SELECT e.id, e.status, e.message,
       sender.name as sender_name,
       receiver.name as receiver_name,
       e.created_at
FROM exchange_requests e
JOIN users sender ON e.sender_id = sender.id
JOIN users receiver ON e.receiver_id = receiver.id
ORDER BY e.created_at DESC;
```

### View Messages
```sql
SELECT m.id, m.content, m.is_read,
       sender.name as sender_name,
       receiver.name as receiver_name,
       m.created_at
FROM messages m
JOIN users sender ON m.sender_id = sender.id
JOIN users receiver ON m.receiver_id = receiver.id
ORDER BY m.created_at DESC;
```

---

## Exporting Data

1. Run any SQL query
2. Click **"Download CSV"** button in the results panel
3. Save the file to your computer

---

## Stopping the Database Viewer

When you're done viewing the database:
1. Go back to the PowerShell window running ViewDatabase.bat
2. Press **Ctrl+C** to stop the server
3. Close the browser tab

---

## Database Location

The SkillSwap database files are stored at:
- `C:\Users\prach\skillswapdb.mv.db` (main database file)
- `C:\Users\prach\skillswapdb.lock.db` (lock file when app is running)
- `C:\Users\prach\skillswapdb.trace.db` (trace/log file)

---

## Notes

- ✅ The database can be viewed **without running the SkillSwap app**
- ✅ The H2 Console is **read-only by default** unless you explicitly run UPDATE/DELETE queries
- ✅ You can have both the SkillSwap app and H2 Console open at the same time (AUTO_SERVER mode allows this)
- ⚠️ Be careful with DELETE or UPDATE queries - they will modify your actual data!

---

## Troubleshooting

**Problem:** "Database not found" error  
**Solution:** Make sure you use the exact JDBC URL: `jdbc:h2:~/skillswapdb;MODE=MySQL;AUTO_SERVER=TRUE`

**Problem:** Connection refused  
**Solution:** Make sure ViewDatabase.bat is still running in the PowerShell window

**Problem:** Can't see recent data  
**Solution:** Click the "Refresh" button in H2 Console or re-run your query
