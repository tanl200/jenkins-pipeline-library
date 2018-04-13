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
	. ./${_PROJECT}/ENV

	curl https://bootstrap.pypa.io/get-pip.py | python2.7 - --user
	~/.local/bin/pip2 install -r requirements.txt

	curl -L https://github.com/kubernetes/kops/releases/download/1.9.0/kops-linux-amd64 -o /tmp/kops && chmod +x /tmp/bin/kops

}

runKops() {
	_ACTION=$(getOpsType)
	_PROJECT=$(getProjectName)
	# Load ENV file generate from kops_generator.py
	# CLUSTER_NAME=xxx
	# KOPS_VERSION=xxx
	. ./${_PROJECT}/ENV

	if [ ${_ACTION} == "init" ]
	then
		python2 kops_generator.py --config providers/${_PROJECT}/config.yaml --template providers/${_PROJECT}/kops_template.yaml
		kops create --dry-run -f providers/${_PROJECT}/${KOPS_FILE:-kops_cluster.yaml}
		# kops update cluster --name=${CLUSTER_NAME} --yes --out=. --target=terraform 
	elif [ ${_ACTION} == "replace" ]
	then
		kops replace -f providers/${_PROJECT}/${KOPS_FILE:-kops_cluster.yaml}
		kops update cluster --name=${CLUSTER_NAME} --yes --out=. --target=terraform
	else
		exit 1
	fi
}