#/bin/bash
oc login https://ec2-54-202-93-168.us-west-2.compute.amazonaws.com:8443 -u developer -p developer --insecure-skip-tls-verify=true
sed -i "s/VERSION_APP/$(cat gradle.properties | grep "$version" | cut -d'=' -f2)/g" build.yml
oc project workshop
oc replace -f build.yml
oc start-build transaction-svc-build
git checkout build.yml