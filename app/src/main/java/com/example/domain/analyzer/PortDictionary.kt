package com.example.domain.analyzer

data class PortSecurityEntry(
    val port: Int,
    val service: String,
    val protocol: String,
    val category: String, // "Remote Access", "Web", "Database", "File Sharing", "Infrastructure"
    val riskLevel: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val attackVectors: String,
    val hardeningTips: String
)

object PortDictionary {
    val entries = listOf(
        PortSecurityEntry(
            port = 21,
            service = "FTP",
            protocol = "TCP",
            category = "File Sharing",
            riskLevel = "HIGH",
            attackVectors = "Cleartext transmission of passwords; anonymous authentication exploits; bounce attacks; unpatched ProFTPd/vsftpd backdoors.",
            hardeningTips = "Disable plain FTP. Migrate to SFTP (over SSH port 22) or FTPS with TLS. Disable anonymous login."
        ),
        PortSecurityEntry(
            port = 22,
            service = "SSH",
            protocol = "TCP",
            category = "Remote Access",
            riskLevel = "MEDIUM",
            attackVectors = "Credential brute-forcing; weak password dictionaries; private key exposure; unpatched OpenSSH vulnerabilities (e.g. regreSSHion).",
            hardeningTips = "Disable password auth in sshd_config (enforce ed25519 keys); disable root login (PermitRootLogin no); deploy fail2ban; change default port or restrict via firewall."
        ),
        PortSecurityEntry(
            port = 23,
            service = "Telnet",
            protocol = "TCP",
            category = "Remote Access",
            riskLevel = "CRITICAL",
            attackVectors = "Completely unencrypted text protocol; credential sniffing on local network; MITM session hijacking.",
            hardeningTips = "Immediately terminate and disable Telnet service. Replace with SSHv2."
        ),
        PortSecurityEntry(
            port = 25,
            service = "SMTP",
            protocol = "TCP",
            category = "Infrastructure",
            riskLevel = "MEDIUM",
            attackVectors = "Open relay exploitation for spamming; user enumeration (VRFY / EXPN commands); spoofing without SPF/DKIM/DMARC.",
            hardeningTips = "Close open relay; require STARTTLS and SMTP authentication; implement strict SPF, DKIM, and DMARC DNS records."
        ),
        PortSecurityEntry(
            port = 53,
            service = "DNS",
            protocol = "TCP/UDP",
            category = "Infrastructure",
            riskLevel = "MEDIUM",
            attackVectors = "DNS amplification DDoS; DNS cache poisoning; unauthorized zone transfers (AXFR); subdomain enumeration.",
            hardeningTips = "Restrict zone transfers to trusted secondary DNS servers only; enable DNSSEC; disable recursion on authoritative servers."
        ),
        PortSecurityEntry(
            port = 80,
            service = "HTTP",
            protocol = "TCP",
            category = "Web",
            riskLevel = "MEDIUM",
            attackVectors = "Unencrypted web traffic; session cookie hijacking; credential harvesting; packet sniffing.",
            hardeningTips = "Enforce automatic 301 redirect to HTTPS (port 443); enable HSTS with preload header."
        ),
        PortSecurityEntry(
            port = 88,
            service = "Kerberos",
            protocol = "TCP/UDP",
            category = "Infrastructure",
            riskLevel = "HIGH",
            attackVectors = "AS-REP Roasting; Kerberoasting (requesting TGS tickets for offline cracking); Golden/Silver ticket persistence.",
            hardeningTips = "Enforce AES encryption (disable RC4/DES); enforce long, high-entropy service account passwords (>25 chars); monitor TGS requests."
        ),
        PortSecurityEntry(
            port = 135,
            service = "RPC Endpoint Mapper",
            protocol = "TCP",
            category = "Infrastructure",
            riskLevel = "HIGH",
            attackVectors = "Remote RPC enumeration; MS-RPC exploitation; relay attacks.",
            hardeningTips = "Never expose to the public Internet. Restrict access using Windows Firewall or hardware perimeter firewalls."
        ),
        PortSecurityEntry(
            port = 139,
            service = "NetBIOS Session",
            protocol = "TCP",
            category = "File Sharing",
            riskLevel = "HIGH",
            attackVectors = "Null session enumeration; SMB signing bypass; legacy Windows exploitation.",
            hardeningTips = "Disable NetBIOS over TCP/IP in network adapter settings; block ports 137-139 at boundary."
        ),
        PortSecurityEntry(
            port = 389,
            service = "LDAP",
            protocol = "TCP/UDP",
            category = "Infrastructure",
            riskLevel = "HIGH",
            attackVectors = "Cleartext LDAP credential sniffing; anonymous bind user enumeration; LDAP injection.",
            hardeningTips = "Enforce LDAPS (port 636) with TLS; disable anonymous binding; enforce LDAP channel binding and signing."
        ),
        PortSecurityEntry(
            port = 443,
            service = "HTTPS",
            protocol = "TCP",
            category = "Web",
            riskLevel = "LOW",
            attackVectors = "Weak cipher suites (RC4, 3DES); unpatched OpenSSL vulnerabilities; expired or self-signed certificates; web application flaws (OWASP Top 10).",
            hardeningTips = "Disable SSLv3, TLS 1.0, and TLS 1.1; enforce TLS 1.2/1.3 with forward secrecy (ECDHE); configure automated certificate renewal (Let's Encrypt)."
        ),
        PortSecurityEntry(
            port = 445,
            service = "SMB over IP",
            protocol = "TCP",
            category = "File Sharing",
            riskLevel = "CRITICAL",
            attackVectors = "EternalBlue (MS17-010); WannaCry / NotPetya lateral movement; NTLM relay attacks; PsExec remote execution.",
            hardeningTips = "Block port 445 on WAN firewalls; disable SMBv1 entirely; require SMB Signing and SMB Encryption (SMB 3.1.1)."
        ),
        PortSecurityEntry(
            port = 1433,
            service = "MS-SQL Server",
            protocol = "TCP",
            category = "Database",
            riskLevel = "CRITICAL",
            attackVectors = "Brute-forcing 'sa' account; xp_cmdshell command execution; SQL injection pivoting.",
            hardeningTips = "Never expose MS-SQL directly to WAN; disable or rename 'sa' account; disable xp_cmdshell; require Windows Authentication / TLS."
        ),
        PortSecurityEntry(
            port = 1521,
            service = "Oracle DB TNS",
            protocol = "TCP",
            category = "Database",
            riskLevel = "HIGH",
            attackVectors = "TNS poisoning; default administrative passwords (SYS, SYSTEM); PL/SQL injection.",
            hardeningTips = "Enable valid node checking (sqlnet.ora); enforce encryption and strong password policies; keep on isolated subnet."
        ),
        PortSecurityEntry(
            port = 3306,
            service = "MySQL / MariaDB",
            protocol = "TCP",
            category = "Database",
            riskLevel = "HIGH",
            attackVectors = "Unauthenticated network binding (0.0.0.0); brute-force attacks on root; privilege escalation through user-defined functions (UDF).",
            hardeningTips = "Bind exclusively to 127.0.0.1 or internal private subnet; disable remote root login; enforce strong passwords and TLS connections."
        ),
        PortSecurityEntry(
            port = 3389,
            service = "RDP (Remote Desktop)",
            protocol = "TCP/UDP",
            category = "Remote Access",
            riskLevel = "CRITICAL",
            attackVectors = "BlueKeep (CVE-2019-0708); automated credential stuffing; ransomware initial access broker vector.",
            hardeningTips = "Never expose RDP directly to the Internet; place behind an authenticated VPN or Zero-Trust Gateway; enable Network Level Authentication (NLA); enable MFA."
        ),
        PortSecurityEntry(
            port = 5432,
            service = "PostgreSQL",
            protocol = "TCP",
            category = "Database",
            riskLevel = "HIGH",
            attackVectors = "Weak trust authentication in pg_hba.conf; remote command execution via COPY PROGRAM; CVE-2019-9193.",
            hardeningTips = "Configure pg_hba.conf to require scram-sha-256; bind to localhost or private VPC only; enforce TLS with client certificates."
        ),
        PortSecurityEntry(
            port = 6379,
            service = "Redis Cache",
            protocol = "TCP",
            category = "Database",
            riskLevel = "CRITICAL",
            attackVectors = "Default unauthenticated access; unauthorized CONFIG SET commands to write webshells or root SSH keys.",
            hardeningTips = "Enable protected-mode yes; require strong password (requirepass); rename or disable dangerous commands (CONFIG, FLUSHALL); bind to loopback."
        ),
        PortSecurityEntry(
            port = 8080,
            service = "HTTP Alternate / Tomcat",
            protocol = "TCP",
            category = "Web",
            riskLevel = "HIGH",
            attackVectors = "Default administrative manager credentials (Tomcat manager/html); proxy exploitation; staging/dev exposure.",
            hardeningTips = "Remove default webapps (docs, examples, manager); restrict manager application access to localhost IP valve; protect with WAF."
        ),
        PortSecurityEntry(
            port = 9200,
            service = "Elasticsearch REST",
            protocol = "TCP",
            category = "Database",
            riskLevel = "CRITICAL",
            attackVectors = "Unauthenticated data exfiltration; deletion of indices; Log4Shell / remote script execution in legacy versions.",
            hardeningTips = "Enable X-Pack / Elastic Security with TLS; require authentication; never expose to public network."
        ),
        PortSecurityEntry(
            port = 27017,
            service = "MongoDB",
            protocol = "TCP",
            category = "Database",
            riskLevel = "CRITICAL",
            attackVectors = "Unauthenticated database access (historical default); automated ransomware bots wiping data and leaving ransom note.",
            hardeningTips = "Enable security.authorization: enabled in mongod.conf; bind to 127.0.0.1; require SCRAM-SHA-256 auth; restrict firewall access."
        )
    )
}
