# SkillSwap — Peer-to-Peer Student Skill Exchange (Java Swing + H2)

A desktop application built with **Pure Java (JDK 8+)**, **Java Swing/AWT**, **H2 Embedded Database (JDBC)**, and **BCrypt** password hashing following the **MVC architecture**.

## Features
- **Authentication**: Student registration & login with BCrypt password hashing (`LoginFrame`, `RegisterFrame`).
- **Dashboard Hub**: Real-time statistics cards, campus announcements, and sidebar navigation (`DashboardFrame`).
- **My Sessions** opens on the bookings you made or host. Filter by role to see the partner, scheduled time, and status. Manage your own posts in **My offers** and **My help requests**.
- **Smart Peer Matching**: Calculates compatibility scores (0–100%) based on complementary skills and learning modes (`FindMatchesFrame`).
- **Skill Exchange Requests**: Send, accept, or reject direct peer skill swap proposals (`SendRequestDialog`, `MyRequestsFrame`).
- **Ratings & Leaderboard**: 1–5 star session reviews and Top 10 peer mentor rankings (`MyExchangesFrame`, `RateSessionDialog`, `LeaderboardFrame`).
- **Profile Management**: Update skills, learning preferences, and bio (`EditProfileDialog`).

## Setup & Running Locally

1. Install a JDK (Java 8 or newer).
2. On Windows, run `build.bat`. On macOS or Linux, run `bash build.sh`.

The local build uses H2 1.4.200 and jBCrypt from `lib/`. This checkout includes H2 and a jBCrypt JAR built from the upstream Java source. A clean checkout can also use the Maven artifacts `com.h2database:h2:1.4.200` and `org.mindrot:jbcrypt:0.4`.

## Demo Credentials (Auto-seeded on First Launch)
- **Email**: `alice@uni.edu` | **Password**: `pass123`
- **Email**: `bob@uni.edu` | **Password**: `pass123`
