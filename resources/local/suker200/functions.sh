#!/bin/sh
export PATH=$PATH:~/.local/bin:/tmp/bin/

set -e pipefail

getOpsType() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f1)
}

getCommitAction() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f2)
}

getProjectName() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f3)
}

getCommitMessage() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD))
}

getCommitID() {
	echo $(git rev-parse HEAD)
}

prepareKops() {
	_ACTION=$(getCommitAction)
	_PROJECT=$(getProjectName)

	curl https://bootstrap.pypa.io/get-pip.py | python2.7 - --user
	~/.local/bin/pip2 install --user -r requirements.txt

	# curl -L https://github.com/kubernetes/kops/releases/download/1.9.0/kops-linux-amd64 -o /tmp/bin/kops && chmod +x /tmp/bin/kops

	
}

prepareTerraform() {
	curl -L https://releases.hashicorp.com/terraform/0.11.7/terraform_0.11.7_linux_amd64.zip -o /tmp/terraform.zip && \
		 cd /tmp/ && unzip -o terraform.zip && cp terraform bin/ && chmod +x bin/terraform
}

runKops() {
	_ACTION=$(getCommitAction)
	_PROJECT=$(getProjectName)
	echo ${_ACTION}

	# Generate kops_cluster + kops_template file
	python2 kops_generator.py --config projects/${_PROJECT}/config.yaml --template projects/${_PROJECT}/kops_template.yaml --project ${_PROJECT}
	# Load ENV file generate from kops_generator.py
	# CLUSTER_NAME=xxx
	# KOPS_VERSION=xxx
	. ./projects/${_PROJECT}/ENV

	kops replace --force -f projects/${_PROJECT}/kops/${KOPS_FILE:-kops_cluster.yaml} --state=${KOPS_STATE_STORE}

	kops create secret --name=${CLUSTER_NAME} sshpublickey admin -i projects/example/id_rsa.pub --state=${KOPS_STATE_STORE}

	kops update cluster --name=${CLUSTER_NAME} --yes --out=projects/${_PROJECT}/kops/ --target=terraform --state=${KOPS_STATE_STORE}

}

runTerraform() {
	_ACTION=${1:-$(getCommitAction)}
	_TERRAFORM_DIR=${2:-.}
	_SUFFIX_NAME=${3}
	_PROJECT=$(getProjectName)
	echo ${_ACTION}

	# Generate kops_cluster + kops_template file
	python2 kops_generator.py --config projects/${_PROJECT}/config.yaml --template projects/${_PROJECT}/kops_template.yaml --project ${_PROJECT}

	cd projects/${_PROJECT}/$_TERRAFORM_DIR
	terraform init

	if [ "${_ACTION}" = "plan" ]
	then
		terraform plan > ../../../upload/kops_upload
		runUpload ${_PROJECT} ${JOB_NAME}-${BUILD_NUMBER} "../../../upload/kops_upload" ${_SUFFIX_NAME} 
	elif [ "${_ACTION}" = "apply" ]
	then
		terraform apply  -input=false -auto-approve 
	else
		echo "${_ACTION} is not support action type"
		exit 1
	fi
}

runUpload() {
	_PROJECT=$1
	_FILE_NAME=$2
	_FILE_UPLOAD_NAME=$3
	_SUFFIX_NAME=$4
	_UPLOAD_TOKEN=${UPLOAD_TOKEN:-unkown}
	_UPLOAD_SERVER=${UPLOAD_SERVER:-127.0.0.1}
	curl -X PUT  -Ffile=@${_FILE_UPLOAD_NAME} ${_UPLOAD_SERVER}/files/${_PROJECT}-${_FILE_NAME}${_SUFFIX_NAME}?token=${_UPLOAD_TOKEN}
}