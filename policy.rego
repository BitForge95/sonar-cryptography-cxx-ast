package cbomkit.policy

# -----------------------------------------------------------------------------
# Base Policy Posture
# -----------------------------------------------------------------------------
default allow = false

allow {
    count(violation) == 0
}

# -----------------------------------------------------------------------------
# Post-Quantum Cryptography (PQC) Evaluation Rules
# -----------------------------------------------------------------------------
# Iterate through the ingested CBOM components using standard index assignment [_]
violation[msg] {
    component := input.components[_]
    
    # Match against the static analysis rule ID triggered by our Java scanner
    component.ruleId == "CPP_OPENSSL_EVP_ENCRYPT"
    
    msg := sprintf(
        "PQC AUDIT REQUIRED: Legacy cryptographic API '%v' detected in %v at line %v. OpenSSL EVP routines must be manually verified for quantum resilience.", 
        [component.apiCall, component.filePath, component.lineNumber]
    )
}