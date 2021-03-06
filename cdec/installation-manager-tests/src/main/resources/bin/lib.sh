#!/bin/bash
#
# CODENVY CONFIDENTIAL
# ________________
#
# [2012] - [2015] Codenvy, S.A.
# All Rights Reserved.
# NOTICE: All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any. The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#

. ./config.sh

trap cleanUp EXIT

cleanUp() {
    vagrantDestroy
}

printAndLog() {
    echo $@
    log $@
}

logStartCommand() {
    log
    log "=== [ "`date`" ] COMMAND STARTED: "$@
}

logEndCommand() {
    log "=================================== COMMAND COMPLETED: "$@
    log
}

log() {
    echo "TEST: "$@ >> ${TEST_LOG}
}

validateExitCode() {
    EXIT_CODE=$1
    VALID_CODE=$2
    IS_INSTALL_CODENVY=$3

    if [[ ! -z ${VALID_CODE} ]]; then
        if [[ ${EXIT_CODE} == ${VALID_CODE} ]]; then
            return
        fi
    else
        if [[ ${EXIT_CODE} == "0" ]]; then
            return
        fi
    fi

    printAndLog "RESULT: FAILED"

    $(retrieveTestLogs)

    if [[ ! -z ${IS_INSTALL_CODENVY} ]]; then
        installLogContent=$(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@$(detectMasterNode) "cat install.log | sed 's/-\\\|\\///g'" | sed 's/\r//')
        printAndLog "============= Install.log file content ========="
        printAndLog "${installLogContent}"
        printAndLog "================================================="
    fi

    vagrantDestroy
    exit 1
}

retrieveTestLogs() {
    INSTALL_ON_NODE=$(detectMasterNode)
    logDirName="logs/`basename "$0" | sed 's/\\.sh//g'`"
    log "Name of directory with logs: ${logDirName}"
    [[ -d "${logDirName}" ]] && exit

    mkdir --parent ${logDirName}

    if [[ ${INSTALL_ON_NODE} == "master.${HOST_URL}" ]]; then
        for HOST in master api analytics data site runner1 builder1 datasource; do
            # store puppet logs
            $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo chown -R root:root /var/log/puppet")
            $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo chmod 777 /var/log/puppet")
            scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:/var/log/puppet/puppet-agent.log ${logDirName}/puppet-agent-${HOST}.log

            # store messages log
            $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo cp /var/log/messages /home/vagrant/messages")
            $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo chmod 777 /home/vagrant/messages")
            scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:messages ${logDirName}/messages-${HOST}

            if [[ ${HOST} == "api" || ${HOST} == "analytics" || ${HOST} == "site" || ${HOST} == "runner1" || ${HOST} == "builder1" ]]; then
                # store Codenvy log
                $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo cp /home/codenvy/codenvy-tomcat/logs/catalina.out /home/vagrant/codenvy-catalina.out")
                $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo chmod 777 /home/vagrant/codenvy-catalina.out")
                scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:codenvy-catalina.out ${logDirName}/codenvy-catalina-${HOST}.out
            fi

            if [[ ${HOST} == "master" ]]; then
                # store IM Server log
                $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo cp /home/codenvy-im/codenvy-im-tomcat/logs/catalina.out /home/vagrant/im-server-catalina.out")
                $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy "sudo chmod 777 /home/vagrant/im-server-catalina.out")
                scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:im-server-catalina-${HOST}.out ${logDirName}/
            fi
        done
    else
        # store puppet logs
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo chown -R root:root /var/log/puppet")
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo chmod 777 /var/log/puppet")
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:/var/log/puppet/puppet-agent.log ${logDirName}/

        # store Codenvy log
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo cp /home/codenvy/codenvy-tomcat/logs/catalina.out /home/vagrant/codenvy-catalina.out")
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo chmod 777 /home/vagrant/codenvy-catalina.out")
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:codenvy-catalina.out ${logDirName}/

        # store IM Server log
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo cp /home/codenvy-im/codenvy-im-tomcat /logs/catalina.out /home/vagrant/im-server-catalina.out")
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo chmod 777 /home/vagrant/im-server-catalina.out")
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:im-server-catalina.out ${logDirName}/

        # store messages log
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo cp /var/log/messages /home/vagrant/messages")
        $(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "sudo chmod 777 /home/vagrant/messages")
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:messages ${logDirName}/
    fi

    scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:/home/vagrant/install.log ${logDirName}/install.log
    scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:/home/vagrant/codenvy-im/codenvy-cli/data/tmp/im-non-interactive.log ${logDirName}/im-non-interactive.log
}

vagrantDestroy() {
    vagrant destroy -f >> ${TEST_LOG}
}

validateInstalledCodenvyVersion() {
    VERSION=$1

    [[ -z ${VERSION} ]] && VERSION=${LATEST_CODENVY3_VERSION}
    logStartCommand "validateInstalledCodenvyVersion "${VERSION}

    executeIMCommand "im-install" "--list"
    validateExpectedString ".*\"artifact\".*\:.*\"codenvy\".*\"version\".*\:.*\"${VERSION}\".*\"status\".*\:.*\"SUCCESS\".*"

    logEndCommand "validateInstalledCodenvyVersion"
}

validateInstalledImCliClientVersion() {
    VERSION=$1

    [[ -z ${VERSION} ]] && VERSION=${LATEST_IM_CLI_CLIENT_VERSION}

    logStartCommand "validateInstalledImCliClientVersion "${VERSION}

    executeIMCommand "im-install" "--list"
    validateExpectedString ".*\"artifact\".*\:.*\"installation-manager-cli\".*\"version\".*\:.*\"${VERSION}\".*\"status\".*\:.*\"SUCCESS\".*"

    logEndCommand "validateInstalledImCliClientVersion"
}

installCodenvy() {
    MULTI_OPTION=""
    VERSION_OPTION=""
    INSTALL_ON_NODE=$(detectMasterNode)

    VALID_CODE=0
    if [[ $1 =~ --valid-exit-code=.* ]]; then
        VALID_CODE=`echo "$1" | sed -e "s/--valid-exit-code=//g"`
        shift
    fi

    if [[ ${INSTALL_ON_NODE} == "master.${HOST_URL}" ]]; then
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key -P 2222 ~/.vagrant.d/insecure_private_key vagrant@127.0.0.1:./.ssh/id_rsa >> ${TEST_LOG}
        MULTI_OPTION="--multi"
    fi

    VERSION=$1
    [[ ! -z ${VERSION} ]] && VERSION_OPTION="--version="${VERSION}

    logStartCommand "installCodenvy "$@

    ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "export TERM='xterm' && bash <(curl -L -s ${UPDATE_SERVICE}/repository/public/download/install-codenvy) --silent --fair-source-license=accept ${MULTI_OPTION} ${VERSION_OPTION}" >> ${TEST_LOG}
    EXIT_CODE=$?
    validateExitCode ${EXIT_CODE} ${VALID_CODE} --installCodenvy

    logEndCommand "installCodenvy"
}

installImCliClient() {
    logStartCommand "installImCliClient "$@
    INSTALL_ON_NODE=$(detectMasterNode)
    VERSION=$1
    VERSION_OPTION=""
    [[ ! -z ${VERSION} ]] && VERSION_OPTION="--version="${VERSION}

    ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "export TERM='xterm' && bash <(curl -L -s ${UPDATE_SERVICE}/repository/public/download/install-im-cli)  --fair-source-license=accept ${VERSION_OPTION}" >> ${TEST_LOG}
    validateExitCode $?

    logEndCommand "installImCliClient"
}

vagrantUp() {
    VAGRANT_FILE=$1

    cp ${VAGRANT_FILE} Vagrantfile

    vagrant up >> ${TEST_LOG}
    validateExitCode $?
}

auth() {
    doAuth $1 $2 "sysldap" $3
}

authOnSite() {
    doAuth $1 $2 "org" $3
}

authWithoutRealmAndServerDns() {
    doAuth $1 $2
}

