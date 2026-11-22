# MiniProject-DocPortal 🏥

A Doctor-Patient Management Web Portal built with **Java Servlets (Jakarta EE 6.0)**, **HTML5/CSS3**, and **MySQL/JDBC**.

---

## 📌 Project Overview

The Doctor-Patient Portal simplifies and digitizes medical operations, bridging the communication and scheduling gap between patients, doctors, and hospital administrators:
- **User Authentication:** Registration and login for patients, doctors, and staff with role-based access control. Newly registered doctors require administrator verification before gaining portal access.
- **Doctor Directory:** View available doctors, their medical specialties, and contact details. Doctors are verified by the admin before inclusion.
- **Appointment Management:** Real-time scheduling with a 30-minute time window system (enforcing a strict limit of max 3 patients/tokens per 30 minutes for quality consultation).
- **Prescription System:** Allows doctors to set allowed dosage time limits. Once the time limit expires, the prescription is blurred and stamped with *"ALLOWED DOSE TIME HAS COMPLETED"*.
- **Slip Generator:** Generates printable appointment slips for booked appointments.

---

## 🏗️ Project Architecture & Tech Stack

- **Backend:** Java Servlets (Jakarta EE 6.0 / Servlet 6.0)
- **Database Connectivity:** JDBC (`com.mysql.cj.jdbc.Driver`)
- **Database Engine:** MySQL 8.0+ / TiDB Cloud / Aiven (Cloud-ready)
- **Frontend:** HTML5, CSS3, Responsive Design with dynamic feedback
- **Supported Servers:** Apache Tomcat 10.1+ / Jetty 12+ / GlassFish 7+

### Application Structure
```text
MiniProject-DocPortal/
├── pom.xml                               # Maven build configuration
├── schema.sql                            # Complete database schema and seed data
├── README.md                             # Project documentation
└── sample/
    ├── build/classes/                    # Compiled class files
    └── src/
        └── main/
            ├── java/
            │   ├── conn/
            │   │   └── DBConnection.java # Database connection manager (Env + Properties)
            │   ├── LoginServlet.java
            │   ├── RegisterServlet.java
            │   ├── DashboardServlet.java
            │   ├── DoctorServlet.java
            │   ├── ManageAppointmentServlet.java
            │   ├── PrescriptionServlet.java
            │   └── DownloadSlipServlet.java
            ├── resources/
            │   └── db.properties         # Database credentials and configuration
            └── webapp/
                ├── WEB-INF/web.xml       # Servlet mappings & deployment descriptor
                ├── Welcome.html          # Portal home page
                ├── Login.html            # Sign-in page
                ├── register.html         # User sign-up page
                ├── dashboard.html        # Main dashboard
                ├── InsertDoctor.html     # Doctor onboarding
                ├── ManageAppointment.html# Appointment booking & status
                ├── Prescription.html     # Doctor prescription writer & lookup
                └── styles.css            # Stylesheets & assets
```

---

## 🗄️ Database Tables (`schema.sql`)

1. **`loginone`**: User credentials (`id`, `username`, `email`, `password`, `role`, `created_at`)
2. **`doctors`**: Doctor information (`id`, `name`, `specialty`, `contact`, `created_at`)
3. **`appointments`**: Appointment records (`id`, `appointmentId`, `doctorName`, `patientId`, `patientName`, `appointmentDate`)
4. **`prescriptions`**: Patient prescriptions (`id`, `doctorName`, `patientId`, `prescription`, `created_at`)

---

## 🌐 Setting Up a Free Online Cloud Database

You can host your database on **TiDB Cloud (Serverless)** or **Aiven for MySQL** completely free without requiring a credit card:

### Option A: TiDB Cloud Serverless (Recommended — 100% Free Forever)
1. Go to [TiDB Cloud](https://tidbcloud.com/) and sign up for free (using GitHub or Google).
2. Click **Create Cluster** and select **Serverless (Free Tier)**.
3. Once created, click **SQL Editor** in the TiDB Cloud dashboard.
4. Open [schema.sql](schema.sql), copy its contents, paste them into the SQL Editor, and click **Run**.
5. Click **Connect** on your TiDB dashboard and choose **Java (JDBC)**.
6. Open [sample/src/main/resources/db.properties](sample/src/main/resources/db.properties) and update the values:
   ```properties
   db.driver=com.mysql.cj.jdbc.Driver
   db.url=jdbc:mysql://<your-tidb-host>:4000/<your-database>?useSSL=true&sslMode=VERIFY_IDENTITY
   db.user=<your-tidb-username>
   db.password=<your-tidb-password>
   ```

### Option B: Using Environment Variables
You can also configure database credentials without touching code files by setting environment variables in your deployment environment or operating system:
```bash
export DB_URL="jdbc:mysql://<host>:<port>/<dbname>?useSSL=true&sslMode=REQUIRED"
export DB_USER="<username>"
export DB_PASSWORD="<password>"
```
