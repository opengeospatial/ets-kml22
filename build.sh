#!/bin/sh
# Builds the test and teamengine for UNIX/OS

build_teamengine=false

# TEAM Engine tag from https://github.com/opengeospatial/teamengine
te_tag=4.0.5
test_name=ets-kml22
tag=2.2-r11-SNAPSHOT

# base to download and install teamengine. 
# change the base if the directory is other than the current user default directory

base=~
mkdir -p $base/teamengine/TE_BASE/scripts
mkdir -p $base/teamengine/te_build
mkdir -p $base/teamengine/te_install 
folder_to_build=$base/teamengine/te_build
te_install=$base/teamengine/te_install

TE_BASE=$base/teamengine/TE_BASE

########
# build the test
mvn install -DskipTests=true
cd target 

unzip -q -o $test_name-$tag-ctl-scripts.zip -d $TE_BASE/scripts/
unzip -q -o $test_name-$tag-deps.zip -d $TE_BASE/resources/lib/

if $build_teamengine; then
   #download and build TEAM Engine
   cd $folder_to_build
   git_url=https://github.com/opengeospatial/teamengine.git 
   git clone -b $te_tag --depth 1  $git_url
   mvn install -DskipTests=true
   unzip -q -o $folder_to_build/teamengine/teamengine-console/target/teamengine-console-$te_tag-bin.zip -d $te_install
   unzip  -q -o $folder_to_build/teamengine/teamengine-console/target/teamengine-console-$te_tag-base.zip -d $TE_BASE
fi   

echo "  "
echo "-----------------   "
echo "  "
echo "Congratulations"
echo "  "
echo "TEAM Engine ($te_tag) and the ets-KML 2.2 have been installed."
echo "  "
echo " UNIX/OS users - You can run the test with the following commands: "
echo "   export TE_BASE=$TE_BASE"
echo "   $base/teamengine/te_install/bin/unix/test.sh -source=kml22/2.2/ctl/kml22-suite.ctl"
echo "  "
echo " Windows users - You can run the test with the following commands:"
echo "   set TE_BASE=$TE_BASE"
echo "   $base/teamengine/te_install/bin/windows/test.bat -source=kml22/2.2/ctl/kml22-suite.ctl"
echo "  "
echo " Any issue or questions please send an email to the CITE forum: 
http://cite.opengeospatial.org/forum"
echo "  "
