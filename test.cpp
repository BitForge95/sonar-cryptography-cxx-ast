#include <openssl/evp.h>
#include <openssl/err.h>
#include <iostream>

void encrypt_data(const unsigned char* key, const unsigned char* iv) {
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    if (!ctx) {
        std::cerr << "Failed to allocate cipher context" << std::endl;
        return;
    }

    /* 
     * CBOMKIT TARGET:
     * Function Call: EVP_EncryptInit_ex
     * Arg 0 (Context): ctx
     * Arg 1 (Algorithm): EVP_aes_256_cbc() -> Our CDT parser must extract this node!
     * Arg 2 (Engine): nullptr
     * Arg 3 (Key): key
     * Arg 4 (IV): iv
     */
    if (1 != EVP_EncryptInit_ex(ctx, EVP_aes_256_cbc(), nullptr, key, iv)) {
        std::cerr << "Encryption init failed" << std::endl;
    } else {
        std::cout << "Context initialized with AES-256-CBC" << std::endl;
    }
    
    // Clean up the context to prevent memory leaks
    EVP_CIPHER_CTX_free(ctx);
}

int main(int argc, char* argv[]) {
    // Dummy 256-bit key and 128-bit IV for static analysis testing.
    // In a real app, these should be securely derived.
    unsigned char key[32] = {0x01, 0x02, 0x03, 0x04}; 
    unsigned char iv[16]  = {0x0A, 0x0B, 0x0C, 0x0D};  
    
    encrypt_data(key, iv);
    
    return 0;
}