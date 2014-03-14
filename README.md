SimpleRss
===

News Aggregator for Android 4.0+.

You can find the latest APK signed by me in the apk folder.

Screenshots
===

![ScreenShot](screenshots/thumbnails/one_preview.png)
![ScreenShot](screenshots/thumbnails/two_preview.png)
![ScreenShot](screenshots/thumbnails/three_preview.png)

![ScreenShot](screenshots/thumbnails/four_preview.png)
![ScreenShot](screenshots/thumbnails/five_preview.png)
![ScreenShot](screenshots/thumbnails/six_preview.png)

Permissions
===
```
   <uses-permission android:name="android.permission.INTERNET"/>
```

INTERNET - This is to download rss feeds and images from the feeds.

```
   <uses-permission android:name="android.permission.WAKE_LOCK"/>
```

WAKE_LOCK - This is to update feeds when the app is closed mid update and when scheduled to update (in settings).

Building
===

Make sure to have the Android SDK Platform API 19 installed.

To build the code using Gradle:

```
   ./gradlew assembleDebug
```

Install the apk to a device connected with adb:

```
   adb install -r build/apk/SimpleRss-debug-unaligned.apk
```