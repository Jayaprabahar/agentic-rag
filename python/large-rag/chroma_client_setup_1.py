import chromadb
from chromadb.config import Settings

print(" Initializing ChromaDB persistant Client...")
client = chromadb.PersistentClient(
    path="../chroma_db",
    settings=Settings(anonymized_telemetry=False)
)

collection = client.get_or_create_collection(
    name="return_policy_data",
    metadata={"hnsw:space": "cosine"}
)

print(f" Data Created: {collection.name}")
print(f" Memories: {collection.count()}")
print(" AI Data Ready!")