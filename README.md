

# 💸 SmartSpender App — *Part 3*

> 📱 **Your personal offline budgeting and expense manager with built-in motivation tools**

---

## 📖 Overview

SmartSpender is an easy-to-use, offline **Android mobile app** designed to help people track their personal expenses, set monthly spending goals, and develop healthier financial habits.

It provides a clear, friendly interface where users can log expenses, categorize them, view their financial progress, and get rewarded for good money management behavior — **all in South African Rands (R)**.

The app was built using **Kotlin** and **RoomDB (SQLite)**, ensuring secure, local storage of financial data without requiring an internet connection.

---

## 🎯 Project Purpose

In South Africa (and globally), people often struggle to stick to budgets or track where their money goes. SmartSpender addresses this problem by making financial management:

* **Simple** — clean layouts and easy navigation
* **Offline** — all data stored locally, no internet needed
* **Motivating** — with gamification features like badges, points, and real-time feedback
* **Visually insightful** — with a SmartPulse™ graph showing your financial habits over time

---

## 📌 What’s New in Part 3

In this final project phase, we added exciting new features based on feedback and practical usability improvements:

### 🌗 Dark Mode / Light Mode

* Users can now **switch between a dark theme and a light theme** based on personal preference or lighting conditions.
* The app saves this preference automatically and remembers it every time you open it.

---

### 🔔 Notification Center

* A new **Notification Tab** allows users to view helpful financial notifications.
* These include:

  * Alerts about overspending
  * Reminders of daily expenses
  * Progress toward monthly budget goals
* It helps users stay aware of their spending habits in real time.

---

### 📊 SmartPulse™

* A **visual  feature** showing users how well they’re sticking to their minimum and maximum monthly budget goals over time.
* Uses a **pulse-style line chart** with color-coded indicators:

  * 🟢 Under budget
  * 🟡 Near maximum
  * 🔴 Over budget


---

### 🎮 Gamification (NEW)

SmartSpender now includes **gamification features** to make budgeting fun, rewarding, and motivating.

**Why?**
Because research shows people are more likely to stick to good financial habits if the experience is enjoyable and they feel rewarded.


#### 🏅 Badges System

Earn **achievement badges** for reaching key financial milestones:



We also Implemented the graph system so that the user can see the spending in a grapgh format 

---

#### 🎮 Additional Gamification Elements:

| 🎮 Feature                          | 📖 What it Does                                                                                 |
| :---------------------------------- | :---------------------------------------------------------------------------------------------- |
| **Real-Time Savings Feedback**      | Shows how much you’re saving compared to last month, instantly after each transaction.          |
| **Progress Visualization**          | A progress bar that fills up as you save, with motivational messages based on percentage saved. |
| **Points Reward System**            | Earn points for good budgeting: save 20% = +50pts, save 10-19% = +30pts, etc.                   |
| **Personalized Financial Tips**     | Custom advice based on your spending habits to help you improve.                                |
| **Achievement Celebration Dialogs** | Pops up a cheerful dialog with a trophy icon and your points when you hit a financial goal.     |

**Example:**
If you save R1,000 this month compared to R5,000 last month:

* Progress bar fills to 75%
* Earn 30 points
* Dialog says: *“Great Saver! (+30 pts)”*

---

## 📚 Full Feature List

| 📦 Category                   | ✅ Features                                                                        |
| :---------------------------- | :-------------------------------------------------------------------------------- |
| 🧑‍💻 **User Authentication** | Local login with username and password.                                           |
| 📂 **Category Management**    | Create, edit, and delete expense categories.                                      |
| 💸 **Expense Tracking**       | Add expenses with: date, time, category, description, and optional receipt photo. |
| 🎯 **Goal Setting**           | Set monthly minimum and maximum spending limits.                                  |
| 📅 **Reports & Insights**     | View expenses by date, see totals by category, and analyze spending patterns.     |
| 💾 **Offline Storage**        | All data stored securely using RoomDB (SQLite) — no internet required.            |
| 🖼️ **Image Handling**        | Attach receipt photos to expenses, stored privately in the app.                   |
| 🎮 **Gamification**           | Badge rewards, points, progress bar, and savings tips.                            |
| 🌗 **Dark/Light Mode**        | Switch between light and dark themes for visual comfort.                          |
| 🔔 **Notification Center**    | Real-time alerts for budget reminders, overspending, and savings achievements.    |
| 📊 **SmartPulse Visualizer**  | Graphical view of your monthly spending trends and goal adherence.                |

