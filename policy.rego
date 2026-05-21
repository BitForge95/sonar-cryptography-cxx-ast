package cbomkit.policy

# -----------------------------------------------------------------------------
# Base Policy Posture
# -----------------------------------------------------------------------------
# Zero-trust default: Unless explicitly allowed, the code fails compliance.
default allow := false

# The policy allows the code to pass only if there are no violations triggered.
allow if {
    count(violation) == 0
}

# TODO: Add violation logic to evaluate the CycloneDX CBOM input.