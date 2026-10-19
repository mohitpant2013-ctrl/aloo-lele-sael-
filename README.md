# My Minecraft projects (one repo, many projects)

Every project lives in its own folder under `projects/`. Projects never share files,
so you can add, change or delete one without touching the others.

```
projects/
  shield-statuses/        Forge 1.16.5 - green/red shield status
  <next-project>/         add new ones here
.github/workflows/build.yml   builds ALL projects automatically
```

## How to build
Push to GitHub -> open the **Actions** tab -> open the run -> download the jar from **Artifacts**
(one artifact per project, named like the folder).
To build just one project: Actions -> "Build projects" -> **Run workflow** -> type the folder name.

## How to add a new project
Create `projects/<name>/` with:
- `build.gradle` + `settings.gradle` (the build is only started for folders that contain `build.gradle`)
- `java-version.txt`   e.g. `8` for 1.16.5, `17` for 1.18-1.20.4, `21` for 1.20.5+  (default 17)
- `gradle-version.txt` e.g. `7.6.4`, `8.5`  (default 8.5)
- `src/main/java/...`, `src/main/resources/...`

## Rules for any AI working in this repo
1. Work only inside the one project folder you were asked to change.
2. Never edit another project or the workflow unless asked.
3. Keep every project buildable on its own with `java-version.txt` + `gradle-version.txt`.
4. Put the project's short description and Minecraft/loader version in its own `README.md`.

## Projects
| Folder | What | Minecraft / loader |
|---|---|---|
| shield-statuses | Shows green/red shield status for you and other players | 1.16.5 Forge 36.2.39 |