---

## ⚙️ Tech Stack

| Tool / Library          | Purpose                                                     |
| :---------------------- | :---------------------------------------------------------- |
| **Kotlin**              | Core app development language                               |
| **RoomDB (SQLite)**     | Offline database storage for all user data                  |
| **XML**                 | Interface design layouts                                    |
| **SharedPreferences**   | Store preferences like streaks, badges, and theme selection |
| **RecyclerView**        | Display badges and transaction history lists                |
| **NotificationManager** | Show financial alerts and achievement dialogs               |

---

## 📁 Project Structure

```
SmartSpender/
├── app/
│   └── src/main/
│       ├── java/
│       │   ├── activities/        # App screens (Dashboard, AddExpense, Badges, etc.)
│       │   ├── database/          # RoomDB DAOs, Entities, and Database classes
│       │   ├── helpers/           # Preference Manager, Badge Manager, etc.
│       └── res/
│           ├── layout/            # XML layout files
│           ├── drawable/          # Images, icons, badge graphics
│           └── values/            # Themes, colors, strings
├── demo/                          # Demo video walkthrough
├── screenshots/                   # App screenshots
├── PGSL_Form.pdf                  # Formal project rubric
├── README.md                      # This file
```

---

## 📸 Screenshots
<img width="454" alt="login:sign up light mode" src="https://github.com/user-attachments/assets/b322f8d4-2e21-4ed9-873a-6034a52ae50f" />
<img width="489" alt="login:sign up dark" src="https://github.com/user-attachments/assets/600a6f76-a89f-4087-827f-660631b32636" />
<img width="438" alt="Create Account: light" src="https://github.com/user-attachments/assets/e96a8390-01d7-4a0c-9cac-382d1f359ee6" />
<img width="436" alt="create account : dark" src="https://github.com/user-attachments/assets/82a46196-e69e-427d-8b9d-0e60e41af94a" />
<img width="440" alt="add expense: light" src="https://github.com/user-attachments/assets/b702f79c-f63f-45c2-8f68-8114eb3d5377" />
<img width="481" alt="add expense:dark" src="https://github.com/user-attachments/assets/422f2ffd-5154-481d-96b3-8b701647bbb8" />
<img width="456" alt="rewards point: light" src="https://github.com/user-attachments/assets/44a97750-76de-4787-8343-6df45bc84ff8" />
<img width="428" alt="rewards points: dark" src="https://github.com/user-attachments/assets/55165893-47e2-409f-b5ae-5309c8a21a03" />
<img width="446" alt="Graph:light" src="https://github.com/user-attachments/assets/c773bcb9-fc1c-40b0-b979-439e4b122a9f" />
<img width="449" alt="graph: dark " src="https://github.com/user-attachments/assets/9fbdea13-e644-4281-a9bb-6484c8533e7b" />
<img width="420" alt="smart Pulse: Light" src="https://github.com/user-attachments/assets/39f541b3-8367-497b-86a9-97b7fd509a18" />
<img width="450" alt="Smart Pulse: dark" src="https://github.com/user-attachments/assets/5539f335-4bff-4551-8afe-4521ebbc8aa8" />











*(See full screenshots folder or demo video for more)*

---

## 🎥 Video Demo & Design

* 📽️ [YouTube App Demo Video](https://www.youtube.com/)

---

## 👩‍💻 Contributors

| Name               | Student Number |
| :----------------- | :------------- |
| Phathisa Ndaliso   | ST10241408     |
| Lindokuhle Zwane   | ST10381088     |
| Musawenkosi Bhebhe | ST10405518     |

---

## ✅ Smart Choices. Smart Savings.

**Thank you for choosing SmartSpender — your personal budgeting companion made simple, local, and rewarding.**

---

