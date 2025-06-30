# 🖼️ StegoApp — JavaFX LSB Steganography Tool

**StegoApp** is a desktop JavaFX application for hiding and extracting text messages within images using the LSB (Least Significant Bit) steganography technique. It features a modern user interface with zooming, dark mode, and the ability to visually compare images.


## 🔧 Features

- 📥 Load images (via file chooser or drag & drop)
- 🔐 Encode text into an image using LSB
- 🔓 Decode hidden text from an image
- 🔄 Toggle between:
  - Original image
  - Encoded image
  - Difference view (visual comparison)
- 🔍 Zoom in and out
- 🌙 Light/Dark theme toggle
- ⚠️ Error handling (e.g., `MessageTooLargeException`)

---

## 🚀 Technologies Used

- **Java 17+**
- **JavaFX**
- **Gradle** (build system)
- Custom implementation of LSB steganography

---

## ▶️ How to Run

### With Gradle:

```bash
./gradlew run
```

### 🛠️ Build Standalone Application
To generate a fully self-contained build with a custom Java Runtime (no need to install Java separately):

```
./gradlew jlink
```
This will create a directory:
```
build/image/StegoApp
```
It includes:

Executable application

All required dependencies

A minimal embedded JRE tailored for this app

