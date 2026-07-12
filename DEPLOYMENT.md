# Maven Central Deployment Guide

This document explains how to publish TailwindFX to Maven Central.

## Prerequisites

### 1. Sonatype OSSRH Account

1. Create an account at [https://s01.oss.sonatype.org/](https://s01.oss.sonatype.org/)
2. Create a new ticket/issue at [https://issues.sonatype.org/](https://issues.sonatype.org/) to request namespace ownership for `io.github.yasmramos`
   - You'll need to verify ownership of your GitHub account
   - Once approved, you can deploy under the `io.github.yasmramos` groupId

### 2. GPG Key Setup

Generate a GPG key for signing artifacts:

```bash
# Generate GPG key
gpg --full-generate-key

# List your keys
gpg --list-keys

# Export public key (to share with others)
gpg --armor --export yasmramos95@gmail.com > public-key.asc

# Export private key (keep this secure!)
gpg --armor --export-secret-keys yasmramos95@gmail.com > private-key.asc
```

### 3. Maven Settings

Create or update `~/.m2/settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>ossrh</id>
            <username>your-ossrh-username</username>
            <password>your-ossrh-token</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>release</id>
            <properties>
                <gpg.keyname>YOUR_GPG_KEY_ID</gpg.keyname>
                <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
</settings>
```

## GitHub Secrets Configuration

For automated deployment via GitHub Actions, configure these secrets in your repository settings:

1. Go to: **Settings → Secrets and variables → Actions**
2. Add the following secrets:

| Secret Name | Description |
|------------|-------------|
| `OSSRH_USERNAME` | Your Sonatype OSSRH username |
| `OSSRH_TOKEN` | Your Sonatype OSSRH token (generate from OSSRH profile) |
| `GPG_PRIVATE_KEY` | Content of your private-key.asc file |
| `GPG_PASSPHRASE` | Passphrase for your GPG key |

## Local Deployment

### Deploy Snapshot Version

```bash
mvn clean deploy
```

### Deploy Release Version

1. Update version in `pom.xml` (remove `-SNAPSHOT` suffix):
   ```xml
   <version>1.0.0</version>
   ```

2. Build and deploy with GPG signing:
   ```bash
   mvn clean deploy -P release -DperformRelease=true
   ```

3. Update version back to snapshot:
   ```xml
   <version>1.0.1-SNAPSHOT</version>
   ```

## Automated Deployment via GitHub Actions

The workflow is configured to trigger on:
- **Release creation**: Automatically deploys when you create a GitHub release
- **Manual dispatch**: Manually trigger from Actions tab with version input

### Steps to Release:

1. **Update version** in `pom.xml`:
   ```bash
   mvn versions:set -DnewVersion=1.0.0
   mvn versions:commit
   ```

2. **Commit and push**:
   ```bash
   git add pom.xml
   git commit -m "chore: release version 1.0.0"
   git push origin develop
   ```

3. **Create a GitHub Release**:
   - Go to Releases → Draft a new release
   - Tag version: `v1.0.0`
   - Release title: `v1.0.0`
   - Publish release

4. **Monitor the workflow**:
   - Go to Actions tab
   - Watch "Publish to Maven Central" workflow
   - Once complete, artifacts will be in OSSRH staging

5. **Release from OSSRH**:
   - Log into [https://s01.oss.sonatype.org/](https://s01.oss.sonatype.org/)
   - Go to Staging Repositories
   - Close and release your repository
   - Artifacts will sync to Maven Central within ~2 hours

## Verification

After deployment, verify your artifact is available:

1. **Check OSSRH**: [https://s01.oss.sonatype.org/#search](https://s01.oss.sonatype.org/#search)
2. **Check Maven Central**: [https://central.sonatype.com/search](https://central.sonatype.com/search)
3. **Search by groupId**: `io.github.yasmramos`

## Troubleshooting

### Common Issues

**GPG signing fails:**
```bash
# Ensure gpg-agent is running
eval $(gpg-agent --daemon)

# Or use loopback mode (configured in pom.xml)
```

**Deployment authorization fails:**
- Verify OSSRH credentials in `~/.m2/settings.xml` or GitHub secrets
- Ensure your OSSRH account has permissions for the namespace

**Javadoc errors:**
- The pom.xml is configured with `-Xdoclint:none` to bypass strict javadoc checks
- Fix any broken @link references in source code

## Required Artifacts

Maven Central requires these artifacts for each deployment:
- ✅ `tailwindfx-{version}.jar` - Main library JAR
- ✅ `tailwindfx-{version}-sources.jar` - Source code JAR
- ✅ `tailwindfx-{version}-javadoc.jar` - Javadoc JAR
- ✅ `tailwindfx-{version}.pom` - POM file
- ✅ `tailwindfx-{version}.jar.asc` - GPG signature (all above files)

## Next Steps

1. Complete OSSRH namespace verification
2. Generate and configure GPG keys
3. Set up GitHub secrets
4. Test with a snapshot deployment
5. Create your first release!

For more information:
- [Sonatype OSSRH Guide](https://central.sonatype.org/pages/ossrh-guide.html)
- [Maven Deploy Plugin](https://maven.apache.org/plugins/maven-deploy-plugin/)
- [GPG Signing](https://central.sonatype.org/pages/working-with-pgp-signature-files.html)
