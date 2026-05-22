package cbomkit.policy

# -----------------------------------------------------------------------------
# Base Policy Posture
# -----------------------------------------------------------------------------
default allow := false

allow if {
    count(violation) == 0
}

# -----------------------------------------------------------------------------
# Post-Quantum Cryptography (PQC) Evaluation Rules
# -----------------------------------------------------------------------------
# Iterate through the ingested CBOM components. If we detect legacy OpenSSL 
# primitives, we generate a violation message.

violation[msg] {
    # Traverse the input JSON array (e.g., input.components)
    some component in input.components
    
    # Match against the static analysis rule ID triggered by our Java scanner
    component.ruleId == "CPP_OPENSSL_EVP_ENCRYPT"
    
    msg := sprintf(
        "PQC AUDIT REQUIRED: Legacy cryptographic API '%v' detected in %v at line %v. OpenSSL EVP routines must be manually verified for quantum resilience.", 
        [component.apiCall, component.filePath, component.lineNumber]
    )
}