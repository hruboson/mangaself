from fastapi import FastAPI, HTTPException, Query, Body
from typing import List
from pymongo import ReturnDocument
from pymongo.collection import Collection
from urllib.parse import unquote

from models import Profile, Publication, Chapter
from database import profiles_collection

app = FastAPI(title="Mangaself API")

COL: Collection = profiles_collection

# -------------------- HELPERS --------------------

def find_profile(id: str):
    doc = profiles_collection.find_one({"id": id})
    if not doc:
        return None
    # Convert the Mongo document to a Pydantic Profile
    return Profile(
        id=doc["id"],
        name=doc.get("name", ""),
        readingMode=doc.get("readingMode", ""),
        associatedPublications=[
            Publication(
                systemPath=p.get("systemPath", ""),
                coverPath=p.get("coverPath", ""),
                title=p.get("title", ""),
                description=p.get("description", ""),
                chapters=[Chapter(**c) for c in p.get("chapters", [])],
                lastChapterRead=p.get("lastChapterRead", 0),
                favourite=p.get("favourite", False)
            )
            for p in doc.get("associatedPublications", [])
        ]
    )

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
    pipeline = [
        {"$project": {"associatedPublications": 1, "_id": 0}},
        {"$unwind": "$associatedPublications"},
        {"$replaceRoot": {"newRoot": "$associatedPublications"}}
    ]

    all_pubs = [
        Publication(
            systemPath=p.get("systemPath", ""),
            coverPath=p.get("coverPath", ""),
            title=p.get("title", ""),
            description=p.get("description", ""),
            chapters=[Chapter(**c) for c in p.get("chapters", [])],
            lastChapterRead=p.get("lastChapterRead", 0),
            favourite=p.get("favourite", False)
        )
        for p in COL.aggregate(pipeline)
    ]

    return all_pubs

@app.post("/profile/{id}/publications", response_model=Publication)
def add_publication(id: str, pub: Publication):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    
    # check if publication already exists -> return existing
    for existing_pub in profile.associatedPublications:
        if existing_pub.systemPath == pub.systemPath:
            return existing_pub

    res = COL.find_one_and_update(
        {"id": id},
        {"$push": {"associatedPublications": pub.dict()}},
        return_document=ReturnDocument.AFTER
    )

    added_pub = res["associatedPublications"][-1]
    return Publication(
        systemPath=added_pub.get("systemPath", ""),
        coverPath=added_pub.get("coverPath", ""),
        title=added_pub.get("title", ""),
        description=added_pub.get("description", ""),
        chapters=[Chapter(**c) for c in added_pub.get("chapters", [])],
        lastChapterRead=added_pub.get("lastChapterRead", 0),
        favourite=added_pub.get("favourite", False)
    )

@app.get("/profile/{id}/publications", response_model=List[Publication])
def get_all_publications_of_profile(id: str):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    return profile.associatedPublications

@app.get("/profile/{id}/publication", response_model=Publication)
def get_publication_by_system_path(id: str, systemPath: str = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")

    pipeline = [
        {"$match": {"id": id}},
        {"$unwind": "$associatedPublications"},
        {"$match": {"associatedPublications.systemPath": systemPath}},
        {"$replaceRoot": {"newRoot": "$associatedPublications"}}
    ]

    result = list(COL.aggregate(pipeline))
    if not result:
        raise HTTPException(status_code=404, detail="Publication not found")

    pub_doc = result[0]
    return Publication(
        systemPath=pub_doc.get("systemPath", ""),
        coverPath=pub_doc.get("coverPath", ""),
        title=pub_doc.get("title", ""),
        description=pub_doc.get("description", ""),
        chapters=[Chapter(**c) for c in pub_doc.get("chapters", [])],
        lastChapterRead=pub_doc.get("lastChapterRead", 0),
        favourite=pub_doc.get("favourite", False)
    )

@app.delete("/publication")
def remove_publication(systemPath: str = Query(...)):
    result = COL.update_many(
        {},  # match all profiles
        {"$pull": {"associatedPublications": {"systemPath": systemPath}}}
    )

    if result.modified_count == 0:
        raise HTTPException(status_code=404, detail="Publication not found")

    return {"status": "deleted", "modified_profiles": result.modified_count}

@app.delete("/profile/{id}/publication")
def remove_publication_from_profile(id: str, systemPath: str = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")

    COL.update_one(
        {"id": id},
        {"$pull": {"associatedPublications": {"systemPath": systemPath}}}
    )

    return {"status": "deleted"}

@app.delete("/publications")
def clear_publications():
    result = COL.update_many({}, {"$set": {"associatedPublications": []}})

    return {
        "status": "cleared",
        "modified_profiles": result.modified_count
    }

# -------------------- PUBLICATION PROPERTIES --------------------

@app.put("/profile/{id}/publication/cover")
def edit_publication_cover(id: str, systemPath: str = Query(...), coverPath: str = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")

    result = COL.update_one(
        {"id": id, "associatedPublications.systemPath": systemPath},
        {"$set": {"associatedPublications.$.coverPath": coverPath}}
    )

    if result.modified_count == 0:
        raise HTTPException(status_code=404, detail="Publication not found")

    return {"status": "updated", "coverPath": coverPath}

@app.put("/profile/{id}/publication/favourite")
def toggle_publication_favourite(id: str, systemPath: str = Query(...), toggleTo: bool = Query(...)):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")
    pass

# -------------------- CHAPTERS --------------------

# Add chapters to publication
@app.put("/profile/{id}/publication/chapters")
def add_chapters_to_publication(
    id: str,
    pubUri: str = Query(...),
    chapters: List[Chapter] = Body(...)
):
    profile = find_profile(id)
    if not profile:
        raise HTTPException(status_code=404, detail="Profile not found")

    # Replace chapters list of the matching publication
    result = COL.update_one(
        {"id": id, "associatedPublications.systemPath": pubUri},
        {"$set": {"associatedPublications.$.chapters": [ch.dict() for ch in chapters]}}
    )

    if result.matched_count == 0:
        raise HTTPException(status_code=404, detail="Publication not found")

    return {"status": "chapters updated", "count": len(chapters)}

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
