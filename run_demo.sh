#!/bin/bash
echo "======================================"
echo "Building Maven project..."
echo "======================================"
mvn clean compile -q

echo "======================================"
echo "Running AST Scanner against test.cpp..."
echo "======================================"
mvn exec:java -q -Dexec.mainClass="org.pqca.cbomkit.CppCryptoScanner" > cbom_output.json

echo "[*] Scanner Output (CycloneDX JSON):"
cat cbom_output.json

echo ""
echo "======================================"
echo "Evaluating CBOM against OPA Policy..."
echo "======================================"
opa eval -i cbom_output.json -d policy.rego "data.cbomkit.policy"