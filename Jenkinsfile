// This Jenkinsfile is used by Jenkins to run the 'FireworksLayout' step of Reactome's release.
// It requires that the 'DiagramConverter' step has been run successfully before it can be run.

import org.reactome.release.jenkins.utilities.Utilities

// Shared library maintained at 'release-jenkins-utils' repository.
def utils = new Utilities()

pipeline{
	agent any

	// Set output folder that will contain files created by step.
	environment {
		OUTPUT_FOLDER = "fireworks"
		ECR_URL = 'public.ecr.aws/reactome/fireworks-layout'
		CONT_NAME = 'fireworks_layout_container'
		CONT_ROOT = '/opt/fireworks-layout'
	}

	stages{
		// This stage checks that upstream project 'DiagramConverter' was run successfully.
		stage('Check DiagramConverter build succeeded'){
			steps{
				script{
					utils.checkUpstreamBuildsSucceeded("File-Generation/job/DiagramConverter/")
				}
			}
		}

		stage('Setup: Pull and clean docker environment'){
			steps{
				sh "docker pull ${ECR_URL}:latest"
				sh """
					if docker ps -a --format '{{.Names}}' | grep -Eq '${CONT_NAME}'; then
						docker rm -f ${CONT_NAME}
					fi
				"""
			}
		}

		// Execute the jar file, producing the diagram JSON files.
		stage('Main: Run Fireworks-Layout'){
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					sh "mkdir -p ${env.OUTPUT_FOLDER}"
					sh "sudo rm -rf ${env.OUTPUT_FOLDER}/*"
					withCredentials([usernamePassword(credentialsId: 'neo4jUsernamePassword', passwordVariable: 'pass', usernameVariable: 'user')]){
						sh """\
							docker run \\
							-v \$(pwd)/${env.OUTPUT_FOLDER}:${CONT_ROOT}/${env.OUTPUT_FOLDER} \\
							--net=host \\
							--name ${CONT_NAME}_exec \\
							${ECR_URL}:latest \\
							/bin/bash -c 'java -Dlogback.configurationFile=src/main/resources/logback.xml -jar target/fireworks-exec.jar --user $user --password $pass --folder ./config --output ./${env.OUTPUT_FOLDER}'
						"""
						sh "sudo chown jenkins:jenkins ${env.OUTPUT_FOLDER} -R"
						// Create archive that will be stored on S3.
						sh "tar -zcf fireworks-v${releaseVersion}.tgz ${env.OUTPUT_FOLDER}/"
					}
				}
			}
		}

		// Execute the verifier checking the fireworks output
		stage('Post: Verify EventPDF ran correctly') {
			steps {
				script {
					def releaseVersion = utils.getReleaseVersion()
					def dropTolerancePercentage = 5

					sh """
						docker run \\
						-v \${pwd()}/${env.OUTPUT_FOLDER}:${CONT_ROOT}/${env.OUTPUT_FOLDER}/ \\
						-v \$HOME/.aws:/root/.aws:ro \\
						-e AWS_REGION=us-east-1 \\
						--rm \\
						--net=host \\
						--name ${CONT_NAME}_verifier \\
						${ECR_URL}:latest \\
						/bin/bash -c "java -jar target/fireworks-verifier.jar --releaseNumber ${releaseVersion} --output ${outputDirectory}/ --sizeDropTolerance ${dropTolerancePercentage}"
					"""
				}
			}
		}

		// Lists all JSON fireworks file sizes between current and previous releases for comparison.
		stage('Post: Compare file sizes with previous release') {
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def previousReleaseVersion = utils.getPreviousReleaseVersion()
					def previousFireworksArchive = "fireworks-v${previousReleaseVersion}.tgz"
					sh "mkdir -p ${previousReleaseVersion}"
					// Downloads previous release fireworks archive from S3.
					sh "aws s3 --no-progress cp s3://reactome/private/releases/${previousReleaseVersion}/fireworks/data/${previousFireworksArchive} ${previousReleaseVersion}/"
					dir("${previousReleaseVersion}"){
						sh "tar -xf ${previousFireworksArchive}"
					}
					// Output files between releases.
					def currentDiagramsFileCount = findFiles(glob: "${env.OUTPUT_FOLDER}/*").size()
					def previousDiagramsFileCount = findFiles(glob: "${previousReleaseVersion}/${env.OUTPUT_FOLDER}/*").size()
					echo("Fireworks file sizes for v${releaseVersion}: ${currentDiagramsFileCount}")
					sh "ls -lrt ${env.OUTPUT_FOLDER}"
					echo("Fireworks file sizes for v${previousReleaseVersion}: ${previousDiagramsFileCount}")
					sh "ls -lrt ${previousReleaseVersion}/${env.OUTPUT_FOLDER}/"

					sh "rm -r ${previousReleaseVersion}*"
				}
			}
		}

		// Move fireworks folder to download folder.
		stage('Post: Move fireworks folder to download folder') {
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def downloadPath = "${env.ABS_DOWNLOAD_PATH}/${releaseVersion}"
					sh "sudo rm -rf ${downloadPath}/fireworks"
					sh "mv ${env.OUTPUT_FOLDER} ${downloadPath}/ "
				}
			}
		}

		// Archive everything on S3, and move the 'diagram' folder to the download/vXX folder.
		stage('Post: Archive Outputs'){
			steps{
				script{
					def releaseVersion = utils.getReleaseVersion()
					def dataFiles = ["fireworks-v${releaseVersion}.tgz"]
					def logFiles = []
					// Note at time of writing fireworks-layout does not output log files (but makes very, very verbose stdout)
					def foldersToDelete = []
					utils.cleanUpAndArchiveBuildFiles("fireworks", dataFiles, logFiles, foldersToDelete)
				}
			}
		}
	}
}
