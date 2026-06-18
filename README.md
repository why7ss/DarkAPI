# DarkAPI
## Adding API to your project
`build.gradle`
### Repository
```gradle
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
```
### Dependence
```gradle
implementation 'com.github.why7ss7why-max:DarkAPI:v1.0.3'
```

`build.gradle.kts`
### Repository
```gradle
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
		}
```

### Dependence
```gradle
implementation("com.github.why7ss7why-max:DarkAPI:v1.0.3")
```

## First step
* Register your plugin and prefix:
```java
DarkAPI.registerPlugin(this, "<gray>[<aqua>Рефералы<gray>] <white>");
```
* Download DarkAPI to your server

## Console logging
* Default log
```java
log("text"); // logging "text" to console with white color
```
* Error log
```java
error("text"); // logging "text" to console with red color
```

## Parsing with MiniMessage
```java
player.sendMessage(DarkAPI.parse("<prefix>Hello!")); // showing text "[Рефералы] Hello"
```
