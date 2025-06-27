# 🖼️ Digital Image Processing Tool - APEU

![Java](https://img.shields.io/badge/Java-17+-blue)
![Architecture](https://img.shields.io/badge/Architecture-Clean-success)
![UI](https://img.shields.io/badge/UI-Swing-lightgrey)
![License](https://img.shields.io/badge/License-MIT-green)

A modular Java application for advanced Digital Image Processing (DIP), designed using Clean Architecture and SOLID principles. It provides a Swing-based user interface and an extensible backend for color manipulation, histogram operations, and core image processing logic.

---

## 📁 Project Structure

```bash

├── App.java              # Entry point
├── lib                   # Application logic
│   ├── color             # Core domain logic and abstractions
│   └── core              # Color operations
└── ui                    # Swing-based UI
    ├── commands          # Command pattern for actions
    ├── components        # Reusable UI components
    ├── controllers       # Orchestrate UI and domain logic
    ├── handlers          # File and event handling
    └── views             # Main application window

```

---

## 🚀 Features

* 📷 **Load/Save images** (JPG, PNG, BMP, GIF, WBMP)
* 🎨 **Color operations** (e.g., brightness, contrast, grayscale)
* 💡 **Clean UI** with MVC and Command Pattern
* 🧱 **Clean Architecture**: separation between UI, application, domain, and infrastructure
* 🔧 **Extensible**: add new operations easily

---

## 🧠 Architecture Layers

| Layer            | Description                                                 |
| ---------------- | ----------------------------------------------------------- |
| `domain`         | Pure logic and interfaces (e.g., `Image`, `ColorOperation`) |
| `application`    | Use cases and coordination logic                            |
| `infrastructure` | Implementations for file loading, in-memory images          |
| `ui`             | User interface with commands, views, handlers               |

Each module (`core`, `color`) follows the same internal structure: `application`, `domain`, `infrastructure`.

---

## 🏁 Getting Started

### Prerequisites

* Java 17 or higher
* Maven (optional, for packaging)

### Run

You can run the application via your IDE or using the following command:

```bash
./gradlew run
```

---

## 🧩 Adding New Features

To add a new color operation:

1. Create a new class implementing `ColorOperation` in `lib/color/domain/`.
2. Implement business logic in `application`.
3. Create adapter/loader if necessary in `infrastructure`.
4. Add UI hook in `controllers` and `commands`.

---

## 📌 Roadmap

* [x] Modular project setup
* [x] Color operations (brightness, contrast, etc.)
* [ ] Unit testing coverage

---

## 🤝 Contribution Guidelines

* Use `feature/[name]` or `fix/[issue]` branch naming
* Follow [Conventional Commits](https://www.conventionalcommits.org/)
* Document new features and include basic usage in UI

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 👤 Maintainer

**Edwin Uriel Astudillo Pérez**
📧 [astudillo.perez.edwin.uriel@gmail.com](mailto:astudillo.perez.edwin.uriel@gmail.com)
