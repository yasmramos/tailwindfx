# GPG Keys for Maven Central Deployment

## Key Information

- **Key ID**: `277C17BF8E9FB30354421575953D035FAE175710`
- **Short Key ID**: `953D035FAE175710`
- **User ID**: `yasmramos <yasmramos95@gmail.com>`
- **Key Type**: RSA 4096-bit
- **Created**: 2026-07-12
- **Expiration**: Never (0)

## GitHub Secrets Required

Add the following secrets to your GitHub repository (Settings → Secrets → Actions):

### 1. GPG_PRIVATE_KEY
Copy the entire content of `/tmp/gpg_private_key.asc` and paste it as the value.

```bash
# Command to get the value:
cat /tmp/gpg_private_key.asc
```

### 2. GPG_PASSPHRASE
Value: `MavenCentral2024!`

### 3. OSSRH_USERNAME
Your Sonatype OSSRH username (create account at https://s01.oss.sonatype.org/)

### 4. OSSRH_TOKEN
Your Sonatype OSSRH token (generate from OSSRH profile)

## Public Key Distribution

The public key has been uploaded to:
- keyserver.ubuntu.com
- Also available at `/tmp/gpg_public_key.asc`

Verify with:
```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys 277C17BF8E9FB30354421575953D035FAE175710
```

## Revocation Certificate

A revocation certificate has been automatically generated and stored at:
```
/root/.gnupg/openpgp-revocs.d/277C17BF8E9FB30354421575953D035FAE175710.rev
```

**IMPORTANT**: Save this certificate in a secure location. It allows you to revoke your key if it's compromised or lost.

## Test Results

✅ GPG signing test passed
✅ Maven artifacts signed successfully
✅ Signature verification passed

Files signed:
- tailwindfx-1.0.0-SNAPSHOT.jar.asc
- tailwindfx-1.0.0-SNAPSHOT-sources.jar.asc
- tailwindfx-1.0.0-SNAPSHOT-javadoc.jar.asc
- tailwindfx-1.0.0-SNAPSHOT.pom.asc
