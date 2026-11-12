# Restaurant Ordering System

Java 17 desktop application for creating meals, adding toppings, placing orders, tracking kitchen progress and receiving customer notifications.

Factory Method, Abstract Factory, Decorator, Observer, Strategy and Facade are integrated into the ordering workflow. Regular and vegetarian menu families supply meals and matching drinks. Order history is stored in `data/orders.xml`. Notification feeds and the event log belong to the current application session.

Application entry point: `com.example.restaurant.Main`. `Launch.bat` opens the included executable JAR. `launch.ps1` compiles and starts the desktop application; `launch.ps1 -Demo` runs the console transaction. `build.ps1` executes JUnit tests and builds the executable JAR. Maven project configuration is in `pom.xml`.

The PDF report, UML diagrams and application images are in `docs`.
