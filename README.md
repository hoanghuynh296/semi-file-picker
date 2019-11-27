# semi-file-picker
Use for choosing file 
# feature
- support: image, video, audio
- add custom albums 
- material design
- change color depend on your themes
# Installation
Add it in your root build.gradle at the end of repositories:

 ```
allprojects { 
		repositories { 
			... 
			maven { url 'https://jitpack.io' } 
		} 
}
```
Add the following dependency to your `build.gradle` file: 
```
dependencies {
     implementation 'com.github.hoanghuynh296:semi-file-picker:v0.0.1-beta'
}
```
# Usage
``` 
FilePicker.Builder()
                .maxSelect(20)
                .typesOf(FilePicker.TYPE_IMAGE)
                .start(
                    this,
                    SELECT_IMAGES_REQUEST_ID
                )
```

 And get result: 
 
``` 
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                SELECT_IMAGES_REQUEST_ID -> {
                    val result : Array<String> = FilePicker.getResult(data) // return selected files path
                }
            }
}
```
