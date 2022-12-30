![ic_launcher_round](https://user-images.githubusercontent.com/35755386/209804411-ac4fadb7-2978-4075-81aa-12fb11967a83.png)

# HAVIT: The Iterative Timeline

**HAVIT** brings an unique set of features that combines social media, video editing platform, and a daily habit tracker.

---

## Useful Links

[Project Scope Document](https://lynjeong.notion.site/Scope-Statement-2fb256b59bff4f749568277d656a9580)

[Software Project Plan](https://lynjeong.notion.site/Software-Project-Plan-3a25cd6001224308a9ca8408c7de8aa2)

---

## Tips & Tricks

- This is a project written in one and only **Java**, without using Jetpack Compose or any other fancy features. To run it, open it on Android Studio.

- To pull all the recent updates from the ```main``` branch, you have to execute ```git pull origin main``` in your repository folder!

- Minimum SDK level is set to version 23, while the Target SDK level is 33. Camera feature uses the latest CameraX API by Google.

- On public repo, conceal ```android/app/google-services.json``` and ```android/app/src/main/res/raw/service_account.json``` as they contain API keys for Firebase and Firestore, respectively.

- To refresh & install new dependencies written on ```build.gradle```, simply go to *File* settings on your desktop's top navigation bar, then hit *Sync Project with Gradle* button!

- One of the main benefits of using LiveData is that it is lifecycle-aware, which means it will automatically stop emitting updates when the associated lifecycle owner (such as an activity or fragment) is in the "destroyed" state, and it will only start emitting updates again when the lifecycle owner is in the "started" or "resumed" state. This helps to avoid memory leaks and other problems caused by updating UI elements when they are not being displayed.

---

<table><tr>

<td valign="center"><img width="500" alt="Screenshot-1" src="https://user-images.githubusercontent.com/35755386/209806062-aa66f78e-09e1-4977-bdb6-287b388a3ae6.jpg"></td>

<td valign="center"><img width="500" alt="Screenshot-2" src="https://user-images.githubusercontent.com/35755386/209806112-05018597-1650-4651-b947-81b790a424c1.jpg"></td>

<td valign="center"><img width="500" alt="Screenshot-3" src="https://user-images.githubusercontent.com/35755386/209806135-f04ba766-6055-4f02-9344-5b8ea22ec4a9.jpg"></td>

</tr></table>

![UML class](https://user-images.githubusercontent.com/35755386/208787080-85a6a410-15bb-47c8-8af6-9ebfef9e678a.jpeg)
