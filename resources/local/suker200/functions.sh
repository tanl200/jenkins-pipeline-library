#!/bin/sh
_TEMP_DIR="/tmp/${BUILD_NUMBER}"

export PATH=$PATH:~/.local/bin:${_TEMP_DIR}

set -e pipefail
trap finish EXIT

finish() {
	if [ -d "${_TEMP_DIR}" ]; then
		rm -rf ${_TEMP_DIR}
	fi
}

getOpsType() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f1)
}

getProjectName() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f2)
}

getCommitMessage() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD))
}

getCommitID() {
	echo $(git rev-parse HEAD)
}

Kops() {
	local _PROJECT=$(getProjectName)

	# Generate kops_cluster + kops_template file
	python2 kops_generator.py --config projects/${_PROJECT}/config.yaml --template projects/${_PROJECT}/kops_template.yaml --project ${_PROJECT}
	# Load ENV file generate from kops_generator.py
	# CLUSTER_NAME=xxx
	# KOPS_VERSION=xxx
	. ./projects/${_PROJECT}/ENV

	if [ ! -d "${_TEMP_DIR}" ]; then
		mkdir -p ${_TEMP_DIR}
	fi

	prepareKops

	runKops
}

prepareKops() {
	curl https://bootstrap.pypa.io/get-pip.py | python2.7 - --user
	~/.local/bin/pip2 install --user -r requirements.txt

	curl -L https://github.com/kubernetes/kops/releases/download/${KOPS_VERSION}/kops-linux-amd64 -o ${_TEMP_DIR}/kops && chmod +x ${_TEMP_DIR}/kops
}


runKops() {
	kops replace --force -f projects/${_PROJECT}/kops/${KOPS_FILE:-kops_cluster.yaml} --state=${KOPS_STATE_STORE}

	kops create secret --name=${CLUSTER_NAME} sshpublickey admin -i projects/example/id_rsa.pub --state=${KOPS_STATE_STORE}

	kops update cluster --name=${CLUSTER_NAME} --yes --out=projects/${_PROJECT}/kops/output --target=terraform --state=${KOPS_STATE_STORE}
}

Terraform() {
	local _PROJECT=$(getProjectName)

	local _RUN=$1
	local _TERRAFORM_DIR=${2:-.}
	local _SUFFIX_NAME=${3}

	# Generate kops_cluster + kops_template file
	python2 kops_generator.py --config projects/${_PROJECT}/config.yaml --template projects/${_PROJECT}/kops_template.yaml --project ${_PROJECT}
	# TERRAFORM_VERSION=xxx

	. ./projects/${_PROJECT}/ENV

	prepareTerraform

	if [ "${_RUN}" = "plan" ]; then
		runTerraform plan ${_PROJECT} ${_TERRAFORM_DIR} ${_SUFFIX_NAME}

	elif [ "${_RUN}" = "apply" ]; then
		runTerraform apply ${_PROJECT} ${_TERRAFORM_DIR}

	else
		echo "${_RUN} is not support action type"
		exit 1
	fi

}

prepareTerraform() {
	if [ ! -d "${_TEMP_DIR}" ]; then
		mkdir -p ${_TEMP_DIR}
	fi

	if [ ! -e "${_TEMP_DIR}/terraform" ]; then
		curl -L https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip -o ${_TEMP_DIR}/terraform.zip && \
			 cd ${_TEMP_DIR}/ && unzip -o terraform.zip && chmod +x terraform && cd $WORKSPACE
	fi
}

runTerraform() {
	local _ACTION=$1
	local _PROJECT=$2
	local _TERRAFORM_DIR=$3
	local _SUFFIX_NAME=$4

	cd projects/${_PROJECT}/$_TERRAFORM_DIR
	terraform init -backend-config=tf_backend

	if [ "${_ACTION}" = "plan" ]; then
		terraform plan > ../../../upload/kops_upload
		runUpload ${_PROJECT} ${JOB_NAME}-${BUILD_NUMBER} "../../../upload/kops_upload" ${_SUFFIX_NAME} 

	elif [ "${_ACTION}" = "apply" ]; then
		terraform apply  -input=false -auto-approve

	else
		exit 129
	fi
}

runUpload() {
	local _PROJECT=$1
	local _FILE_NAME=$2
	local _FILE_UPLOAD_NAME=$3
	local _SUFFIX_NAME=$4
	local _UPLOAD_TOKEN=${UPLOAD_TOKEN:-unkown}
	local _UPLOAD_SERVER=${UPLOAD_SERVER:-127.0.0.1}
	curl -X PUT  -Ffile=@${_FILE_UPLOAD_NAME} ${_UPLOAD_SERVER}/files/${_PROJECT}-${_FILE_NAME}${_SUFFIX_NAME}?token=${_UPLOAD_TOKEN}
}