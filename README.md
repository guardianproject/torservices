
# TorServices

TorServices is a free proxy app that empowers other apps to use the internet more securely. It acts as a simple Tor "provider" for apps to hide their Internet traffic by encrypting it, then bouncing through a series of computers around the world. Tor is free software and an open network that helps you defend against a form of network surveillance that threatens personal freedom and privacy, confidential business activities and relationships, and state security known as traffic analysis. 

This app is a complement to Orbot, which provides the full, "batteries included" Tor utility.


## Building

`./gradlew assemble` is the quickest way to get started, that builds a universal
APK.  It is also possible to build per-ABI APKs:

```bash
./gradlew assembleRelease -Pabi-splits               # build all variants
./gradlew assembleRelease -Pabi-splits=arm64-v8a     # build one
./gradlew assembleRelease -Pabi-splits=armeabi-v7a,arm64-v8a  # multiple

# or set it in the properties
echo abi-splits=true >> gradle.properties
```

### dependency updates

This project uses gradles dependency verification feature. So whenever a
dependency is changed developers must run:

```
./gradlew --write-verification-metadata pgp,sha256
```

This will update the checksums in `gradle/verification-metadata.xml`.
