#!/bin/sh
set -e

echo "=== Verifying PersoPhoto Kotlin Source Files ==="
FAIL=0

for f in $(find app/src -name "*.kt"); do
    # Check brace balance
    OPEN=$(grep -o '{' "$f" | wc -l)
    CLOSE=$(grep -o '}' "$f" | wc -l)
    if [ "$OPEN" -ne "$CLOSE" ]; then
        echo "❌ Brace mismatch in $f: open=$OPEN close=$CLOSE"
        FAIL=1
    fi

    # Check parenthesis balance
    OPEN_P=$(grep -o '(' "$f" | wc -l)
    CLOSE_P=$(grep -o ')' "$f" | wc -l)
    if [ "$OPEN_P" -ne "$CLOSE_P" ]; then
        echo "❌ Paren mismatch in $f: open=$OPEN_P close=$CLOSE_P"
        FAIL=1
    fi
done

if [ "$FAIL" -eq 0 ]; then
    echo "✓ All Kotlin source files have balanced braces & parentheses!"
else
    echo "❌ Some files failed syntax checks!"
    exit 1
fi
