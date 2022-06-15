[![Build](https://github.com/promcteam/promccore/actions/workflows/publish.yml/badge.svg?branch=dev)](https://github.com/promcteam/promccore/packages/1129377)

# ${project.name}

If you wish to use ${project.name} as a dependency in your projects, include the following in your `pom.xml`

```xml
<repository>
    <id>promcteam</id>
    <url>https://maven.pkg.github.com/promcteam/promccore</url>
</repository>
        ...
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>VERSION</version>
</dependency>
```

Find version numbers [here](https://github.com/promcteam/promccore/packages/1129377).

Additionally, you'll need to make sure that you have properly configured [Authentication with GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages).

You may also use JitPack!
