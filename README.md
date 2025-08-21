# EduFlow Android App

<div align="center">
  <h3>🎓 Modern Educational Management System</h3>
  <p>A comprehensive Android application for streamlined education management with role-based access for administrators, teachers, and students.</p>
</div>

## 📱 Overview

EduFlow is a modern Android application designed to simplify educational management processes. Built with Kotlin and Firebase, it provides a seamless experience for managing courses, assignments, attendance, and educational resources across different user roles.

## ✨ Key Features

### 👨‍💼 Admin Dashboard
- **User Management**: Manage student and teacher accounts
- **System Overview**: Monitor overall system activity
- **Reports**: Generate attendance and results reports
- **Student Assignment**: Assign students to classes and courses

### 👨‍🏫 Teacher Dashboard
- **Course Material Management**: Upload and organize educational content
- **Assignment Creation**: Create and manage assignments with due dates
- **Attendance Tracking**: Use QR code scanning for quick attendance marking
- **Student Submissions**: Review and grade student submissions
- **File Upload**: Support for various file types (PDF, images, videos)

### 👨‍🎓 Student Dashboard
- **Course Materials**: Access uploaded course materials and resources
- **Assignment Submission**: Submit assignments with file uploads
- **Attendance**: Generate QR codes for attendance marking
- **Results**: View grades and academic performance
- **Profile Management**: Update personal information and academic details

### 🔧 General Features
- **Secure Authentication**: Firebase-based user authentication with role management
- **Real-time Updates**: Live synchronization of data across all users
- **Push Notifications**: Stay updated with important announcements
- **File Management**: Cloudinary integration for efficient file storage and retrieval
- **QR Code Integration**: Quick attendance marking and verification
- **Responsive Design**: Modern Material Design UI with smooth animations

## 🛠️ Technology Stack

- **Language**: Kotlin
- **Platform**: Android (API 29+)
- **Architecture**: MVVM with Fragment-based navigation
- **UI Framework**: Material Design with ViewBinding
- **Backend**: Firebase
  - Authentication
  - Firestore Database
  - Cloud Storage
  - Cloud Messaging
- **File Storage**: Cloudinary
- **QR Code**: ZXing (Zebra Crossing)
- **Image Loading**: Glide
- **Navigation**: Android Navigation Component

## 📋 Prerequisites

Before you begin, ensure you have met the following requirements:

- **Android Studio**: Latest stable version (recommended: Arctic Fox or newer)
- **Minimum SDK**: API level 29 (Android 10)
- **Target SDK**: API level 36
- **Kotlin**: 1.8+
- **Firebase Project**: Set up with Authentication, Firestore, and Storage
- **Cloudinary Account**: For file upload functionality

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/chandula04/EduFlow-Android-App.git
cd EduFlow-Android-App
```

### 2. Firebase Configuration
1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
3. Download the `google-services.json` file
4. Place it in the `app/` directory
5. Enable the following Firebase services:
   - Authentication (Email/Password)
   - Firestore Database
   - Cloud Storage
   - Cloud Messaging

### 3. Cloudinary Setup
1. Create a Cloudinary account at [Cloudinary](https://cloudinary.com/)
2. Get your cloud name, API key, and API secret
3. Update the Cloudinary configuration in `MainActivity.kt`:
```kotlin
val config = mapOf(
    "cloud_name" to "your_cloud_name",
    "api_key" to "your_api_key",
    "api_secret" to "your_api_secret"
)
```

### 4. Build and Run
1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run the application on your device or emulator

## 📁 Project Structure

```
app/src/main/java/com/cmw/eduflow/
├── MainActivity.kt                 # Main activity with Firebase initialization
├── HomeFragment.kt                # Landing page with role-based navigation
├── fragments/
│   ├── AdminDashboardFragment.kt   # Admin interface
│   ├── TeacherDashboardFragment.kt # Teacher interface
│   ├── StudentDashboardFragment.kt # Student interface
│   ├── LoginFragment.kt           # User authentication
│   ├── RegisterFragment.kt        # User registration
│   ├── ProfileFragment.kt         # User profile management
│   ├── MaterialsListFragment.kt   # Course materials
│   ├── AssignmentsListFragment.kt # Assignment management
│   └── AttendanceFragment.kt      # Attendance tracking
├── adapters/
│   ├── AssignmentAdapter.kt       # Assignment list adapter
│   ├── CourseMaterialAdapter.kt   # Course material adapter
│   └── AttendanceAdapter.kt       # Attendance record adapter
├── models/
│   ├── Assignment.kt              # Assignment data model
│   ├── CourseMaterial.kt         # Course material data model
│   └── AttendanceRecord.kt       # Attendance data model
└── services/
    └── MyFirebaseMessagingService.kt # Push notification service
