package com.example.domain.analyzer

data class SecurityPlaybook(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val steps: List<PlaybookStep>,
    val quickScript: String? = null
)

data class PlaybookStep(
    val title: String,
    val details: String,
    val commandHint: String? = null
)

object SecurityPlaybooks {
    val playbooks = listOf(
        SecurityPlaybook(
            id = "PB-01",
            title = "Internal Defensive Penetration Testing Protocol",
            category = "Methodology",
            summary = "Structured rules of engagement and methodology for IT teams auditing their own infrastructure safely without causing outages.",
            steps = listOf(
                PlaybookStep(
                    title = "1. Authorization & Scope Definition",
                    details = "Obtain explicit written permission from asset owners. Clearly delineate in-scope IP addresses, domains, and production exclusions. Set testing windows outside peak business hours."
                ),
                PlaybookStep(
                    title = "2. Non-Destructive Reconnaissance",
                    details = "Perform passive DNS, certificate transparency, and non-intrusive service discovery. Avoid DoS/stress testing against production systems.",
                    commandHint = "nmap -sV -sC -Pn --top-ports 100 -T3 192.168.1.0/24"
                ),
                PlaybookStep(
                    title = "3. Vulnerability Identification & Proof of Concept",
                    details = "Verify findings with benign proofs-of-concept (e.g., retrieving whoami or database version). Never execute destructive payloads, drop files, or alter production data."
                ),
                PlaybookStep(
                    title = "4. Root Cause Analysis & Mitigation Guidance",
                    details = "Document reproduction steps, CVSS scoring, exact affected line/config, and concrete remediation code before presenting to the development team."
                ),
                PlaybookStep(
                    title = "5. Re-Testing & Sign-off",
                    details = "Re-audit the mitigated asset to verify the patch prevents bypasses and that no regression was introduced."
                )
            ),
            quickScript = """
# Quick safe discovery script for self-owned network
echo "[*] Scanning self-owned subnet safely..."
nmap -sS -T3 -F 192.168.1.0/24 -oN internal_audit_summary.txt
echo "[+] Audit baseline logged to internal_audit_summary.txt"
""".trimIndent()
        ),
        SecurityPlaybook(
            id = "PB-02",
            title = "Linux Server Baseline Hardening Checklist",
            category = "Infrastructure",
            summary = "Production hardening baseline for Debian/Ubuntu and RHEL servers hosting internal or external services.",
            steps = listOf(
                PlaybookStep(
                    title = "1. SSH Daemon Hardening",
                    details = "Disable root SSH login, disable password authentication, use ed25519 public keys only, set idle timeout.",
                    commandHint = "PermitRootLogin no\nPasswordAuthentication no\nMaxAuthTries 3"
                ),
                PlaybookStep(
                    title = "2. Enable Host Firewall (UFW / iptables)",
                    details = "Default policy: DROP all incoming, allow established/related, allow strictly required ports only.",
                    commandHint = "ufw default deny incoming\nufw allow in 22/tcp\nufw enable"
                ),
                PlaybookStep(
                    title = "3. Automated Security Patches",
                    details = "Configure unattended-upgrades to automatically apply critical security patches daily.",
                    commandHint = "apt-get install unattended-upgrades && dpkg-reconfigure --priority=low unattended-upgrades"
                ),
                PlaybookStep(
                    title = "4. Brute-Force Protection (Fail2ban)",
                    details = "Install and enable fail2ban to automatically jail IPs attempting repeated failed authentications.",
                    commandHint = "apt-get install fail2ban\nsystemctl enable --now fail2ban"
                ),
                PlaybookStep(
                    title = "5. Kernel Sysctl Network Hardening",
                    details = "Disable IP forwarding, ignore ICMP broadcast requests, enable TCP SYN cookies to mitigate SYN flood attacks."
                )
            ),
            quickScript = """
#!/usr/bin/env bash
# Quick Sysctl Hardening Snippet
cat <<EOF >> /etc/sysctl.d/99-security.conf
net.ipv4.tcp_syncookies = 1
net.ipv4.conf.all.rp_filter = 1
net.ipv4.conf.all.accept_source_route = 0
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.icmp_echo_ignore_broadcasts = 1
EOF
sysctl --system
""".trimIndent()
        ),
        SecurityPlaybook(
            id = "PB-03",
            title = "Docker & Container Hardening Guide",
            category = "DevSecOps",
            summary = "Essential security configurations for building and running Docker containers in compliance with CIS Docker Benchmark.",
            steps = listOf(
                PlaybookStep(
                    title = "1. Non-Root User Execution",
                    details = "Never run container processes as root. Define a dedicated UID/GID inside the Dockerfile.",
                    commandHint = "RUN groupadd -r appuser && useradd -r -g appuser appuser\nUSER appuser"
                ),
                PlaybookStep(
                    title = "2. Read-Only Root Filesystem",
                    details = "Mount root container filesystem as read-only. Mount writable temp paths as tmpfs.",
                    commandHint = "docker run --read-only --tmpfs /tmp --tmpfs /run"
                ),
                PlaybookStep(
                    title = "3. Drop Kernel Capabilities",
                    details = "Drop all default Linux capabilities and add back only strictly necessary ones (e.g. NET_BIND_SERVICE).",
                    commandHint = "docker run --cap-drop=ALL --cap-add=NET_BIND_SERVICE"
                ),
                PlaybookStep(
                    title = "4. Prevent Privilege Escalation",
                    details = "Pass the no-new-privileges flag to prevent SUID binaries from granting elevated permissions.",
                    commandHint = "docker run --security-opt=no-new-privileges:true"
                ),
                PlaybookStep(
                    title = "5. Minimal Distroless Base Images",
                    details = "Use Google Distroless or Alpine minimal images to minimize package attack surface and omit shells."
                )
            ),
            quickScript = """
# Hardened multi-stage Dockerfile skeleton
FROM eclipse-temurin:21-jre-alpine AS runner
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --chown=appuser:appgroup app.jar .
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
""".trimIndent()
        ),
        SecurityPlaybook(
            id = "PB-04",
            title = "SANS 6-Step Incident Response Protocol",
            category = "Incident Response",
            summary = "Step-by-step triage guide when detecting active security breaches or anomalous intrusions.",
            steps = listOf(
                PlaybookStep(
                    title = "Step 1: Preparation",
                    details = "Maintain contact lists, out-of-band communication channels, forensic toolkits, and validated immutable backups."
                ),
                PlaybookStep(
                    title = "Step 2: Identification & Triage",
                    details = "Collect memory snapshots and disk images before rebooting. Preserve log files (auth.log, syslog, firewall, web access logs). Calculate SHA-256 of artifacts."
                ),
                PlaybookStep(
                    title = "Step 3: Containment",
                    details = "Isolate compromised hosts from network (pull cable or assign isolation VLAN). Block attacker IP/domain at perimeter firewall. Revoke compromised credentials and rotate API tokens."
                ),
                PlaybookStep(
                    title = "Step 4: Eradication",
                    details = "Identify entry vector (vulnerability/phish). Wipe compromised systems cleanly and rebuild from verified golden images rather than trying to clean up backdoors."
                ),
                PlaybookStep(
                    title = "Step 5: Recovery",
                    details = "Restore clean data from pre-infection backups. Reconnect to network with heightened telemetry and logging. Monitor closely for persistent beaconing."
                ),
                PlaybookStep(
                    title = "Step 6: Lessons Learned",
                    details = "Conduct post-incident review within 7 days. Update threat models, patch root vulnerability, and enhance detection rules."
                )
            ),
            quickScript = """
# Immediate host isolation iptables rule (preserves local console access)
iptables -F
iptables -A INPUT -i lo -j ACCEPT
iptables -A OUTPUT -o lo -j ACCEPT
iptables -P INPUT DROP
iptables -P FORWARD DROP
iptables -P OUTPUT DROP
echo "[!] System isolated from network"
""".trimIndent()
        )
    )
}
