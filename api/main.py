from fastapi import FastAPI, HTTPException, Query
from typing import List
from pymongo import ReturnDocument
from pymongo.collection import Collection
from urllib.parse import unquote

from models import Profile, Publication, Chapter
from database import profiles_collection

app = FastAPI(title="Profiles / Publications API")

COL: Collection = profiles_collection

# -------------------- HELPERS --------------------

def find_profile(id: str):
    return COL.find_one({"id": id})

# -------------------- PROFILES --------------------

@app.get("/profiles", response_model=List[Profile])
def get_all_profiles():
    return list(COL.find())

@app.get("/profile/{id}", response_model=Profile)
def get_profile(id: str):
    doc = find_profile(id)
    if not doc:
        raise HTTPException(status_code=404, detail="Profile not found")
    return doc

@app.post("/profile", response_model=Profile)
def create_profile(profile: Profile):
    if find_profile(profile.id):
        raise HTTPException(status_code=400, detail="Profile already exists")
    COL.insert_one(profile.dict())
    return profile

@app.delete("/profile/{id}")
def delete_profile(id: str):
    res = COL.delete_one({"id": id})
    if res.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Profile not found")
    return {"status": "deleted"}

@app.delete("/profiles")
def clear_profiles():
    COL.delete_many({})
    return {"status": "cleared"}

@app.put("/profile/{id}", response_model=Profile)
def update_profile(id: str, updated: Profile):
    res = COL.find_one_and_replace({"id": id}, updated.dict(), return_document=ReturnDocument.AFTER)
    if not res:
        raise HTTPException(status_code=404, detail="Profile not found")
    return res

# -------------------- PUBLICATIONS --------------------

@app.get("/publications", response_model=List[Publication])
def get_all_publications():
    pass

@app.post("/profile/{id}/publications", response_model=Publication)
def add_publication(id: str, pub: Publication):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

@app.get("/profile/{id}/publications", response_model=List[Publication])
def get_all_publications_of_profile(id: str):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

@app.get("/profile/{id}/publication", response_model=Publication)
def get_publication_by_system_path(id: str, systemPath: str = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

@app.delete("/publication")
def remove_publication(systemPath: str = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

@app.delete("/profile/{id}/publication")
def remove_publication_from_profile(id: str, systemPath: str = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

@app.delete("/publications")
def clear_publications():
    pass

# -------------------- PUBLICATION PROPERTIES --------------------

@app.put("/profile/{id}/publication/cover")
def edit_publication_cover(id: str, systemPath: str = Query(...), coverPath: str = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

@app.put("/profile/{id}/publication/favourite")
def toggle_publication_favourite(id: str, systemPath: str = Query(...), toggleTo: bool = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

# -------------------- CHAPTERS --------------------

# Add chapters to publication
@app.put("/profile/{id}/publication/chapters")
def add_chapters_to_publication(id: str):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

# Edit cover
@app.put("/profile/{id}/publication/cover")
def edit_publication_cover(id: str):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

# Toggle favourite
@app.put("/profile/{id}/publication/favourite")
def toggle_publication_favourite(id: str):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

@app.put("/profile/{id}/publication/chapter")
def update_chapter(id: str):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass
