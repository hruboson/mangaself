from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017/")
db = client["mangaself"]

profiles_collection = db["profiles"]
