cat > gradlew << 'EOF'
#!/bin/bash

# Gradle Wrapper SIMPLIFICADO
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$JAR" ]; then
    echo "ERROR: No se encontró $JAR"
    exit 1
fi

exec java -jar "$JAR" "$@"
EOF

chmod +x gradlew
