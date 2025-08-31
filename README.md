## **Aegis Security: IIEST-UCO BANK HACKATHON 2025**

Author: Gradient Geeks  
Date: July 19, 2025  
Version: 1.2  
### **Introduction**

This document provides a complete, explicit walkthrough of the Aegis Security Environment's operation, from the initial app launch to the completion of a secure transaction. It details the role of each component, including the underlying technical and mathematical processes that guarantee security.

***Hackathon Implementation Note:*** *This document describes the full, enterprise-grade architecture. For the purpose of the hackathon demonstration, where publishing to the Play Store and using the live Play Integrity API is not feasible, the Play Integrity checks will be simulated. The core cryptographic logic for key provisioning and HMAC signing remains the same.*

### **The Cast of Components**

* **UCO Bank App:** The main application Anurag interacts with.  
* **Aegis Client SDK (sfe-sdk):** Our library, running silently inside the UCO Bank App to provide security functions.  
* **Aegis Security API:** Our central, authoritative Spring Boot backend that acts as the brain of the system.  
* **UCO Bank Backend:** The bank's own server that manages accounts and processes financial transactions.

### **Phase 1: First-Time App Launch \- The Secure Handshake**

**Objective:** To cryptographically establish that Anurag's app is genuine and to provision his device with a unique, secret identity.

**Step 1: The SDK Wakes Up (Inside the UCO Bank App)**

* **Technical Action:** The moment Anurag opens the app, the sfe-sdk's initialization code runs. It checks its private storage and finds no Secret Key. It immediately knows this is a first-time launch and begins the provisioning process.

**Step 2: Gathering Proof (Inside the Aegis Client SDK)**

* **Technical Action:** Before contacting any server, the SDK prepares its case.  
  1. It retrieves the shared Registration Key that was embedded in the app by the UCO Bank developers.  
  2. It calls Google's Play Integrity API. It generates a large random number, the nonce, to prevent replay attacks.  
     Hackathon Note: For the demo, this step will be commented out. The SDK will proceed without a live token from Google, as the demo app is not published on the Play Store.  
* **Mathematical Detail:** The Play Integrity API communicates with Google's servers, which assess the app and device. Google then creates a **J**SON **W**eb **S**ignature (JWS) token. This token is a string in the format header.payload.signature. The payload contains the integrity verdict and the nonce you sent. The signature is a cryptographic proof created by Google's private key.

**Step 3: The Registration Call (UCO Bank App \-\> Aegis Security API)**

* **Technical Action:** The sfe-sdk makes a POST request to our https://api.aegis-security.io/v1/register. The body of this request contains:  
  {  
    "clientId": "UCOBANK\_PROD\_ANDROID",  
    "registrationKey": "THE\_SHARED\_REGISTRATION\_KEY",  
    "integrityToken": null // Will be null or empty for the demo  
  }

**Step 4: The Rigorous Backend Verification (Inside the Aegis Security API)**

* **Technical Action:** Our Spring Boot application receives the request and performs a multi-stage check.  
  1. **Validate the Integrity Token:** It first ignores the registrationKey. It takes the integrityToken, fetches Google's public keys, and mathematically verifies the token's signature. It also checks that the nonce inside the token matches what it expects.  
  2. **Inspect the Verdict:** If the signature is valid, it inspects the token's payload. It checks for appIntegrity: "PLAY\_RECOGNIZED" and deviceIntegrity: "MEETS\_DEVICE\_INTEGRITY".  
  3. Check the Registration Key: Only if the token is valid and the verdict is clean does it then check if the provided registrationKey matches the one we issued to UCO Bank.  
     Hackathon Note: In our demonstration, the backend will bypass the integrity token validation (Sub-steps 1 & 2 of this section). It will proceed directly to checking the registrationKey (Sub-step 3\) to simulate a trusted environment and allow the key provisioning to complete.  
* **Mathematical Detail (RSA Usage):** The JWS signature verification is an **RSA** or ECDSA cryptographic operation. Your backend uses Google's public RSA key to verify the signature on the integrity token. The formula is conceptually isSignatureValid \= RSA\_Verify(data: header.payload, signature: signature, publicKey: googlePublicKey). This mathematically proves the token is authentic and untampered, ensuring the integrity report can be trusted.

**Step 5: The Secret Key is Born (Inside the Aegis Security API)**

* **Technical Action:** With verification complete, our backend generates a new, unique Secret Key for Anurag's device.  
* **Mathematical Detail:** This key is generated using a **C**ryptographically **S**ecure **P**seudo-**R**andom **N**umber **G**enerator (CSPRNG): secretKey \= CSPRNG.generate(256 bits). This is not derived from any input; it is pure, high-entropy random data, making it impossible to guess. Our backend then stores this key in its database, linked to a unique ID for Anurag's device.

