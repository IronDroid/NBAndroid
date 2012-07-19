#!/bin/bash

# run this in project root directory to increase specification version of all modules
# and update dependencies in kit

if [ ! -d .hg ] ; then 
  echo "wrong directory" >/dev/stderr
  exit 1
fi

if [ -z "$1" ] ; then 
  echo "Usage: $0 newversion" >/dev/stderr
  exit 2
fi
version=$1

for file in */manifest.mf
do
  sed --in-place -e "s/OpenIDE-Module-Specification-Version: [0-9.]*/OpenIDE-Module-Specification-Version: ${version}/g" ${file}
done

old_major=$(echo ${version} | cut -d. -f1)
old_minor=$(echo ${version} | cut -d. -f2)
old_minor=$(expr ${old_minor} - 1)

sed --in-place -e "s:<specification-version>${old_major}.${old_minor}</specification-version>:<specification-version>${version}</specification-version>:g" kit/nbproject/project.xml

