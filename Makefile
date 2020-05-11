version=`cat version.txt`

update-version:
	mvn versions:set -DnewVersion=$(version)


build-docker:
	docker build --build-arg ESM_VERSION=$(version) -t khaller/esm:$(version) .