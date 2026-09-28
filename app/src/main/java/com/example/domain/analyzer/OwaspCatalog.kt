package com.example.domain.analyzer

import com.example.data.local.entity.OwaspItemEntity

object OwaspCatalog {
    val initialItems = listOf(
        // OWASP Web Top 10
        OwaspItemEntity(
            code = "A01:2021",
            category = "WEB",
            title = "Broken Access Control",
            description = "Restrictions on what authenticated users are allowed to do are not properly enforced. Attackers can act as users or administrators.",
            defenseStrategy = "Enforce principle of least privilege; deny by default; validate access tokens on every API endpoint server-side; disable web server directory listing."
        ),
        OwaspItemEntity(
            code = "A02:2021",
            category = "WEB",
            title = "Cryptographic Failures",
            description = "Sensitive data exposure in transit or at rest caused by weak cipher suites, deprecated hashing (MD5/SHA1), or hardcoded keys.",
            defenseStrategy = "Encrypt sensitive data in transit with TLS 1.3 and at rest with AES-256-GCM. Never store plain credentials. Hash passwords using Argon2 or bcrypt."
        ),
        OwaspItemEntity(
            code = "A03:2021",
            category = "WEB",
            title = "Injection (SQL, NoSQL, OS Command)",
            description = "User-supplied data is not validated, filtered, or sanitized before being evaluated by an interpreter, allowing unauthorized command execution.",
            defenseStrategy = "Use parameterized queries, PreparedStatements, ORM bindings, safe API wrappers, and strict positive input validation allowlists."
        ),
        OwaspItemEntity(
            code = "A04:2021",
            category = "WEB",
            title = "Insecure Design",
            description = "Flaws related to design and architectural omissions, such as lack of threat modeling, missing business logic limits, and insecure state flows.",
            defenseStrategy = "Implement threat modeling during architecture phases; integrate security unit tests; establish plausible rate limits for financial and auth actions."
        ),
        OwaspItemEntity(
            code = "A05:2021",
            category = "WEB",
            title = "Security Misconfiguration",
            description = "Unnecessary features enabled, default accounts/passwords untouched, overly verbose error stack traces exposed to end-users.",
            defenseStrategy = "Harden environments with automated Infrastructure-as-Code (IaC); disable sample apps, debug endpoints, and default accounts; customize error pages."
        ),
        OwaspItemEntity(
            code = "A06:2021",
            category = "WEB",
            title = "Vulnerable and Outdated Components",
            description = "Using third-party libraries, client frameworks, or container images with known publicly disclosed CVE vulnerabilities.",
            defenseStrategy = "Run automated Software Bill of Materials (SBOM) dependency scans (Dependabot, Snyk, OWASP Dependency-Check); pin approved library versions."
        ),
        OwaspItemEntity(
            code = "A07:2021",
            category = "WEB",
            title = "Identification and Authentication Failures",
            description = "Weak credential verification, missing MFA, session fixation, or predictable session identifiers allowing credential stuffing and hijacking.",
            defenseStrategy = "Enforce Multi-Factor Authentication (MFA); implement rate limiting / lockout on failed login attempts; use cryptographically random session IDs."
        ),
        OwaspItemEntity(
            code = "A08:2021",
            category = "WEB",
            title = "Software and Data Integrity Failures",
            description = "Code and infrastructure that does not protect against integrity violations, such as unverified auto-updates or insecure object deserialization.",
            defenseStrategy = "Sign software artifacts with digital signatures; verify package checksums; use safe serialization formats like JSON over binary serialized objects."
        ),
        OwaspItemEntity(
            code = "A09:2021",
            category = "WEB",
            title = "Security Logging and Monitoring Failures",
            description = "Insufficient logging of security events (failed logins, privilege escalation, high-value transactions), allowing attackers to persist unnoticed.",
            defenseStrategy = "Ensure all login, access control, and server-side validation failures are logged with context; centralize audit logs into SIEM; set alerts for anomalies."
        ),
        OwaspItemEntity(
            code = "A10:2021",
            category = "WEB",
            title = "Server-Side Request Forgery (SSRF)",
            description = "Web application fetches a remote resource without validating the user-supplied URL, allowing attackers to access internal cloud metadata (169.254.169.254).",
            defenseStrategy = "Sanitize and validate all client-supplied URLs; enforce DNS resolution allowlists; disable HTTP redirections; block requests to private IP ranges (RFC 1918)."
        ),

        // OWASP Mobile Top 10
        OwaspItemEntity(
            code = "M01:2024",
            category = "MOBILE",
            title = "Improper Credential Usage",
            description = "Hardcoding API secrets, encryption keys, or administrative credentials directly inside the APK/IPA binary or asset bundle.",
            defenseStrategy = "Store secrets in hardware-backed Android Keystore or EncryptedSharedPreferences; inject API keys through secure backend proxy."
        ),
        OwaspItemEntity(
            code = "M02:2024",
            category = "MOBILE",
            title = "Inadequate Supply Chain Security",
            description = "Vulnerabilities introduced through malicious SDKs, compromised Gradle build plugins, or untrusted third-party dependencies.",
            defenseStrategy = "Audit Gradle build dependencies; verify checksums; minimize third-party tracking SDKs; analyze SDK privacy permissions."
        ),
        OwaspItemEntity(
            code = "M03:2024",
            category = "MOBILE",
            title = "Insecure Authentication / Authorization",
            description = "Bypassing biometric prompts or relying purely on client-side authentication checks without server validation.",
            defenseStrategy = "Enforce all authorization decisions on the server; authenticate biometric challenges using CryptoObject tied to Android Keystore."
        ),
        OwaspItemEntity(
            code = "M04:2024",
            category = "MOBILE",
            title = "Insufficient Input / Output Validation",
            description = "Data from intents, deep links, WebViews, or IPC endpoints is trusted without validation, leading to injection or app hijacking.",
            defenseStrategy = "Validate and sanitize incoming Intent extras, Deep Links, and IPC parameters; disable addJavascriptInterface in WebViews unless required."
        ),
        OwaspItemEntity(
            code = "M05:2024",
            category = "MOBILE",
            title = "Insecure Communication",
            description = "Transmitting sensitive mobile data over cleartext HTTP or failing to validate TLS certificates, permitting MITM attacks.",
            defenseStrategy = "Enforce HTTPS; use Network Security Config to disallow cleartext traffic; implement OkHttp CertificatePinner on high-security endpoints."
        ),
        OwaspItemEntity(
            code = "M06:2024",
            category = "MOBILE",
            title = "Inadequate Privacy Controls",
            description = "Collecting unnecessary device identifiers (IMEI, MAC, location) or logging sensitive PII into system logcat or analytics.",
            defenseStrategy = "Request only least-privilege runtime permissions; strip PII from crash logs; disable Log.d in release builds using Proguard/R8."
        ),
        OwaspItemEntity(
            code = "M07:2024",
            category = "MOBILE",
            title = "Insufficient Binary Protections",
            description = "Lack of code obfuscation, symbol stripping, or root/jailbreak detection, allowing easy decompilation and analysis via JADX/Frida.",
            defenseStrategy = "Enable R8/Proguard code shrinking and obfuscation; remove debug symbols; consider runtime integrity checks (Play Integrity API)."
        ),
        OwaspItemEntity(
            code = "M08:2024",
            category = "MOBILE",
            title = "Security Misconfiguration",
            description = "Exported activities, services, or broadcast receivers without permissions (android:exported=true), or debuggable builds released.",
            defenseStrategy = "Explicitly set android:exported=false on internal components; apply custom permission protectionLevels to exported components."
        ),
        OwaspItemEntity(
            code = "M09:2024",
            category = "MOBILE",
            title = "Insecure Data Storage",
            description = "Storing tokens, passwords, or database files in world-readable external storage or unencrypted SQLite databases.",
            defenseStrategy = "Store data in internal app storage (/data/data); encrypt database files using SQLCipher or Android Keystore MasterKey."
        ),
        OwaspItemEntity(
            code = "M10:2024",
            category = "MOBILE",
            title = "Insufficient Cryptography",
            description = "Using obsolete ciphers (RC4, DES) or weak hardcoded salts and IVs for cryptographic operations.",
            defenseStrategy = "Use Android Keystore-backed AES-256-GCM; generate cryptographically secure IVs using SecureRandom; avoid ECB mode."
        )
    )
}
