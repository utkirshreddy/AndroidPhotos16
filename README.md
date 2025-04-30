# AndroidPhotos16

### HOW TO RUN:
Upload project into Android Studio, and wait for gradle to fully build the project. This should be automatic. Afterwards, you can just click run.

### Notes:
- Saving to gallery is not automatic. It may take a second to load into the photos gallery


### AI Use:

Here are the prompts that I used to generate portions of our code

1. I am building a photos app in android studio with java. I have attached the UI that I would like to use for this application. Create the first UI for the home page in xml, paying close attention to every detail and note that I included within the drawing. (similar prompt for the other UI images)
2. In the home UI, there is a button that allows the user to create an album. I already have the design of the tile that should appear when the album is created. Implement this feature where the user should be able to make the album, set the title, and put the tile on the home page. Then this album should be able to save to the app's storage.
3. In the album view, there is a button that allows the user to add a photo to the current album. The photo can come from anywhere in the device. Create the code for this feature where the it adds the photo from the device into the app, and then subsequently will be stored within the album and in the app itself.
4. In the photo class, I need a way to obtain the file extension that is a part of the photo file based on the current information stored in the class.
5. In the photo class, I need a way to obtain the album name of the album that the photo is a part of.
6. For the text input in my Search UI, there needs to be autocompletion when typing in anything. Connect back to all of the tags in every photo using the tags in the photo class, and be able to display each unique tag.
7. Within the photo class, if a user wants to add a tag in the program, the photo class needs to update its tag list. Given a tag type and tag value, create an add tag function within this class.
8. When an album is added, I need the album tile to appear within the album. Create a class that handles displaying the albums in a grid layout. Just worry about connecting this class to the xml in the album item file.
9. When a photo is added, I need the photo tile to appear within the album. Create a class that handles displaying the photos in a grid layout. Just worry about connecting this class to the xml in the photo item file.
10. I need a way to store the albums and its photos within the app, so that when I close it, all of the albums and photos will still be there. Create a class that can handle that.
11. In the onCreate for the Search Activity class, can you fill out each of the UI components and set up the click functionality for some of the components. You can put place holders for new functions or clicks for right now.
12. I need a way to obtain the original file name of a file uploaded to an app based on the Uri
13. I need a way to obtain the file extension of a file from a Uri
14. I need a way to display the photo in this new photo view. Take the variables that I have already set and construct a method that can set the photo and update the tags.
15. When a photo is trying to display from a Uri, I need to make sure that photo can load, even if the photo is not on the device anymore. Create a method that can ensure the image is saved to app.
16. Implement this feature where a button is pressed that allows the user to save the current photo to the device gallery.
17. Create a function that displays a dialog with dropdown inputs for tag type (person/location) and tag value, allowing users to add metadata to their photos. It handles input validation, prevents duplicate tags, and updates both the UI and data model when a tag is successfully added.
18. I have a dropdown that has buttons to move, delete, and save to gallery in the album view. Can you make it so that when pressed, it initiates the corresponding function
19. Create a function that can move one photo to another album. There needs to be more than one album to be able to move, and the other album must not already have this photo in it
20. I need a function that can update the UI anytime a photo is moved, or deleted so that I don't have to reenter and program to see things updated

Inside of the files, we have indicated where there was AI use and if it is mixed with code we made by ourselves

Here are the UI diagrams we uploaded:
![AlbumView Dashboard UI (1)](https://github.com/user-attachments/assets/93aac12b-b9fd-4b6d-a496-7b3dfd5899b1)
![Android photos home UI (1)](https://github.com/user-attachments/assets/101a70e1-8880-4dbc-ac38-8aab28303c78)
![PhotoView UI (1)](https://github.com/user-attachments/assets/5a8c706b-05e0-4778-9752-29e6af97d2f5)
![SearchUI (1)](https://github.com/user-attachments/assets/b14c7148-6a2f-4aa9-9deb-04c8979bbce0)



