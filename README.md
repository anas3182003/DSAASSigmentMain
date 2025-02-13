# DSAASSigmentMain
follow this steps to clone the repo and commit the code

---

## **2. Clone the Repository (For New Team Members)**
Your teammates can clone the repository to their local machines using:
```sh
git clone <repo-url>
```
Example:
```sh
git clone https://github.com/user/repo.git
```

---

## **3. Pull the Latest Changes**
Before making changes, they should pull the latest updates:
```sh
git pull origin main  # or the relevant branch
```

---

## **4. Create a New Branch**
Each teammate should create their own branch to work on:
```sh
git checkout -b feature-branch
```

---

## **5. Add, Commit, and Push Changes**
Once they’ve made changes, they can commit and push:
```sh
git add .
git commit -m "Added new feature"
git push origin feature-branch
```

---

## **6. Create a Pull Request (PR)**
1. Go to **GitHub** → Open the repository.
2. Click on **"Pull Requests"** → **"New Pull Request"**.
3. Select **feature-branch** and compare it with **main**.
4. Click **"Create Pull Request"**, add a description, and request reviews.

---

## **7. Review & Merge Changes**
- The repository owner or a team member with **write access** reviews the PR.
- If everything looks good, they can **merge** it into `main` using:
  ```sh
  git checkout main
  git pull origin main
  git merge feature-branch
  git push origin main
  ```

---

## **8. Keep Repo Updated**
To avoid conflicts, teammates should always:
```sh
git pull origin main
```
before making new changes.

---