doAuth() {
    logStartCommand "auth "$@

    USERNAME=$1
    PASSWORD=$2
    REALM=$3
    SERVER_DNS=$4

    [[ -z ${SERVER_DNS} ]] && SERVER_DNS="http://${HOST_URL}"

    if [[ -n ${REALM} ]]; then
        local REALM_PARAMETER=", \"realm\":\"${REALM}\""
    fi

    OUTPUT=$(curl -s -X POST -H "Content-Type: application/json" -d "{\"username\":\"${USERNAME}\", \"password\":\"${PASSWORD}\"${REALM_PARAMETER}}" ${SERVER_DNS}/api/auth/login)

    EXIT_CODE=$?

    log ${OUTPUT}
    validateExitCode $?

    fetchJsonParameter "value"
    TOKEN=${OUTPUT}

    logEndCommand "auth"
}

executeIMCommand() {
    logStartCommand "executeIMCommand "$@

    VALID_CODE=0
    if [[ $1 =~ --valid-exit-code=.* ]]; then
        VALID_CODE=`echo "$1" | sed -e "s/--valid-exit-code=//g"`
        shift
    fi
    EXECUTE_ON_NODE=$(detectMasterNode)

    OUTPUT=$(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${EXECUTE_ON_NODE} "/home/vagrant/codenvy-im/codenvy-cli/bin/codenvy $@")
    EXIT_CODE=$?

    log ${OUTPUT}
    validateExitCode ${EXIT_CODE} ${VALID_CODE}

    logEndCommand "executeIMCommand"
}

executeSshCommand() {
    logStartCommand "executeSshCommand "$@

    VALID_CODE=0
    if [[ $1 =~ --valid-exit-code=.* ]]; then
        VALID_CODE=`echo "$1" | sed -e "s/--valid-exit-code=//g"`
        shift
    fi

    COMMAND=$1

    EXECUTE_ON_NODE=$2
    [[ -z ${EXECUTE_ON_NODE} ]] && EXECUTE_ON_NODE=$(detectMasterNode)

    OUTPUT=$(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${EXECUTE_ON_NODE} "${COMMAND}")
    EXIT_CODE=$?

    log ${OUTPUT}
    validateExitCode ${EXIT_CODE} ${VALID_CODE}

    logEndCommand "executeSshCommand"
}

detectMasterNode() {
    ping -c1 -q "master.${HOST_URL}" >> /dev/null
    if [[ $? == 0 ]]; then
        echo "master.${HOST_URL}"
    else
        ping -c1 -q ${HOST_URL} >> /dev/null
        if [[ $? == 0 ]]; then
            echo ${HOST_URL}
        else
            validateExitCode 1
        fi
    fi
}

fetchJsonParameter() {
    validateExpectedString ".*\"$1\".*"
    OUTPUT=`echo ${OUTPUT} | sed 's/.*"'$1'"\s*:\s*"\([^"]*\)*".*/\1/'`
}

# --method={POST|GET|...}
# --content-type=...
# --body=...
# --url=...
# --output-http-code
# --verbose
doHttpRequest() {
    for var in "$@"; do
        if [[ "$var" =~ --content-type=.* ]]; then
            local CONTENT_TYPE_OPTION=`echo "-H \"Content-Type: $var\"" | sed -e "s/--content-type=//g"`

        elif [[ "$var" =~ --body=.* ]]; then
            local BODY_OPTION=`echo "-d '$var'" | sed -e "s/--body=//g"`

        elif [[ "$var" =~ --url=.* ]]; then
            local URL=`echo "'$var'" | sed -e "s/--url=//g"`

        elif [[ "$var" =~ --method=.* ]]; then
            local METHOD_OPTION=`echo "-X $var" | sed -e "s/--method=//g"`
            
        elif [[ "$var" == "--output-http-code" ]]; then
            local OUTPUT_HTTP_CODE_OPTION="-o /dev/null -w \"%{http_code}\""

        elif [[ "$var" == "--verbose" ]]; then
            local VERBOSE_OPTION="-v"
        fi
    done

    local COMMAND="curl -s $VERBOSE_OPTION $OUTPUT_HTTP_CODE_OPTION $CONTENT_TYPE_OPTION $BODY_OPTION $METHOD_OPTION $URL"
    
    logStartCommand $COMMAND
    
    OUTPUT=$(eval $COMMAND)
    EXIT_CODE=$?
    log ${OUTPUT}

    validateExitCode ${EXIT_CODE}

    logEndCommand "curl"
}

