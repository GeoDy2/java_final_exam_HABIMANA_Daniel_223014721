Research Platform Management System (RPMS)


**Project Overview**

The Research Platform Management System (RPMS) is a Java-based desktop application developed to support the management of academic research activities. The system provides a centralized environment for handling researchers, projects, datasets, experiments, and publications in an organized and structured manner.

The application is implemented using Java Swing for the graphical user interface and MySQL for persistent data storage. It is intended for academic institutions or research teams that require a simple, desktop-based solution for managing research information.

**Project Objectives**

The main objectives of the Research Platform Management System are to:

Centralize the management of research-related data

Allow efficient creation, viewing, updating, and deletion of records

Maintain clear relationships between researchers, projects, datasets, and experiments

Ensure data consistency using a relational database

Provide a user-friendly interface suitable for non-technical users

Support data export for reporting and documentation

**System Features**

User authentication with role identification (Admin/User)

Researcher profile management

Project management with status tracking

Dataset management linked to publications

Experiment tracking and dataset association

Publication record management

Search and filtering functionality

CSV export for datasets and projects

Dashboard-based navigation using tabs and panels

**System Architecture**

The system follows a layered architectural approach, separating responsibilities for clarity and maintainability:

__Presentation Layer__
Java Swing components including LoginForm, ResearchPMS, dashboard panels, tables, dialogs, and UI styling.

__Business Logic Layer__
Handles input validation, event handling, navigation logic, and coordination of CRUD operations.

__Data Access Layer__
Implemented using DB.java and SQL queries executed within panel classes via JDBC.

__Database Layer__
A MySQL database named research_platform, storing all research-related entities.

**Technologies Used**

Programming Language: Java

GUI Framework: Java Swing

Database: MySQL

Database Connectivity: JDBC

IDE:  Eclipse

Version Control:  GitHub

**Database Overview**

The system uses a MySQL database named research_platform.
Core tables include:

researcher

project

dataset

experiment

publication

fundings

Eexperiment_dataset

These tables are connected through primary and foreign key relationships to ensure referential integrity and proper data linkage.

**Testing**

The application was tested using a combination of:

Unit testing for individual components

Integration testing between the GUI and database

System testing to verify complete workflows

Informal user acceptance testing (UAT)

All essential system functions were verified to operate correctly.

**Project Structure**
src/
├── com.form/
│   ├── LoginForm.java
│   ├── ResearchPMS.java
│   ├── UIStyle.java
│   └── DB.java
│
├── com.panel/
│   ├── DashboardPanel.java
│   ├── DatasetPanel.java
│   ├── ExperimentPanel.java
│   ├── FundingPanel.java
│   ├── MyProfilePanel.java
│   ├── ProjectPanel.java
│   ├── PublicationPanel.java
│   └── ResearcherPanel.java
│
├── icons/
    └── (application icons)


🚀 How to Run the Project

Clone the repository:

git clone https://github.com/GeoDy2/java_final_exam_HABIMANA_Daniel_223014721


Import the project into your Java IDE.

Create a MySQL database named:

research_platform


Import the provided SQL schema (available).

Update database credentials in DB.java.

Run the application from the Main class.

**User Roles**

Admin: Full access to all system modules and management features

User: Access limited to permitted operations based on role

**Future Enhancements**

Role-based permission enforcement

Advanced reporting and analytics

Improved security features

Web-based or cloud-based version

Integration with external research identifiers (e.g., ORCID)

**Academic Context**

This project was developed as an academic research and software engineering project. It demonstrates practical application of:

+ System analysis and design

+ Java Swing GUI development

+ Relational database design

+ Layered software architecture

+ Testing and evaluation methodologies

**License**

This project is intended for educational and academic use only.
