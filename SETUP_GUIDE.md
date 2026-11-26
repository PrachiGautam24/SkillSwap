# SkillSwap Setup Guide for Beginners

This guide will help you run SkillSwap even if you've never run a Java project before!

---

## 📥 Step 1: Install Java

Before you can run SkillSwap, you need Java installed on your computer.

### Check if you already have Java:
```bash
java -version
```

If you see a version number (like "java version 1.8.0" or higher), you're good! Skip to Step 2.

### If you don't have Java:

**Windows:**
1. Go to https://adoptium.net/
2. Download "Temurin JDK 8 or newer" for Windows
3. Run the installer
4. Keep clicking "Next" with default settings
5. Restart your terminal/PowerShell

**macOS:**
1. Open Terminal
2. Install Homebrew if you don't have it:
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
3. Install Java:
   ```bash
   brew install openjdk@17
   ```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

---

## 📂 Step 2: Download SkillSwap

### Option A: Using Git (Recommended)
If you have Git installed:
```bash
git clone https://github.com/PrachiGautam24/SkillSwap.git
cd SkillSwap
```

### Option B: Download ZIP
1. Go to https://github.com/PrachiGautam24/SkillSwap
2. Click the green "Code" button
3. Click "Download ZIP"
4. Extract the ZIP file to a folder
5. Open terminal/PowerShell in that folder

---

## ▶️ Step 3: Run SkillSwap

**Windows:**
1. Open PowerShell in the SkillSwap folder
2. Run:
   ```powershell
   .\build.bat
   ```
3. Wait for compilation to finish
4. The app window will open automatically!

**macOS/Linux:**
1. Open Terminal in the SkillSwap folder
2. Run:
   ```bash
   chmod +x build.sh
   bash build.sh
   ```
3. Wait for compilation to finish
4. The app window will open automatically!

---

## 🔐 Step 4: Login

When the app opens, login with one of these demo accounts:

**Account 1:**
- Email: `alice@uni.edu`
- Password: `pass123`

**Account 2:**
- Email: `bob@uni.edu`
- Password: `pass123`

---

## 🎉 You're Done!

You should now see the SkillSwap dashboard. Try these features:
- ✅ Click "Find Matches" to see potential skill exchange partners
- ✅ Click "Create Session" to offer a skill-sharing session
- ✅ View notification badges [n] on buttons for pending items
- ✅ Edit your profile to change skills and bio

---

## 🔄 Running the App Again

You don't need to rebuild every time! Just run:

**Windows:**
```powershell
java -cp "bin;lib/*" com.skillswap.Main
```

**macOS/Linux:**
```bash
java -cp "bin:lib/*" com.skillswap.Main
```

Or simply run the build script again:
- Windows: `.\build.bat`
- macOS/Linux: `bash build.sh`

---

## 🗄️ Viewing the Database

Want to see what's in the database?

**Windows:**
```powershell
.\ViewDatabase.bat
```

**macOS/Linux:**
```bash
chmod +x ViewDatabase.sh
./ViewDatabase.sh
```

This opens a web interface where you can:
- Browse all tables
- Run SQL queries
- Export data to CSV

Connection details when browser opens:
- **JDBC URL**: `jdbc:h2:~/skillswapdb;MODE=MySQL;AUTO_SERVER=TRUE`
- **User Name**: `sa`
- **Password**: (leave empty)

For more database tips, see [HOW_TO_VIEW_DATABASE.md](HOW_TO_VIEW_DATABASE.md)

---

## ❌ Troubleshooting

### "java is not recognized"
**Problem**: Java is not installed or not in your PATH  
**Solution**: 
1. Reinstall Java (see Step 1)
2. Restart your terminal
3. Try `java -version` again

### "Unable to access jarfile"
**Problem**: You're not in the SkillSwap folder  
**Solution**: 
```bash
cd path/to/SkillSwap
```

### "Error: Could not find or load main class"
**Problem**: The project hasn't been compiled  
**Solution**: 
- Windows: Run `.\build.bat`
- macOS/Linux: Run `bash build.sh`

### "Database locked"
**Problem**: Another instance of SkillSwap is running  
**Solution**: 
1. Close all SkillSwap windows
2. Wait 5 seconds
3. Try running again

### App window doesn't open
**Problem**: GUI display issue  
**Solution**: 
1. Make sure you're running on a system with a display (not SSH)
2. Check the terminal for error messages
3. Try running in a different terminal

### Can't login with demo accounts
**Problem**: Database might be corrupted  
**Solution**: 
1. Close the app
2. Delete the database files:
   - Windows: Delete `C:\Users\YourName\skillswapdb.*`
   - macOS/Linux: Delete `~/skillswapdb.*`
3. Run the app again (database will be recreated)

---

## 🎓 For Teachers/Presenters

### Quick Demo Setup
If you want to demo this to someone:
1. Make sure Java is installed: `java -version`
2. Clone/download the repo
3. Run the build script
4. Login with `alice@uni.edu` / `pass123`
5. Show key features: dashboard badges, create session, find matches, ratings

### Sharing with Students
Students can:
1. Clone your repo: `git clone https://github.com/PrachiGautam24/SkillSwap.git`
2. Run the build script
3. Start using immediately with demo accounts

### Viewing Your Database During Demo
1. Keep SkillSwap running
2. Run `ViewDatabase.bat` (Windows) or `ViewDatabase.sh` (macOS/Linux)
3. Browser opens with H2 Console
4. Connect using the JDBC URL from above
5. Show live data in tables!

---

## 📱 System Requirements

- **Operating System**: Windows 10+, macOS 10.12+, or Linux
- **Java**: JDK 8 or newer (JDK 11 or 17 recommended)
- **RAM**: 512 MB minimum (1 GB recommended)
- **Disk Space**: 50 MB for app + database
- **Display**: Any resolution (optimized for 1920×1080)

---

## 🔗 Useful Links

- **GitHub Repository**: https://github.com/PrachiGautam24/SkillSwap
- **Java Download**: https://adoptium.net/
- **Git Download**: https://git-scm.com/downloads
- **Report Issues**: https://github.com/PrachiGautam24/SkillSwap/issues

---

## 💡 Tips

- 💾 **Data Persistence**: Your data is saved automatically in the database
- 🔄 **Multiple Users**: Open multiple instances to test as different users
- 🗑️ **Reset Data**: Delete `skillswapdb.*` files to start fresh
- 📊 **Database Viewer**: Use it to verify bookings, ratings, and messages
- 🎨 **UI Scale**: If text is too small/large, adjust your OS display scaling

---

## ✅ Quick Checklist

Before sharing your repository with others, make sure:
- [ ] All code is committed and pushed to GitHub
- [ ] README.md is updated with clear instructions
- [ ] `lib/` folder contains all required JAR files
- [ ] `.gitignore` excludes `bin/` and `*.class` files
- [ ] Build scripts (`build.bat`, `build.sh`) work correctly
- [ ] Demo accounts are seeded in `DBConnection.java`

---

**Need more help?** Check the [README.md](README.md) or [HOW_TO_VIEW_DATABASE.md](HOW_TO_VIEW_DATABASE.md) for detailed information.

Happy Skill Swapping! 🚀
