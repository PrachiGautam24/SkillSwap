# SkillSwap — Peer-to-Peer Student Skill Exchange (Java Swing + H2)

A desktop application built with **Pure Java (JDK 8+)**, **Java Swing/AWT**, **H2 Embedded Database (JDBC)**, and **BCrypt** password hashing following the **MVC architecture**.

## 🌟 Features
- **Authentication**: Student registration & login with BCrypt password hashing with comprehensive validation
- **Dashboard Hub**: Real-time statistics cards, campus announcements, and notification badges for pending items
- **My Sessions**: View bookings you made or host with capacity management (1-100 participants)
- **Smart Peer Matching**: Calculates compatibility scores (0–100%) based on complementary skills and learning modes
- **Skill Exchange Requests**: Send, accept, or reject direct peer skill swap proposals
- **Bidirectional Ratings**: Both learner and tutor can rate each other after sessions (1–5 stars)
- **Leaderboard**: Top 10 peer mentor rankings based on average ratings
- **Profile Management**: Update skills, learning preferences, and bio with validation
- **Automatic Messaging**: Notifications for all booking status changes
- **Date/Time Pickers**: User-friendly spinners prevent past date bookings
- **Database Viewer**: Built-in H2 Console access for data inspection

## 🚀 Quick Start for New Users

### Prerequisites
- **Java JDK 8 or newer** installed on your system
  - Check if you have Java: `java -version`
  - If not, download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)

### Step 1: Clone the Repository
```bash
git clone https://github.com/PrachiGautam24/SkillSwap.git
cd SkillSwap
```

### Step 2: Build and Run

**Windows:**
```bash
.\build.bat
```

**macOS/Linux:**
```bash
chmod +x build.sh
bash build.sh
```

The build script will:
1. Compile all Java files
2. Automatically launch the SkillSwap application
3. Create the database on first run

### Step 3: Login with Demo Account
On first launch, demo accounts are auto-created:
- **Email**: `alice@uni.edu` | **Password**: `pass123`
- **Email**: `bob@uni.edu` | **Password**: `pass123`

**That's it!** You're ready to explore SkillSwap! 🎉

---

## 📋 Detailed Setup Instructions

### Manual Build (Alternative to build scripts)

**Compile:**
```bash
# Windows PowerShell
if (Test-Path "bin") { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory -Path bin
javac -d bin -cp "lib/*" -encoding UTF-8 (Get-ChildItem -Recurse -Filter "*.java" -Path src).FullName

# macOS/Linux
rm -rf bin
mkdir bin
javac -d bin -cp "lib/*" -encoding UTF-8 $(find src -name "*.java")
```

**Run:**
```bash
# Windows
java -cp "bin;lib/*" com.skillswap.Main

# macOS/Linux
java -cp "bin:lib/*" com.skillswap.Main
```

---

## 🗄️ Viewing the Database

SkillSwap includes tools to view and query the database:

### Quick Method
```bash
# Windows
.\ViewDatabase.bat

# macOS/Linux
chmod +x ViewDatabase.sh
./ViewDatabase.sh
```

This opens H2 Console in your browser. Use these connection settings:
- **JDBC URL**: `jdbc:h2:~/skillswapdb;MODE=MySQL;AUTO_SERVER=TRUE`
- **User Name**: `sa`
- **Password**: (leave empty)

For detailed database viewing instructions and SQL queries, see [HOW_TO_VIEW_DATABASE.md](HOW_TO_VIEW_DATABASE.md)

---

## 🎯 Key Features Explained

### Bidirectional Ratings System
After a session is completed, **both** the learner and tutor can rate each other independently. This ensures fair feedback for all participants.

### Session Capacity Management
When creating a session, hosts can set a maximum capacity (1-100 participants). The system prevents overbooking automatically.

### Date/Time Pickers
Create sessions using intuitive spinner controls that prevent selecting past dates. No more manual typing or date validation errors!

### Dashboard Notification Badges
See at a glance:
- **[n]** pending bookings for sessions you're hosting
- **[n]** unread messages
- **[n]** pending exchange requests

### Automatic Messaging
Learners receive automatic notifications when:
- Booking is accepted
- Booking is rejected
- Booking is completed
- Booking is cancelled
- Session is deleted by host

---

## 📁 Project Structure

```
SkillSwap/
├── src/com/skillswap/
│   ├── dao/              # Data Access Objects (database layer)
│   ├── model/            # Data models (User, Session, Booking, etc.)
│   ├── util/             # Utilities (PasswordUtil, ValidationUtil, SwingUtils)
│   ├── view/             # UI components (Frames and Dialogs)
│   └── Main.java         # Application entry point
├── lib/                  # External libraries (H2, jBCrypt)
├── bin/                  # Compiled classes (generated)
├── build.bat             # Windows build script
├── build.sh              # Unix/Linux build script
├── ViewDatabase.bat      # Database viewer for Windows
├── ViewDatabase.sh       # Database viewer for Unix/Linux
└── README.md             # This file
```

---

## 🛠️ Technologies Used

- **Java 8+**: Core programming language
- **Java Swing/AWT**: GUI framework
- **H2 Database 1.4.200**: Embedded SQL database
- **jBCrypt**: Password hashing library
- **JDBC**: Database connectivity
- **MVC Architecture**: Clean separation of concerns

---

## 🧪 Testing

The project includes test files in the `tests/` directory. Run tests with:

```bash
javac -d bin -cp "bin;lib/*" tests/*.java
java -cp "bin;lib/*" BookingFlowTest
```

---

## 🐛 Known Issues & Fixes

All critical bugs have been fixed in the latest version:
- ✅ Bidirectional ratings implemented
- ✅ Session capacity limits enforced
- ✅ Past date booking prevention
- ✅ Automatic messaging for all booking changes
- ✅ Profile validation (email, password, name)
- ✅ Duplicate booking prevention
- ✅ Dashboard notification badges
- ✅ Session deletion safety

For a complete list of fixes, see the commit history.

---

## 📝 Database Schema

The H2 database includes these tables:
- **users**: User accounts with skills, ratings, and preferences
- **sessions**: Skill-sharing sessions with capacity limits
- **bookings**: Session bookings with status tracking
- **ratings**: Bidirectional ratings (learner ↔ tutor)
- **messages**: In-app messaging system
- **exchange_requests**: Direct skill exchange proposals
- **announcements**: Campus-wide announcements

Database location: `~/skillswapdb.mv.db` (in your home directory)

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📧 Support

If you encounter any issues:
1. Check the [HOW_TO_VIEW_DATABASE.md](HOW_TO_VIEW_DATABASE.md) guide
2. Review the demo credentials above
3. Ensure Java JDK 8+ is properly installed
4. Check that all `.jar` files exist in the `lib/` folder

---

## 📜 License

This project is open source and available for educational purposes.

---

## 🎓 Demo Accounts

**Alice** (Experienced Mentor):
- Email: `alice@uni.edu`
- Password: `pass123`
- Skills Offered: Java, Python, Web Development
- Skills Wanted: Machine Learning, Data Science

**Bob** (Active Learner):
- Email: `bob@uni.edu`
- Password: `pass123`
- Skills Offered: JavaScript, React
- Skills Wanted: Backend Development, Databases

---

## ✨ Latest Updates

**Version 2.0** (Current)
- Bidirectional ratings system
- Session capacity management
- Date/time pickers with past date prevention
- Dashboard notification badges
- Automatic messaging system
- Profile validation
- Database viewing tools

**Version 1.0** (Initial Release)
- Basic authentication and profile management
- Session creation and booking
- Skill matching algorithm
- Rating and leaderboard system