```

## 👥 User Roles & Access

### Administrator
- **Registration**: Direct admin access (predefined in system)
- **Capabilities**: Full system access, user management, reporting
- **Dashboard**: System overview, user management tools

### Teacher
- **Registration**: Requires special teacher code validation
- **Capabilities**: Course management, assignment creation, attendance tracking
- **Dashboard**: Course materials, assignments, student submissions

### Student
- **Registration**: Standard registration with grade selection (1-12)
- **Capabilities**: Access materials, submit assignments, mark attendance
- **Dashboard**: Course materials, assignments, grades, QR code generation

## 🔐 Authentication Flow

1. **New Users**: Register with email, password, and role selection
2. **Teachers**: Must provide valid teacher code during registration
3. **Students**: Select grade level (1-12) during registration
4. **Login**: Email/password authentication with automatic role-based navigation
5. **Session Management**: Persistent login with secure session handling

## 📱 Usage Instructions

### For Teachers
1. **Upload Course Materials**:
   - Navigate to teacher dashboard
   - Click "Add Material" button
   - Select file and provide lesson title
   - Choose subject and upload

2. **Create Assignments**:
   - Go to assignments section
   - Click "Create Assignment"
   - Set title, due date, and attach files
   - Publish to students

3. **Track Attendance**:
   - Use QR scanner to mark student attendance
   - View attendance reports and statistics

### For Students
1. **Access Materials**:
   - Browse uploaded course materials
   - Download or view files directly in app

2. **Submit Assignments**:
   - View assigned tasks with due dates
   - Upload submission files (PDF format)
   - Track submission status

3. **Generate QR Code**:
   - Create QR code for attendance marking
   - Show to teacher for quick attendance

## 🔧 Configuration

### Firebase Rules
Ensure your Firestore security rules allow appropriate access:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own profile
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Teachers can manage their materials and assignments
    match /materials/{materialId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && resource.data.teacherId == request.auth.uid;
    }
    
    match /assignments/{assignmentId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && resource.data.teacherId == request.auth.uid;
    }
  }
}
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the Repository**
```bash
git fork https://github.com/chandula04/EduFlow-Android-App.git
```

2. **Create a Feature Branch**
```bash
git checkout -b feature/your-feature-name
```

3. **Make Changes**
   - Follow Kotlin coding conventions
   - Add comments for complex logic
   - Test your changes thoroughly

4. **Commit Changes**
```bash
git commit -m "Add: your feature description"
```

5. **Push to Branch**
```bash
git push origin feature/your-feature-name
```

6. **Create Pull Request**
   - Provide clear description of changes
   - Include screenshots for UI changes
   - Ensure all tests pass

### Code Style Guidelines
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public functions
- Maintain consistent indentation (4 spaces)
- Use ViewBinding for UI components

## 🐛 Troubleshooting

### Common Issues

1. **Build Errors**
   - Ensure `google-services.json` is in the correct location
   - Check Firebase project configuration
   - Verify Cloudinary credentials

2. **Authentication Issues**
   - Verify Firebase Authentication is enabled
   - Check network connectivity
   - Ensure email format is valid

3. **File Upload Problems**
   - Verify Cloudinary configuration
   - Check internet connection
   - Ensure file size limits are not exceeded

4. **QR Code Scanner Issues**
   - Grant camera permissions
   - Ensure device has a working camera
   - Check lighting conditions

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- **Issues**: [GitHub Issues](https://github.com/chandula04/EduFlow-Android-App/issues)
- **Discussions**: [GitHub Discussions](https://github.com/chandula04/EduFlow-Android-App/discussions)

## 🙏 Acknowledgments

- **Firebase**: For providing robust backend services
- **Cloudinary**: For efficient file storage and management
- **ZXing**: For QR code scanning functionality
- **Material Design**: For beautiful UI components
- **Android Community**: For continuous support and resources

---

<div align="center">
  <p>Made with ❤️ for the education community</p>
  <p>© 2024 EduFlow. All rights reserved.</p>
</div>