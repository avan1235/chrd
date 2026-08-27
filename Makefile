.PHONY: all .executable desktop wasm lint .clean-gradle clean

all:
	@# do nothing by default

.executable:
	chmod +X ./gradlew

dev: .executable
	./gradlew desktopApp:hotRun --auto

desktop: .executable
	./gradlew desktopApp:runReleaseDistributable

wasm: .executable
	./gradlew webApp:wasmJsBrowserProductionRun

.clean-gradle: .executable
	./gradlew clean

clean: .clean-gradle .clean-docker