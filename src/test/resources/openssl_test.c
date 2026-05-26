#include <stdio.h>
#include <openssl/evp.h>
#include <openssl/core_names.h>

void initialize_crypto_contexts() {
    EVP_PKEY_CTX *legacy_ctx = NULL;
    EVP_PKEY_CTX *pqc_ctx = NULL;

    // Grab a context for legacy RSA. 
    // The AST scanner needs to catch the "RSA" string literal here and flag it as vulnerable.
    legacy_ctx = EVP_PKEY_CTX_new_from_name(NULL, "RSA", NULL);
    if (!legacy_ctx) {
        printf("Failed to init RSA context.\n");
    }

    // Spin up a context for ML-KEM. 
    // The parser should extract "ML-KEM-768" and map it to a secure PQC primitive.
    pqc_ctx = EVP_PKEY_CTX_new_from_name(NULL, "ML-KEM-768", NULL);
    if (!pqc_ctx) {
        printf("Failed to init ML-KEM context.\n");
    }

    // Cleanup
    if (legacy_ctx) EVP_PKEY_CTX_free(legacy_ctx);
    if (pqc_ctx) EVP_PKEY_CTX_free(pqc_ctx);
}

int main() {
    initialize_crypto_contexts();
    return 0;
}