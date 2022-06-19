#!/bin/bash
# arguments
WORKSPACE=$1
CHANGE_LOG_MESSAGE=$2
VERSION=$3
BRANCH=$4
GIT_ID=$5
GIT_URL=$6

# variables
COMPONENT='audit-server'

# if no arguments is provided, log error on console and exit
[ $# -eq 0 ] && echo -e "No arguments supplied" && exit 1

NEW=$(echo -e "${CHANGE_LOG_MESSAGE}" | grep -n "New Features:" | cut -d : -f 1 | tail -n1)
BUG=$(echo -e "${CHANGE_LOG_MESSAGE}" | grep -n "Bug Fixes:" | cut -d : -f 1 | tail -n1)
COMP=$(echo -e "${CHANGE_LOG_MESSAGE}" | grep -n "Compatible Components:" | cut -d : -f 1 | tail -n1)

# if changelog message is invalid, log error on console and exit
if [ "${NEW}" == "" ] && [ "${BUG}" == "" ] && [ "${COMP}" == "" ] || [ "$(echo -e "${CHANGE_LOG_MESSAGE}" | grep "^ *- \+")" == "" ]; then
    echo -e "[ERROR] invalid change log message; release won't be published." && exit 1
fi

# rebase to the latest version of required branch
git checkout ${BRANCH}
git pull --rebase origin ${BRANCH}

# if same version has log on changelog, remove it
CHECK=$(grep "= Release ${COMPONENT}-${VERSION} =" ${WORKSPACE}/ChangeLog.txt)
if [ "${CHECK}" != "" ]; then
    ## get line number of the current version release note
    FIRSTLINE=$(grep -n "= Release ${COMPONENT}-${VERSION} =" ${WORKSPACE}/ChangeLog.txt | cut -d: -f1 | head -n1)
    ## get line number of the immediately next release note
    LASTLINE=$(grep -n "= Release ${COMPONENT}-" ${WORKSPACE}/ChangeLog.txt | grep -v "= Release ${COMPONENT}-${VERSION} =" | cut -d: -f1 | head -n1)
    ### if all release note are tagged with current release, write empty in file
    if [ "${LASTLINE}" == "" ]; then
        printf "" > ${WORKSPACE}/ChangeLog.txt
    ### else remove lines from start to the one before previous release note
    else
        let "LASTLINE=${LASTLINE}-1"
        sed -i ${WORKSPACE}/ChangeLog.txt -re "${FIRSTLINE}"','"${LASTLINE}"'d'
    fi
fi

# write message of latest version to changelog and push it on git
HEADER="==== Release ${COMPONENT}-${VERSION} === (Revision: g_${BRANCH}#${GIT_ID}) ====================================\n"
DATE="Date: "$(date +"%B-%d-%Y")"\n"
CHANGE_LOG_MSG=${HEADER}${DATE}${CHANGE_LOG_MESSAGE}"\n"
printf "${CHANGE_LOG_MSG}$(cat ${WORKSPACE}/ChangeLog.txt)" > ${WORKSPACE}/ChangeLog.txt
dos2unix ${WORKSPACE}/ChangeLog.txt
git add ${WORKSPACE}/ChangeLog.txt
git commit -m "update ChangeLog for ${COMPONENT}-${VERSION}"
git push origin ${BRANCH}

# create tag on git for current revision
git pull --tags
CHECK=$(git ls-remote | grep tags | awk '{print $2}' | cut -d '/' -f 3 | cut -d '^' -f 1  | uniq | grep "${COMPONENT}-v${VERSION}" | grep -v "${GIT_URL}")
## if tag already exists on git, remove it
[ "${CHECK}" != "" ] && git tag --delete "${COMPONENT}-v${VERSION}" && git push --delete origin "${COMPONENT}-v${VERSION}"
## add tag and push to git
git tag -a "${COMPONENT}-v${VERSION}" $(git rev-parse --short=8 HEAD) -m "${CHANGE_LOG_MESSAGE}"
git push origin "${COMPONENT}-v${VERSION}"