**Step 6: Secure Delivery and Storage (Aegis Security API \-\> UCO Bank App \-\> Aegis Client SDK)**

* **Technical Action:** The Secret Key is returned in the API response. The sfe-sdk immediately takes this key and uses the Android Keystore API to store it in the device's secure hardware. The provisioning is complete.

### **Phase 2: Making the First Secure Transaction**

**Objective:** To prove that a request to transfer money is authentic and has not been tampered with.

**Step 1: User Action (Inside the UCO Bank App)**

* **Technical Action:** Anurag decides to pay a friend ₹100. He enters the details and taps "Confirm." The UCO Bank app creates a JSON object for the transaction.

**Step 2: The Signing Ceremony (Inside the Aegis Client SDK)**

* **Technical Action:** The UCO Bank app's code calls our SDK: signedRequest \= sfeSdk.sign(request).  
  1. The SDK takes the transaction JSON and creates the stringToSign by combining the request details (HTTP method, URI, timestamp, nonce, and a hash of the JSON body).  
  2. It retrieves Anurag's unique Secret Key from the Android Keystore.  
  3. It computes the HMAC signature.  
* **Mathematical Detail:** The signature is calculated as Signature \= HMAC\_SHA256(SecretKey, stringToSign). This function produces a unique and verifiable fingerprint of the request, authenticated with the secret key.

**Step 3: The Signed Request (UCO Bank App \-\> UCO Bank Backend)**

* **Technical Action:** The UCO Bank App sends the transaction request to the **UCO Bank Backend**. This request now includes the extra headers generated by our SDK, including the X-Signature.

**Step 4: The Validation Request (UCO Bank Backend \-\> Aegis Security API)**

* **Technical Action:** The UCO Bank Backend receives the request. It does **not** know Anurag's Secret Key. Its only job is to act as a client to our service. It makes a POST request to our https://api.aegis-security.io/v1/validate. The body of this request contains:  
  {  
    "deviceId": "ANURAGS\_UNIQUE\_DEVICE\_ID",  
    "signature": "THE\_SIGNATURE\_FROM\_THE\_APP",  
    "stringToSign": "THE\_EXACT\_STRING\_TO\_SIGN"  
  }

**Step 5: The Final Verdict (Inside the Aegis Security API)**

* **Technical Action:** Our Spring Boot backend receives the validation request.  
  1. It looks up Anurag's Secret Key in its database using his deviceId.  
  2. It re-computes the signature using the Secret Key and the stringToSign it received.  
  3. It performs a **constant-time comparison** between its calculated signature and the one it received from the UCO Bank Backend.  
* **Mathematical Detail:** The backend performs the exact same calculation: ExpectedSignature \= HMAC\_SHA256(SecretKey, stringToSign). The constant-time comparison (MessageDigest.isEqual()) prevents timing attacks by ensuring the comparison always takes the same amount of time, leaking no information.

**Step 6: The Conclusion (Aegis Security API \-\> UCO Bank Backend \-\> UCO Bank App)**

* **Technical Action:**  
  1. Our Aegis API returns {"isValid": true} to the UCO Bank Backend.  
  2. The UCO Bank Backend, now confident the request is authentic, processes the ₹100 transfer.  
  3. It sends a "Success" message back to Anurag's app.

### **Other Core Security Functions: The Secure Vault**

Once the SDK is initialized, it provides other critical security features. The most important is the "Secure Vault" for protecting sensitive data stored on the device's disk (data at rest).

* **Objective:** To allow the UCO Bank app to securely store data like user settings or cached information.  
* **Technical Action (AES & RSA Usage):** The SDK uses a technique called **Envelope Encryption**.  
  1. The SDK generates a new, random **AES-256** key to encrypt the actual data. AES is used because it is a very fast and strong symmetric encryption algorithm, ideal for data of any size.  
  2. To protect the AES key itself, the SDK uses the Android Keystore's master **RSA** key pair. It encrypts the AES key with the public RSA key.  
  3. The encrypted data and the encrypted AES key are stored together in a file.  
* **Mathematical Detail:**  
  1. **Data Encryption:** EncryptedData \= AES\_Encrypt(PlaintextData, AES\_Key)  
  2. **Key Encryption:** Encrypted\_AES\_Key \= RSA\_Encrypt(AES\_Key, Keystore\_PublicKey)  
* **Security Guarantee:** This process is highly secure. An attacker who steals the file cannot read the data because it is encrypted with AES. They cannot get the AES key because it is encrypted with RSA. And they cannot get the RSA private key needed for decryption because it is physically protected by the device's secure hardware and is non-exportable.
