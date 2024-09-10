package com.mayowa.permissiondemo.models

data class Photo(
    val mediaUrl: String,
)

val randomSizedPhotos: List<Photo> = listOf(
    Photo(mediaUrl = "https://images.pexels.com/photos/4456985/pexels-photo-4456985.jpeg?auto=compress&cs=tinysrgb&w=800&lazy=load"),
    Photo(mediaUrl = "https://images.pexels.com/photos/7534339/pexels-photo-7534339.jpeg?auto=compress&cs=tinysrgb&w=800&lazy=load"),
    Photo(mediaUrl = "https://images.pexels.com/photos/28055801/pexels-photo-28055801/free-photo-of-a-woman-in-a-black-dress-is-standing-in-the-desert.jpeg?auto=compress&cs=tinysrgb&w=800&lazy=load"),
    Photo(mediaUrl = "https://images.pexels.com/photos/15030820/pexels-photo-15030820/free-photo-of-close-up-of-pumpkins-in-different-shapes-and-colors.jpeg?auto=compress&cs=tinysrgb&w=800&lazy=load"),
    Photo(mediaUrl = "https://picsum.photos/id/103/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/104/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/105/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/106/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/107/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/108/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/109/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/110/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/111/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/112/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/113/200/300"),
    Photo(mediaUrl = "https://picsum.photos/id/114/200/300"),
)