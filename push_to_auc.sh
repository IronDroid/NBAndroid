#!/bin/bash

# requires Ruby, RubyGems, bundler and kenai_tools
# look at http://kenai.com/jira/browse/KENAI-2858
# sudo apt-get install ruby,rubygems
# gem install --user-install bundler
# gem install --user-install kenai_tools

: ${VERSION:=dev-$PROMOTED_ID}
: ${PROJECT:=nbandroid}
BUILD_DIR=$WORKSPACE/../builds/$PROMOTED_NUMBER

if [ -z "${PASSWD_FILE}" ]; then 
  echo "Missing PASSWD_FILE environment variable" >/dev/stderr
  exit 1
fi

TEMP=`mktemp -d`
pushd ${TEMP}

mkdir nbandroid-$VERSION
cp -av $BUILD_DIR/archive/build/updates/* nbandroid-$VERSION
zip -r nbandroid-$VERSION.zip nbandroid-$VERSION

mv nbandroid-$VERSION updatecenter
dlutil -p "${PASSWD_FILE}" -r kenai.com,${PROJECT} push updatecenter /
dlutil -p "${PASSWD_FILE}" -r kenai.com,${PROJECT} push nbandroid-$VERSION.zip /archives
rm -rf updatecenter
popd
rm -rf ${TEMP}
