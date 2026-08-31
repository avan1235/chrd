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

tag:
	git describe --tags --abbrev=0 --match "v*.*.*" | awk -F. '{print $$1"."$$2"."$$3+1}' | xargs -I {} sh -c 'git tag -a "$$1" -m "$$1" && git push --follow-tags origin master' -- {}

clean: .clean-gradle .clean-docker--follow-tags