# GitHub Repository Setup Instructions

## Creating the Private Repository on GitHub

Follow these steps to create a new private repository called "tournamega" on GitHub:

### Step 1: Create the Repository on GitHub
1. Go to https://github.com/new
2. Enter **tournamega** as the Repository name
3. Add description: "A Minecraft Spigot tournament plugin with team management and spectator mode"
4. Select **Private** visibility
5. Do NOT initialize with README (we already have content to push)
6. Click **Create repository**

### Step 2: Push to Your New Repository
Once the repository is created on GitHub, run these commands:

```powershell
cd C:\Users\Samuel\source\repos\NewRepo

# Set up git environment
$env:Path = "C:\Users\Samuel\dev-tools\PortableGit\bin;$env:Path"
Set-Alias git "C:\Users\Samuel\dev-tools\PortableGit\bin\git.exe" -Force

# Push to the new private repository
git push -u origin master
```

### Step 3: Verify the Push
Check that all commits and files are now in your new private repository:
- Visit https://github.com/duckisapie/tournamega
- Confirm the repository is **Private**
- Verify all source files are present

### Important Notes
- The repository is currently configured to use HTTPS
- If you have GitHub CLI installed, you can also use: `gh repo create tournamega --private --source=. --remote=origin --push`
- To make future commits easier, add the Git bin directory permanently to your Windows PATH

## Project Structure
The repository contains:
- `src/main/java/` - Tournament plugin source code
- `src/main/resources/` - Configuration and plugin manifest files
- `pom.xml` - Maven build configuration
- `build.gradle` - Gradle build alternative
- `target/` - Compiled plugin JAR (generated after build)

## Building the Plugin
From the repository root:
```bash
mvn clean package -DskipTests
```

The compiled plugin JAR will be created at: `target/tournament-plugin-1.0.0.jar`
