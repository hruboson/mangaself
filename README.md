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

1. main screen
    - top bar
            - left side toggle show only favorites (star)
            - middle search bar
            - right side settings + info
    - first tab list of manga
        - tiles (default order by last read)
        - long press to select
            - selected can be batch removed/favorited/...
    - second tab add manga (batch/single, select folder, metadata, ...)

2. manga screen
    - top bar
        - left: go back
        - right: toggle favorite
    - title
    - description

3. reading screen (chapter)
    - basic reading settings at bottom - right->left/left->right/long strip
    - chapter title at top
    - current page number
    - skip to next/previous chapter button

4. settings screen
    - top bar
        - left: go back
        - right: info

5. info screen
    - author credits
    - mangadex credits

## Color palette
Subject to change

1.
    - #242038 Dark purple
    - #9067C6 Amethyst
    - #8D86C9 Tropical indigo
    - #F4E04D Maize
    - #F2ED6F Maize

2. https://coolors.co/f1e3f3-c2bbf0-8fb8ed-62bfed-3590f3


