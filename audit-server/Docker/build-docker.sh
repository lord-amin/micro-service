#!/usr/bin/env bash
git_user=team.j1
git_pass=Asdfg123
project=cloud
module=audit-servier
git=jlibraries/$project
rm -rf $project
BRANCH=$1
RELEASE=""
ddate=`date +"%Y-%m-%d_%H-%M-%S"`
if [ "$BRANCH" != "" ]; then
        echo ">>>>>> cloning $BRANCH"
        git clone -b $BRANCH https://$git_user:$git_pass@gitlab.peykasa.ir/$git.git
        RELEASE="D"
else
        echo ">>>>>> cloning master"
        git clone https://$git_user:$git_pass@gitlab.peykasa.ir/$git.git
        RELEASE="R"
fi
cd $project
commit=$(git log -1 --format=%h --abbrev=12)
#commit=$(git ls-remote -t https://team.j1:Asdfg123@gitlab.peykasa.ir/silo/grains-management.git | grep V1^{})
echo ---- $commit
cd $module
mvn clean package -DskipTests=true -Prelease
version=$(ls target/$module-*.tar.gz | grep -Eo '[0-9]+\.[0-9]+\.[0-9]+')
cd ../
cp $module/Docker/Dockerfile $module/target/
mkdir $module/target/package
cp $module/target/$module-$version.tar.gz $module/target/package/
cd $module/target
TAG=""
if [ "$BRANCH" != "" ]; then
	TAG="$RELEASE"_"$version"
	if [ "$2" == "debug"  ]; then
		TAG="$RELEASE"_"$version"_"$commit"_"$ddate"
	fi
else
	TAG="$RELEASE"_"$module"_"$version"_"$commit"_"$ddate"
fi
echo "---sss> tag is $TAG"
docker build -t gitlab.peykasa.ir:4567/$git:$TAG .
docker push gitlab.peykasa.ir:4567/$git:$TAG
