#include <openssl/evp.h>
#include <openssl/err.h>
#include <iostream>

void encrypt_data(const unsigned char* key, const unsigned char* iv) {
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    if (!ctx) {
        std::cerr << "Failed to allocate cipher context" << std::endl;
        return;
    }

    // Target for AST scanner: EVP_EncryptInit_ex
    // Hardcoding AES-256-CBC for this PoC. 
    if (1 != EVP_EncryptInit_ex(ctx, EVP_aes_256_cbc(), nullptr, key, iv)) {
        std::cerr << "Encryption init failed" << std::endl;
    } else {
        std::cout << "Context initialized with AES-256-CBC" << std::endl;
    }
    
    // TODO: Add context cleanup
}

int main(int argc, char* argv[]) {
    std::cout << "Crypto baseline" << std::endl;
    return 0;
}