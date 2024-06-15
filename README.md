# Genetic Algorithm

## Description
This Java-based project implements a genetic algorithm designed to solve optimization problems efficiently. It supports three modes of execution: sequential, parallel, and distributive, allowing users to choose the best approach based on their computational resources and requirements.

- **Sequential Mode**: Runs the algorithm using a single processor. This mode is best for simple tests or small datasets.
- **Parallel Mode**: Leverages multiple processors on the same machine to speed up the computation. This mode is suitable for moderately complex problems.
- **Distributive Mode**: Utilizes the MPI (Message Passing Interface) to distribute the computation across multiple nodes in a cluster.

## Prerequisites
Before you begin, ensure you have met the following requirements:
- **Java**: JDK 22 must be installed on your machine.
- **MPJ Express**: Version 0.44  is required for MPI capabilities, ensure the `lib` directory from MPJ Express is copied to the root directory of your project. This step is crucial for ensuring that all necessary libraries are accessible during runtime.

## Installation
1. **Clone the repository** or download the source code to your local machine.
2. **Verify the installation** of the prerequisites mentioned above.

## Configuration
Update the paths in the `mpjrun.sh` script to reflect the correct locations on your system:
- `MPJ_HOME` should point to your MPJ Express installation directory.
- `JAVA_HOME` should point to your Java installation directory.

Example:
```bash
export MPJ_HOME=/path/to/mpj
export JAVA_HOME=/path/to/java

Update the execution path accordingly 

./mpjrun.sh -np 4 -classpath /path/to/your/project/out/production/bla GeneticAlgorithmDistributed [arg1] [arg2] [arg3]

```

## Build and Execution
Go to 'File' > 'Project Structure' > 'Project' and set the Project SDK to your installed JDK.
Build the project by selecting 'Build' > 'Build Project' or pressing Ctrl+F9 (Cmd+F9 on macOS).
To run the Main with the default settings, locate the Main.java file, right-click on it, and select 'Run'.
