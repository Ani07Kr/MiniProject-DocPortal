# DocConnect — Doctor-Patient Management Web Portal 🏥✨

[![Live Deployment](https://img.shields.io/badge/Render-Live%20Website-brightgreen?logo=render)](https://miniproject-docportal.onrender.com)
[![Database](https://img.shields.io/badge/Database-TiDB%20Cloud%20Serverless-blue?logo=mysql)](https://tidbcloud.com/)
[![Java](https://img.shields.io/badge/Java-17%20%7C%20Jakarta%20EE-orange?logo=openjdk)](https://jakarta.ee/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)](https://www.docker.com/)
[![Responsive](https://img.shields.io/badge/Responsive-Mobile%20%7C%20Tablet%20%7C%20Desktop-teal)](#-responsive-design-matrix)

> **Live Production Website:**  
> 🌐 **[https://miniproject-docportal.onrender.com](https://miniproject-docportal.onrender.com)**

---

## 📌 Project Overview

**DocConnect** is a modern, enterprise-grade healthcare management portal designed to streamline outpatient consultations, doctor workflows, and hospital operations. Built on a lightweight Java Servlet architecture connected to a scalable cloud database, DocConnect eliminates crowded waiting rooms through intelligent 30-minute time slotting, automated sequential queue tokens, role-segregated medical records, and digital prescriptions with active dosage expiration tracking.

---

## 🌟 Key Features & Innovations

### 1. ⏱️ 30-Minute Consultation Windows & Queue Tokens
- Appointments are booked in **30-minute intervals** across morning and afternoon/evening shifts.
- **Strict Capacity Limit:** A maximum of **3 patients** are allowed per 30-minute slot (~10 minutes per consultation).
- **Automated Queue Tokens:** When an appointment is booked, a sequential token (e.g. `TOKEN #1`, `TOKEN #2`, `TOKEN #3`) is generated automatically and displayed on the confirmation and downloadable slip.

### 2. 💊 Digital Prescriptions with Timed Dosage & Expiration Stamp
- Doctors prescribe medicines with an **allowed dosage duration limit** (`valid_until` timestamp).
- **Interactive Quick Presets:** Doctors can set duration with 1-click pills (`+1 Day`, `+3 Days`, `+7 Days`, `+14 Days`, `+30 Days`).
- **Automatic Expiration Safeguard:** Once the prescribed duration expires, the prescription details are automatically **blurred** and stamped with an unalterable red watermark:
  $$\text{ALLOWED DOSE TIME HAS COMPLETED}$$
  preventing patients from taking expired medication or requesting unauthorized refills.

### 3. 🛡️ Admin Doctor Credential Verification
- When new doctors register, their account is initialized with `status = 'PENDING'`.
- If an unverified doctor attempts to log in, access is blocked with a notice:  
  *`"Your Doctor account is pending administrator verification. Please wait for admin approval."`*
- The Hospital Administrator reviews credentials in the **Doctor Authorization Desk** (`/AdminApprovalServlet`) and activates verified practitioners with a single click.

### 4. 🔒 Role-Based Access Control (RBAC) & Privacy
- **Patient Access:** Patients can book appointments, track active tokens, and view **only their personal prescriptions**. Patients are strictly prevented from writing or editing prescriptions.
- **Doctor Access:** Doctors can consult patients, manage their allocated queues, and write official prescriptions permanently bound to their verified doctor identity.
- **Admin Access:** Full oversight of hospital scheduling, doctor roster additions, and credential verifications.

### 5. 📱 Responsive, Creative Healthcare UI
- Modern medical aesthetics built with **Plus Jakarta Sans**, glassmorphism cards, and fluid layouts.
- **Universal Responsiveness:** Styled to adapt across small mobile screens (320px–375px), standard phones (390px–430px), tablets/iPads (768px–1024px), laptops, and large desktop monitors.
- Sticky navigation bar with DocConnect branding, live session indicators, and fast actions on every page.
- Accessible form controls with 16px font sizing to prevent iOS Safari auto-zooming.

### 6. 📄 Downloadable & Printable Appointment Slips
- Generates official appointment slips formatted for standard paper printing or PDF export with doctor name, patient details, queue token, and allotted 30-minute window.

---

## 🏗️ Architecture & Tech Stack

```text
┌─────────────────────────────────────────────────────────────┐
│                      Client Browser                         │
│  (Desktop, Tablet, Mobile — HTML5 / Modern CSS / Vanilla JS)│
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP / HTTPS (REST & Servlets)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   AppServer (Port 8080)                     │
│    Built-in Java HTTP Server & Dispatcher with Sessions     │
│  ┌───────────────────────┬───────────────────────────────┐  │
│  │     AuthServlet       │        LoginServlet           │  │
│  ├───────────────────────┼───────────────────────────────┤  │
│  │   RegisterServlet     │        DoctorServlet          │  │
│  ├───────────────────────┼───────────────────────────────┤  │
│  │ManageAppointmentServlet│     PrescriptionServlet       │  │
│  ├───────────────────────┼───────────────────────────────┤  │
│  │ AdminApprovalServlet  │      DownloadSlipServlet      │  │
│  └───────────────────────┴───────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────┘
                               │ JDBC (SSL Connection Pool)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  TiDB Cloud Serverless                      │
│             Distributed MySQL 8.0 Compatible                │
└─────────────────────────────────────────────────────────────┘
```

- **Backend:** Java 17, Jakarta EE 10 / Servlet API 6.0
- **Server:** Embedded `AppServer` (no heavy servlet container needed, fast cold start)
- **Database:** TiDB Cloud Serverless MySQL (with SSL TLSv1.3 encryption)
- **Deployment:** Docker Multi-stage Container on Render
- **Frontend:** Pure HTML5, CSS3 Glassmorphism, Vanilla JavaScript (Zero bloated frontend frameworks)

---

## 📁 Repository Structure

```text
MiniProject-DocPortal/
├── Dockerfile                             # Production multi-stage build image
├── render.yaml                            # Render Infrastructure as Code configuration
├── schema.sql                             # Database schema DDL & seed records
├── run.sh                                 # One-click local compilation & launch script
├── README.md                              # Comprehensive project documentation
└── sample/
    ├── build/classes/                     # Compiled JVM bytecode (.class)
    └── src/main/
        ├── java/
        │   ├── conn/
        │   │   └── DBConnection.java      # Dual-mode database manager (Env + Properties)
        │   ├── AdminApprovalServlet.java  # Admin doctor verification desk
        │   ├── AppServer.java             # Standalone production HTTP server & session manager
        │   ├── AuthServlet.java           # Current user session & role API (/AuthServlet)
        │   ├── DoctorServlet.java         # Doctor directory API & roster management
        │   ├── DownloadSlipServlet.java   # Printable token slip generator
        │   ├── LoginServlet.java          # Role authentication & password verification
        │   ├── LogoutServlet.java         # Session cleanup & cookie invalidator
        │   ├── ManageAppointmentServlet.java # Slot booking, 3-token limit & cancellation
        │   ├── PrescriptionServlet.java   # Timed prescription authoring & dosage watermark
        │   └── RegisterServlet.java       # User registration & pending doctor status
        ├── resources/
        │   └── db.properties              # TiDB Cloud credentials configuration
        └── webapp/
            ├── WEB-INF/
            │   ├── web.xml                # Deployment descriptor
            │   └── lib/                   # Bundled dependencies
            │       ├── jakarta.servlet-api-6.0.0.jar
            │       └── mysql-connector-j-8.3.0.jar
            ├── Welcome.html               # Creative homepage with hero, features & stats
            ├── index.html                 # Homepage mirror for standard web routing
            ├── Login.html                 # Creative split-pane portal sign-in
            ├── register.html              # Creative dual-role registration page
            ├── dashboard.html             # User portal dashboard tailored by role
            ├── ManageAppointment.html     # 30-min slot scheduling with queue tokens
            ├── Prescription.html          # Digital prescription management center
            ├── InsertDoctor.html          # Doctor onboarding form
            └── images/                    # UI background assets
```

---

## 🗄️ Database Schema & Entities

The complete database schema is located in [`schema.sql`](schema.sql):

### 1. `loginone` (User Accounts & Authentication)
| Column | Type | Description |
|---|---|---|
| `id` | INT AUTO_INCREMENT | Primary Key |
| `username` | VARCHAR(50) UNIQUE | Unique account username |
| `email` | VARCHAR(100) | User email address |
| `password` | VARCHAR(100) | Account password |
| `role` | VARCHAR(20) | `Patient`, `Doctor`, or `Admin` |
| `status` | VARCHAR(20) | `APPROVED` (active) or `PENDING` (review) |
| `created_at` | TIMESTAMP | Account creation date |

### 2. `doctors` (Medical Practitioner Directory)
| Column | Type | Description |
|---|---|---|
| `id` | INT AUTO_INCREMENT | Primary Key |
| `name` | VARCHAR(100) | Doctor full name (e.g. Dr. A. K. Sharma) |
| `specialty` | VARCHAR(100) | Medical specialty (e.g. Cardiology) |
| `contact` | VARCHAR(20) | Official telephone/contact number |

### 3. `appointments` (Consultation Slots & Queue Tokens)
| Column | Type | Description |
|---|---|---|
| `id` | INT AUTO_INCREMENT | Primary Key |
| `appointmentId` | VARCHAR(50) UNIQUE | Human-readable ID (e.g. `APPT4812`) |
| `doctorName` | VARCHAR(100) | Assigned doctor |
| `patientId` | VARCHAR(50) | Patient phone number / Account ID |
| `patientName` | VARCHAR(100) | Patient full name |
| `appointmentDate` | DATE | Consultation date |
| `time_slot` | VARCHAR(50) | 30-minute interval (e.g. `09:00 AM - 09:30 AM`) |
| `token_number` | INT | Sequential queue token (`1`, `2`, or `3`) |

### 4. `prescriptions` (Digital Prescriptions & Dosage Timers)
| Column | Type | Description |
|---|---|---|
| `id` | INT AUTO_INCREMENT | Primary Key |
| `doctorName` | VARCHAR(100) | Prescribing doctor (locked to authenticated ID) |
| `patientId` | VARCHAR(50) | Patient Phone / Account ID |
| `prescription` | TEXT | Medication, dosage frequency & instructions |
| `valid_until` | DATETIME | Timestamp when allowed dosage expires |
| `created_at` | TIMESTAMP | Date and time prescription was signed |

---

## 🔑 Demo & Test Accounts

You can test all roles immediately using the seeded demo credentials:

| Role | Username | Password | Capabilities |
|---|---|---|---|
| **Hospital Admin** | `admin` | `admin123` | Authorize new doctors, view all appointments & prescriptions |
| **Doctor** | `doctor` | `doctor123` | Issue signed prescriptions with dosage timers, view patient queue |
| **Doctor** | `drsharma` | `drsharma123` | Dr. A. K. Sharma (Cardiology specialist practice) |
| **Patient** | `patient` | `patient123` | Book 30-min slots, view personal prescriptions with dosage stamps |
| **New Register** | *Any* | *Any* | Register your own profile via the [Registration Page](https://miniproject-docportal.onrender.com/register.html) |

---

## 🚀 Running the Project Locally

### Prerequisites
- **Java Development Kit (JDK 17 or higher)**
- **Git**
- Terminal / Shell (macOS, Linux, or Windows WSL/PowerShell)

### Quick Start (Local Run)
1. **Clone the repository:**
   ```bash
   git clone https://github.com/Ani07Kr/MiniProject-DocPortal.git
   cd MiniProject-DocPortal
   ```

2. **Run the automated launch script:**
   ```bash
   chmod +x run.sh
   ./run.sh
   ```
   *The script compiles all servlets and classes using the bundled libraries and launches `AppServer` on port 8080.*

3. **Open the browser:**
   ```text
   http://localhost:8080/Welcome.html
   ```

---

## 🐳 Running with Docker

You can build and run DocConnect in an isolated Docker container with zero setup:

```bash
# Build the Docker image
docker build -t docconnect-portal .

# Run the container (maps host port 8080 to container port 8080)
docker run -p 8080:8080 docconnect-portal
```
Visit `http://localhost:8080` in your web browser.

---

## ☁️ Live Cloud Deployment (Render)

This repository is pre-configured for **Render** via [`Dockerfile`](Dockerfile) and [`render.yaml`](render.yaml):

1. Log in to [Render](https://dashboard.render.com).
2. Click **New +** > **Web Service**.
3. Connect your GitHub repository (`Ani07Kr/MiniProject-DocPortal`).
4. Select **Docker** as the runtime environment.
5. In **Environment Variables**, configure your database connection parameters:
   - `DB_URL`: `jdbc:mysql://<your-tidb-host>:4000/<database>?useSSL=true&sslMode=VERIFY_IDENTITY`
   - `DB_USER`: `<your-tidb-username>`
   - `DB_PASSWORD`: `<your-tidb-password>`
   - `PORT`: `8080`
6. Click **Deploy Web Service**. Render compiles the container and goes live!

---

## 📱 Responsive Design Matrix

| Device Category | Screen Resolution | Layout Adaptation |
|---|---|---|
| **Mobile Phones (Portrait)** | 320px – 480px | Single-column fluid stack, thumb-friendly buttons (min 44px), 16px input font (prevents iOS auto-zoom), scrollable table wrappers |
| **Large Phones & Phablets** | 481px – 768px | Compact sticky navigation bar, 2x2 statistics grid, flexible duration selection pills |
| **Tablets & iPads** | 769px – 1024px | 2-column split showcase on login, full doctor grid, adaptive prescription dosage watermark |
| **Laptops & Desktops** | 1025px+ | High-definition glassmorphism cards, full navbar links, spacious consultation dashboard |

---

## 📄 License
This project is open-source under the [MIT License](LICENSE). Built for academic and healthcare portfolio demonstration.
