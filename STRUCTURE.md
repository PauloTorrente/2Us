# Project Structure Guide

## Why This Folder Structure?

This follows **Maven Standard Directory Layout** - the industry-standard structure for Java projects. While it may seem deep, each level serves a specific purpose required by Java and Maven.

## Directory Breakdown

```
coupleapp-backend/
├── src/                          # Source root (Maven standard)
│   └── main/                     # Main code (Maven standard)
│       ├── java/                 # Java source files (Maven standard)
│       │   └── com/              # Reverse domain naming (Java convention)
│       │       └── coupleapp/    # Your application package
│       │           ├── controller/     # ✅ REST API endpoints
│       │           ├── service/        # ✅ Business logic
│       │           ├── repository/     # ✅ Database access
│       │           ├── entity/         # ✅ Database models
│       │           ├── dto/            # ✅ API request/response objects
│       │           ├── security/       # ✅ Auth & JWT
│       │           ├── exception/      # ✅ Error handling
│       │           └── config/         # ✅ App configuration
│       └── resources/            # Config files (Maven standard)
│           └── application.properties  # ✅ App settings
├── pom.xml                       # Maven dependencies
├── .env.example                  # Environment variables template
└── README.md                     # Documentation
```

## Why Can't We Simplify?

❌ **Cannot change:**
- `src/main/java` - Maven requires this
- `com/coupleapp` - Java package naming convention
  - `com` = commercial domain
  - `coupleapp` = your app name

✅ **What you actually work with:**

Once inside `src/main/java/com/coupleapp/`, you have clean organization:

```
coupleapp/
├── controller/    # Add new endpoints here
├── service/       # Add business logic here
├── repository/    # Add database queries here
├── entity/        # Add new data models here
└── dto/           # Add API objects here
```

## Quick Navigation

**Adding a new feature?**

1. Create controller: `controller/YourFeatureController.java`
2. Create service: `service/impl/YourFeatureService.java`
3. Create repository: `repository/YourFeatureRepository.java`
4. Create entity: `entity/YourFeature.java`

**That's it!** Spring Boot auto-discovers everything in the `com.coupleapp` package.

## Why This Matters

This structure enables:
- ✅ **Auto-discovery** - Spring finds all components automatically
- ✅ **Dependency injection** - Components wire together automatically
- ✅ **Clean separation** - Each layer has a clear responsibility
- ✅ **Industry standard** - Any Java dev understands this structure
- ✅ **Tooling support** - All IDEs recognize this pattern

## Bottom Line

Yes, the path is long: `src/main/java/com/coupleapp/controller`

But you only set it up **once**. After that, you work in clean folders like `controller/`, `service/`, etc.

This is Java. This is Maven. This is the way. 🚀
