# UTB-MT
Project for the Mobile technologies course on UTB.

Author: Ondřej Hruboš (o1\_hrubos *at* utb.cz)

# About
Simple comics/manga LIBRARY app. Create your own manga/comics library in your android device. Use your own folders or zip archives as source.

You can either index the manga yourself or use mangadex api to get basic metadata. Library metadata are stored in local MongoDB.
It gets additional metadata from https://api.mangadex.org/docs/swagger.html
(!DONT FORGET TO CREDIT MANGADEX IN INFO)

# Ideas

- progress saved in local storage
- manga metadata acquired from mangadex api

1. entry screen
    - profile selection
        - list
    - new
        - name

2. main screen
    - top bar
            - left side toggle show only favorites (star)
            - middle search bar
            - right side settings + info
    - first tab list of manga
        - tiles (default order by last read)
        - long press to select
            - selected can be batch removed/favorited/...
    - second tab add manga (batch/single, select folder, metadata, ...)

3. manga screen
    - top bar
        - left: go back
        - right: toggle favorite
    - title
    - description

4. reading screen (chapter)
    - basic reading settings at bottom - right->left/left->right/long strip
    - chapter title at top
    - current page number
    - skip to next/previous chapter button
5. settings screen
    - top bar
        - left: go back
        - right: info
    - middle (settings)
        - profile name
        - reading mode
        - app theme
    - bottom
        - switch profile button - will take the user to entry screen

6. info screen
   - author credits
   - mangadex credits

## Color palette
Subject to change

1. https://coolors.co/242038-636363-f3cc59-ffca24-ffbf00
1. https://coolors.co/242038-9067c6-8d86c9-f4e04d-f2ed6f
1. https://coolors.co/f1e3f3-c2bbf0-8fb8ed-62bfed-3590f3

# Specifications
### MongoDB collection specification:
- Create two collections 
    - Manga
    - User (profiles)
- Each collection is described by at least 3 fields 
    - Manga:
        1. ID\<UUID\>
        2. title\<String\>
        3. description\<String\>
        4. chapters\<Object\>
            1. title\<String\>
            2. description\<String\>
            3. pages\<Int\>
            4. pageLastRead\<Int\>
            5. read\<Boolean\> 
        5. favourite\<Boolean\>
        6. systemPath\<String\>
    - User (profiles):
        1. ID\<UUID\>
        2. name\<String\>
        3. settings\<Object\>
            1. darkMode\<Boolean\>
        4. associatedManga\<List\<Manga\>\>
- Each collection contains at least 5 documents.
- There must be a common field between both collections (typically, an ID).
- One of the collections must define a text (search) index.

### API Project specification:
- The API must expose at least 7 endpoints:
    - Get all documents from one collection
        - Manga
        - Profiles
    - Get one document by id (two endpoints, one for each collection)
        - Manga(ID)
        - Profiles(ID)
    - Get all documents by search (use the text index here)
        - Manga(Title)
    - Add a document (in one collection)
        - Manga
    - Edit a document (in one collection)
        - Manga
    - Delete a document (in one collection)
        - Manga

### Android Front-end specification:
- Written in Kotlin and will use MVVM architecture.
- User interface designed using XML or Jetpack Compose.
- Application icon and splash screen.
- Compilable into APK and presentable in a simulator or device.
- Multiple screens and functional navigation between screens.
- Communication over the network and retrieve data, e.g., from REST API.
