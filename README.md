# Kalan Backend 🎓

> REST API for **Kalan** — a multilingual tech learning platform for Africa and the diaspora.
> Built with Spring Boot 3 · PostgreSQL · JWT · Deployed on Render.

---

## 🌍 About Kalan

Kalan (*"to learn"* in Bambara) is a learning platform targeting African and diaspora developers.
It supports **12 languages** including French, Bambara, Wolof, English and more.
Courses cover Java, Flutter, SQL, Spring Boot, Git, HTML/CSS and more.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 18 |
| ORM | Spring Data JPA / Hibernate |
| Auth | JWT (jjwt 0.12) |
| Security | Spring Security 6 |
| Build | Maven 3.9 |
| Hosting | Render |

---

## 📁 Project Structure

```
src/main/java/com/kalan/
├── KalanApplication.java
├── config/
│   └── SecurityConfig.java        # CORS, JWT filter, auth provider
├── controller/
│   ├── AuthController.java        # /api/v1/auth/register, /login
│   ├── CourseController.java      # /api/v1/courses
│   └── UserController.java        # /api/v1/user/profile, /progress
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── CourseResponse.java
├── entity/
│   ├── User.java
│   ├── Course.java                # Multilingual titles + geo-pricing
│   ├── Lesson.java
│   ├── QuizQuestion.java
│   ├── Enrollment.java
│   └── LessonProgress.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── UserRepository.java
│   ├── CourseRepository.java
│   ├── LessonRepository.java
│   ├── EnrollmentRepository.java
│   └── LessonProgressRepository.java
├── security/
│   ├── JwtUtil.java               # Token generation & validation
│   └── JwtAuthFilter.java         # JWT request filter
└── service/
    ├── AuthService.java           # Register, login logic
    └── CourseService.java         # Course listing, enrollment
```

---

## 🗄️ Database Schema

```
users
  id, email, password, full_name, country, preferred_language,
  role, created_at, last_login_at

courses
  id, title_fr, title_en, title_bm, title_wo,
  description_fr, description_en,
  topic, level, language, instructor_name,
  is_free, price_xof, price_eur, price_usd,
  rating, student_count, published

lessons
  id, course_id, order_index,
  title_fr, title_en, title_bm,
  notes_fr, notes_en,
  video_url, duration_seconds, is_free

quiz_questions
  id, lesson_id,
  question_fr, question_en, question_bm,
  options_fr, options_en, correct_index

enrollments
  id, user_id, course_id,
  enrolled_at, completed_at,
  completed_lessons, payment_status, payment_reference

lesson_progress
  id, user_id, lesson_id,
  completed, watched_seconds, quiz_score,
  started_at, completed_at
```

---

## 🔌 API Endpoints

### Auth — Public
```
POST /api/v1/auth/register
POST /api/v1/auth/login
```

### Courses — Public
```
GET  /api/v1/courses
GET  /api/v1/courses/{id}
```

### Courses — Requires JWT
```
POST /api/v1/courses/{id}/enroll
```

### User — Requires JWT
```
GET  /api/v1/user/profile
GET  /api/v1/user/progress
POST /api/v1/user/progress/lesson/{lessonId}
```

---

## 🚀 Local Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 14+

### 1. Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/kalan-backend.git
cd kalan-backend
```

### 2. Create the database
```sql
CREATE DATABASE kalan_db;
```

### 3. Configure environment
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kalan_db
    username: postgres
    password: YOUR_PASSWORD
```

Or set environment variables:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/kalan_db
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-secret-key-min-32-chars
```

### 4. Run
```bash
mvn spring-boot:run
```

API is live at `http://localhost:8080` ✅

---

## ☁️ Deploy to Render

1. Push to GitHub
2. Go to [render.com](https://render.com) → **New Web Service**
3. Connect your GitHub repo
4. Set:
   - **Build command:** `mvn clean package -DskipTests`
   - **Start command:** `java -jar target/kalan-backend-0.0.1-SNAPSHOT.jar`
5. Add environment variables:
   ```
   DATABASE_URL=jdbc:postgresql://...
   DB_USERNAME=...
   DB_PASSWORD=...
   JWT_SECRET=...
   PORT=8080
   ```
6. Click **Deploy** 🚀

---

## 💳 Payment Support

| Method | Region |
|---|---|
| Wave | Mali, Sénégal, Côte d'Ivoire |
| Orange Money | Mali, Sénégal, Guinea |
| MTN Money | Nigeria, Ghana, 17 countries |
| Stripe | Europe, USA, worldwide |
| PayPal | Worldwide |

Pricing is geo-based:
- **FCFA** for West Africa
- **EUR** for Europe diaspora
- **USD** for USA diaspora

---

## 🌐 Frontend

Flutter app: [github.com/YOUR_USERNAME/kalan](https://github.com/YOUR_USERNAME/kalan)

Supports Web → Android → iOS from one codebase.

---

## 📄 License

MIT — feel free to use and adapt for your own African tech projects.

---

*Built with ❤️ for Africa and the diaspora.*