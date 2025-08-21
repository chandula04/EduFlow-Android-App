# Contributing to EduFlow Android App

Thank you for your interest in contributing to EduFlow! We welcome contributions from the community and are pleased to have you join us.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## 📜 Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

### Our Pledge

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## 🚀 Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Create a branch** for your contribution
4. **Make your changes**
5. **Test your changes**
6. **Submit a pull request**

## 🤝 How to Contribute

### Types of Contributions

We welcome several types of contributions:

- **Bug Fixes**: Help us identify and fix bugs
- **Feature Enhancements**: Add new features or improve existing ones
- **Documentation**: Improve our documentation
- **UI/UX Improvements**: Enhance the user interface and experience
- **Performance Optimizations**: Make the app faster and more efficient
- **Testing**: Add or improve test coverage

### Areas Where Help is Needed

- 🐛 Bug fixes and issue resolution
- 🎨 UI/UX improvements and Material Design implementation
- 📱 Android best practices and performance optimization
- 🔒 Security enhancements
- 📚 Documentation improvements
- 🧪 Test coverage expansion
- 🌐 Accessibility improvements
- 🔧 Code refactoring and optimization

## 🛠️ Development Setup

### Prerequisites

- Android Studio (latest stable version)
- JDK 11 or higher
- Android SDK (API 29+)
- Git
- Firebase account
- Cloudinary account

### Environment Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/chandula04/EduFlow-Android-App.git
   cd EduFlow-Android-App
   ```

2. **Setup Firebase**:
   - Create a Firebase project
   - Add Android app to Firebase project
   - Download `google-services.json`
   - Place it in the `app/` directory

3. **Configure Cloudinary**:
   - Update credentials in `MainActivity.kt`

4. **Build the project**:
   ```bash
   ./gradlew build
   ```

### Project Structure Understanding

Familiarize yourself with the project structure:

```
app/src/main/java/com/cmw/eduflow/
├── MainActivity.kt                 # App entry point
├── fragments/                      # All UI fragments
├── adapters/                       # RecyclerView adapters
├── models/                         # Data models
└── services/                       # Background services
```

## 📐 Coding Standards

### Kotlin Style Guidelines

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names for variables, functions, and classes
- Keep functions small and focused on a single task
- Use data classes for simple data holders
- Prefer immutability when possible

### Code Organization

- **Fragments**: Keep fragments focused on UI logic
- **ViewModels**: Use for business logic and data management
- **Adapters**: Keep adapters simple and reusable
- **Models**: Use data classes for model objects
- **Constants**: Define in companion objects or separate files

### Naming Conventions

```kotlin
// Classes: PascalCase
class StudentDashboardFragment

// Functions and variables: camelCase
fun setupRecyclerView()
val userAccount = "example"

// Constants: UPPER_SNAKE_CASE
const val MAX_FILE_SIZE = 10_000_000

// Resources: snake_case
R.layout.fragment_student_dashboard
R.string.welcome_message
```

### Comments and Documentation

- Add KDoc comments for public functions and classes
- Use inline comments sparingly and only when necessary
- Keep comments up-to-date with code changes

```kotlin
/**
 * Uploads a file to Cloudinary and saves the URL to Firestore
 * @param lessonTitle The title of the lesson
 * @param fileUri The URI of the file to upload
 */
private fun uploadFileToCloudinary(lessonTitle: String, fileUri: Uri) {
    // Implementation here
}
```

## 🧪 Testing Guidelines

### Test Types

1. **Unit Tests**: Test individual functions and classes
2. **Integration Tests**: Test component interactions
3. **UI Tests**: Test user interface elements

### Writing Tests

- Write tests for new features and bug fixes
- Aim for meaningful test coverage
- Use descriptive test names
- Follow the AAA pattern (Arrange, Act, Assert)

```kotlin
@Test
fun `should upload file successfully when valid URI provided`() {
    // Arrange
    val mockUri = mock(Uri::class.java)
    val lessonTitle = "Sample Lesson"
    
    // Act
    uploadFileToCloudinary(lessonTitle, mockUri)
    
    // Assert
    verify(mockCloudinary).upload(mockUri)
}
```

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📝 Pull Request Process

### Before Submitting

1. **Update documentation** if you've made changes to APIs
2. **Add tests** for new functionality
3. **Run existing tests** to ensure nothing breaks
4. **Follow coding standards** outlined above
5. **Update CHANGELOG.md** if applicable

### Pull Request Guidelines

1. **Title**: Use a clear and descriptive title
   - ✅ "Add file upload progress indicator"
   - ❌ "Fix stuff"

2. **Description**: Include:
   - What changes were made and why
   - Any breaking changes
   - Screenshots for UI changes
   - Testing instructions

3. **Size**: Keep PRs focused and reasonably sized
   - Prefer smaller, focused PRs over large ones
   - If working on a large feature, consider breaking it into smaller PRs

4. **Branch naming**:
   ```
   feature/add-file-upload-progress
   bugfix/fix-authentication-crash
   docs/update-readme
   refactor/improve-adapter-performance
   ```

### Review Process

1. Automated checks must pass
2. At least one maintainer approval required
3. Address feedback promptly
4. Keep discussions respectful and constructive

## 🐛 Issue Reporting

### Before Creating an Issue

1. **Search existing issues** to avoid duplicates
2. **Check documentation** for answers
3. **Update to latest version** if possible

### Creating a Good Issue

Include the following information:

#### For Bug Reports

```markdown
**Description**
A clear description of the bug

**Steps to Reproduce**
1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

**Expected Behavior**
What you expected to happen

**Actual Behavior**
What actually happened

**Environment**
- Device: [e.g. Pixel 6]
- Android Version: [e.g. Android 12]
- App Version: [e.g. 1.0.0]

**Additional Context**
Add any other context or screenshots
```

#### For Feature Requests

```markdown
**Feature Description**
A clear description of the feature you'd like to see

**Use Case**
Explain why this feature would be useful

**Proposed Solution**
If you have ideas on how to implement this

**Alternatives Considered**
Any alternative solutions you've considered
```

## 🏷️ Issue Labels

We use labels to categorize issues:

- `bug`: Something isn't working
- `enhancement`: New feature or request
- `documentation`: Documentation needs
- `good first issue`: Good for newcomers
- `help wanted`: Extra attention needed
- `question`: Further information requested
- `duplicate`: Duplicate issue
- `wontfix`: Won't be worked on

## 🎯 Development Workflow

### Git Workflow

1. **Create feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make commits with clear messages**:
   ```bash
   git commit -m "Add: file upload progress indicator
   
   - Shows upload progress percentage
   - Displays file name during upload
   - Cancellable upload operation"
   ```

3. **Keep your branch updated**:
   ```bash
   git fetch origin
   git rebase origin/main
   ```

4. **Push your branch**:
   ```bash
   git push origin feature/your-feature-name
   ```

### Commit Message Guidelines

Use conventional commits format:

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

Examples:
```
feat(auth): add password strength validation
fix(upload): handle network timeout errors
docs(readme): update installation instructions
```

## 📞 Getting Help

If you need help or have questions:

1. **Check existing documentation**
2. **Search through issues**
3. **Create a new issue** with the `question` label
4. **Join discussions** in GitHub Discussions

## 🙏 Recognition

We appreciate all contributions and will:

- Add contributors to our README
- Acknowledge contributions in release notes
- Provide feedback and support

Thank you for contributing to EduFlow! 🎓