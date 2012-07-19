#!/bin/bash

if [ ! -f "$1" ] ; then 
  echo "usage: $0 nbm_file" >/dev/stderr
fi

# add JDK_HOME/bin to path
if [ -n "$2" ] ; then
  PATH="${PATH}:$2/bin"
fi

create_nbm () {
  NBM_TMPDIR=$(mktemp -d)
  unzip -d ${NBM_TMPDIR} "$1"
  pushd ${NBM_TMPDIR}
  sed s/testrunner69/${2}/g \
      netbeans/config/Modules/org-netbeans-modules-android-testrunner69.xml \
      >netbeans/config/Modules/org-netbeans-modules-android-${2}.xml
  rm netbeans/config/Modules/org-netbeans-modules-android-testrunner69.xml
  unpack200 netbeans/modules/org-netbeans-modules-android-testrunner69.jar.pack.gz \
      netbeans/modules/org-netbeans-modules-android-testrunner69.jar
  rm netbeans/modules/org-netbeans-modules-android-testrunner69.jar.pack.gz
  mkdir manifest
  pushd manifest
  unzip ../netbeans/modules/org-netbeans-modules-android-testrunner69.jar \
      META-INF/MANIFEST.MF org/netbeans/modules/android/testrunner69/Bundle.properties
  sed -i "s/= 201006101454/${3}/g" META-INF/MANIFEST.MF
  sed -i "s/Module: org.netbeans.modules.android.testrunner69/Module: org.netbeans.modules.android.${2}/g" \
      META-INF/MANIFEST.MF

  sed -i "s/ for NetBeans 6.9/${5}/g" org/netbeans/modules/android/testrunner69/Bundle.properties
  zip -f ../netbeans/modules/org-netbeans-modules-android-testrunner69.jar \
      META-INF/MANIFEST.MF org/netbeans/modules/android/testrunner69/Bundle.properties
  popd
  rm -rf manifest

  mv netbeans/modules/org-netbeans-modules-android-testrunner69.jar netbeans/modules/org-netbeans-modules-android-${2}.jar
  pack200 netbeans/modules/org-netbeans-modules-android-${2}.jar.pack.gz netbeans/modules/org-netbeans-modules-android-${2}.jar
  rm netbeans/modules/org-netbeans-modules-android-${2}.jar

  sed -i "s/org.netbeans.modules.android.testrunner69/org.netbeans.modules.android.${2}/g" Info/info.xml
  sed -i "s/= 201006101454/${4}/g" Info/info.xml
  sed -i "s/ for NetBeans 6.9/${5}/g" Info/info.xml

  jar -c0f $(dirname ${1})/org-netbeans-modules-android-${2}.nbm netbeans Info
  popd
  rm -rf ${NBM_TMPDIR}
}

create_nbm "$1" testrunner691 "= 201007282301" "= 201007282301" " for NetBeans 6.9.1"
create_nbm "$1" testrunner "> 1.19.1" '\&gt; 1.19.1' " for NetBeans 7.0+"

