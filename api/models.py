from pydantic import BaseModel
from typing import List, Optional

class Chapter(BaseModel):
    title: str
    systemPath: str = ""
    description: str = ""
    position: int = 0
    pages: int = 0
    pageLastRead: int = 0
    read: bool = False

class Publication(BaseModel):
    systemPath: str
    coverPath: str = ""
    title: str = ""
    description: str = ""
    chapters: List[Chapter] = []
    lastChapterRead: int = 0
    favourite: bool = False

class Profile(BaseModel):
    id: str
    name: str = ""
    readingMode: str = ""
    associatedPublications: List[Publication] = []
