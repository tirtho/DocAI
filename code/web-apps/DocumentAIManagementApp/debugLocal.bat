env.bat

copy pom.xml.debugLocal pom.xml

echo "Make sure you have Eclipse is running and setup Eclipse Debug Configuration with Standard socket, localhost, 9090"
echo "After maven starts app locally, run Debug in Eclipse"

mvn package spring-boot:run