doPost() {
    doHttpRequest --method=POST \
                  --content-type=$1 \
                  --body="$2" \
                  --url=$3
}

doGet() {
    doHttpRequest --method=GET \
                  --url=$1
}

createDefaultFactory() {
    logStartCommand "createDefaultFactory"

    TOKEN=$1

    OUTPUT=$(curl "http://${HOST_URL}/api/factory/?token="${TOKEN} -H 'Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7yqwdS1Jq8TWiUAE'  --data-binary $'------WebKitFormBoundary7yqwdS1Jq8TWiUAE\r\nContent-Disposition: form-data; name="factoryUrl"\r\n\r\n{\r\n  "v": "2.1",\r\n  "project": {\r\n    "name": "my-minimalistic-factory",\r\n    "description": "Minimalistic Template"\r\n  },\r\n  "source": {\r\n    "project": {\r\n      "location": "https://github.com/codenvy/sdk",\r\n      "type": "git"\r\n    }\r\n  }\r\n}\r\n------WebKitFormBoundary7yqwdS1Jq8TWiUAE--\r\n')
    EXIT_CODE=$?
    log ${OUTPUT}

    validateExitCode ${EXIT_CODE}

    logEndCommand "createDefaultFactory"
}

validateExpectedString() {
    logStartCommand "validateRegex "$@

    [[ ${OUTPUT} =~ $1 ]] || validateExitCode 1

    logEndCommand "validateRegex"
}

validateErrorString() {
    logStartCommand "validateErrorRegex "$@

    [[ ${OUTPUT} =~ $1 ]] && validateExitCode 1

    logEndCommand "validateRegex"
}

# $1 - NUMBER[SUFFIX]: Pause for NUMBER seconds.  SUFFIX may be 's' for seconds (the default), 'm' for minutes, 'h' for hours or 'd' for days.
# $2 - description to log
doSleep() {
    local TIME_TO_WAIT=$1

    local DESCRIPTION=$2
    [[ ! -z ${DESCRIPTION} ]] && log ${DESCRIPTION}

    executeSshCommand "sleep $TIME_TO_WAIT"
}

addCodenvyLicenseConfiguration() {
    local CONF_DIR='$HOME/che_conf'
    executeSshCommand "sed -i '1iexport CHE_LOCAL_CONF_DIR=$CONF_DIR' ~/.bashrc"
    executeSshCommand "if [ ! -d $CONF_DIR ]; then mkdir $CONF_DIR; fi"
    executeSshCommand "echo license-manager.public_key=$CODENVY_LICENSE_PUBLIC_KEY >> $CONF_DIR/installation-manager.properties"
}

storeCodenvyLicense() {
    storeStorageProperty "codenvy-license-key" "$CODENVY_LICENSE"
}

storeStorageProperty() {
    local property=$1
    local value=$2
    executeSshCommand "if [ ! -d $STORAGE_DIR ]; then sudo mkdir -p $STORAGE_DIR; fi"
    executeSshCommand "if [ ! -f $STORAGE_FILE ]; then sudo touch $STORAGE_FILE; fi"

    # add some text. the following grep command won't fail
    executeSshCommand "sudo bash -c 'echo ${property}= >> ${STORAGE_FILE}'"
    executeSshCommand "sudo sed -i '1 a #####' ${STORAGE_FILE}"

    executeSshCommand "sudo cat $STORAGE_FILE | grep -v ${property}= >> /tmp/tmp.tmp"
    executeSshCommand "sudo mv /tmp/tmp.tmp $STORAGE_FILE"
    executeSshCommand "sudo bash -c 'echo ${property}=${value} >> ${STORAGE_FILE}'"
}
