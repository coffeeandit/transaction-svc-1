#/bin/bash
sed -i "s/VERSION_APP/$(cat gradle.properties | grep "$version" | cut -d'=' -f2)/g" build.yml
oc project workshop
oc replace -f build.yml
oc start-build transaction-build
git checkout build.yml