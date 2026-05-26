#include <iostream>
#include <botan/auto_rng.h>
#include <botan/ml_kem.h>
#include <botan/pubkey.h>

void generate_pqc_keys() {
    Botan::AutoSeeded_RNG rng;

    try {
        // Fire up an ML-KEM-768 keypair using Botan's modern C++ wrappers.
        // The AST parser needs to correctly resolve the Botan:: namespace 
        // and pull the specific ML_KEM_768 enum value passed to the constructor.
        Botan::ML_KEM_PrivateKey priv_key(rng, Botan::ML_KEM_Mode::ML_KEM_768);
        
        // Export the public key to verify that object method calls are tracing correctly.
        auto pub_key = priv_key.public_key();
        
        std::cout << "ML-KEM-768 KEM initialized successfully.\n";
        
    } catch (const std::exception& e) {
        std::cerr << "Crypto init error: " << e.what() << "\n";
    }
}

int main() {
    generate_pqc_keys();
    return 0;
}