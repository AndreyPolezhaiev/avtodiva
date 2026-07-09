# Avtodiva (Desktop ERP for Driving Schools)


## 💡Overview

This project designed to handle school management operations with a focus on scalability, and maintainability.


## 🛠 Tech Stack

* **Backend:** Java 21, Spring Boot, Java Swing.
* **Database:** PostgreSQL (AWS).


## 🏗 Architecture Highlights

* **Automatic backups:** It is configured the automatic functionality to make the DB snapshots that will be stored in the `D:\backups`.


## 📂 Project Structure

* `src/main/java/...`: Backend business logic and REST controllers.
* `src/main/resources/...`: Configuration files.


## 🚀 How to Run
1. Install the [Java 21 (Direct download Link!!!)](https://download.oracle.com/java/21/latest/jdk-21_windows-x64_bin.exe) on your PC
2. Clone the repository: `git clone https://github.com/AndreyPolezhaiev/avtodiva-advanced`
3. Configure your `application.properties` file by adding directly DB credentials.
4. Run with mvn: `mvn clean package`
5. Move the `avtodiva-0.0.1-SNAPSHOT.jar` in the suitable directory for you and simply run it as usual app by double click. 

---

Developed and maintained by **Andrii Polezhaiev**.