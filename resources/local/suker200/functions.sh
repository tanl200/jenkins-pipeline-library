#!/bin/bash

function getCommitMessageAction() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD) | cut -d ":" -f1)
}

function getCommitMessage() {
	echo $(git log --format=%s%b -n 1 $(git rev-parse HEAD))
}

function getCommitID() {
	echo $(git rev-parse HEAD)
}
