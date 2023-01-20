![ic_launcher_round](https://user-images.githubusercontent.com/35755386/209804411-ac4fadb7-2978-4075-81aa-12fb11967a83.png)

# HAVIT: Daily Habit Tracker

**HAVIT** brings an unique set of features that combines social media, video editing platform, and a daily habit tracker.

https://www.havit.space

---

## Useful Links

[Project Scope Document](https://lynjeong.notion.site/Scope-Statement-2fb256b59bff4f749568277d656a9580)

[Software Project Plan](https://lynjeong.notion.site/Software-Project-Plan-3a25cd6001224308a9ca8408c7de8aa2)

---

<table><tr>

<td valign="center"><img width="300" alt="Screenshot-3" src="https://user-images.githubusercontent.com/35755386/213597146-7b062310-df93-488d-9dc6-e84036f26eb4.png"></td>

<td valign="center"><img width="300" alt="Screenshot-4" src="https://user-images.githubusercontent.com/35755386/213597161-cd6da778-b4bc-4d40-ba5e-7a4c33982afd.png"></td>

<td valign="center"><img width="300" alt="Screenshot-5" src="https://user-images.githubusercontent.com/35755386/213597165-01129c78-c6d7-4886-b257-91c5bb1b5162.png"></td>


<td valign="center"><img width="300" alt="Screenshot-7" src="https://user-images.githubusercontent.com/35755386/213597182-8f16f327-549c-4aa2-a604-25315db092de.png"></td>

</tr></table>

---

## Known Bugs

**Other smaller bugs and unresolved feature implementations can be found under the *Issues* tab**

- The export feature does not download the compiled video (Server-side logic work in progress)

- Larger timelines that require longer load times may scroll through the carousel before displaying the images

- If there is a bad network connection, the app may alert the user that they have no timelines

---
## Tips & Tricks

- This is a project written in one and only **Java**, without using Jetpack Compose or any other fancy features. To run it, open it on Android Studio.

- To pull all the recent updates from the ```main``` branch, you have to execute ```git pull origin main``` in your repository folder!

- Minimum SDK level is set to version 23, while the Target SDK level is 33. Camera feature uses the latest CameraX API by Google.

- On public repo, conceal ```android/app/google-services.json```, ```template_uploader/havitcentral-b63dac00aa76.json```, and ```server/assets/havitcentral-b63dac00aa76.json``` as they contain the API keys for Firebase.

- To refresh & install new dependencies written on ```build.gradle```, simply go to *File* settings on your desktop's top navigation bar, then hit *Sync Project with Gradle* button!

- One of the main benefits of using LiveData is that it is lifecycle-aware, which means it will automatically stop emitting updates when the associated lifecycle owner (such as an activity or fragment) is in the "destroyed" state, and it will only start emitting updates again when the lifecycle owner is in the "started" or "resumed" state. This helps to avoid memory leaks and other problems caused by updating UI elements when they are not being displayed.

---

![UPDATED  UML class (1)](https://user-images.githubusercontent.com/35755386/213203379-0308f0e5-f242-4988-ba24-29fdfa69fe6d.png)


