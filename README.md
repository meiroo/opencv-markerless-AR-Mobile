##The Markerless Augmented Reality by OpenCV on Mobile phones

###based on takmin project:

url： https://github.com/takmin/OpenCV-Marker-less-AR

### Windows version. 

Can be found here: https://github.com/meiroo/opencv-markerless-AR

I have compiled the source code under windows 8. also updated the opencv version to 2.4.9.
The visual-studio-2012 project can be found under the "src" folder.

this is my testing video (win8/vs2012): 

http://meiroo.github.io/img/marker.m4v

=======================================================

### Android version (not finished.)

I have transplanted the recognize part on the android platform with the opencv 2.4.9, I have to compile libnonfree.a from the opencv source. 

This based on the opencv-android-mixed-demo, which has Java side for camera handling and also NDK side for native opencv operations. The recognize-part-src was compiled with NDK, then running under my Android Phone, but it's too slow and very hard to debug the native code, so I continue the work with iPhone 5C (much better than my android phone).

=======================================================
### iOS version : 

video:

http://meiroo.github.io/img/CV-ios.m4v

Building and running with opencv-2.4.10-ios-version under iphone 5c, I have made some changes to make it running quicker. Now the recognize part cost 50-80 ms, while the tracking part cost only 15-25 ms, much quicker.
But the speed is still not so good with mobile phones. Also , when tracking , there are accumulated-errors which can cause the pose very easy to fail ( the pose calculated by opencv opticalflow can have some error each frame, and will be accumulated to the last frame).


=========================================================

BTW, I'm working on a Markless tracking project on my current job, video can be found:

##http://meiroo.github.io/projects#ar
