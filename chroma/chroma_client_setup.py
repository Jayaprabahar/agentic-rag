import chromadb
import uuid
from chromadb.config import Settings

##"sentence-transformers/all-MiniLM-L6-v2

client = chromadb.HttpClient(
    host="localhost",
    port=8000,
    ssl=False,
    headers=None,
    tenant="default_tenant",
    database="default_database",
    settings=Settings(allow_reset=True, anonymized_telemetry=False)
)

print(client.heartbeat())
print('chroma_client', client)

collection = client.get_or_create_collection(name="amazon_return_policy",
                                                    metadata={"description": "Amazon Return Policy Document Collection"})
print('collection', collection)

with open("amazon_return_policy.txt", "r", encoding="utf-8") as f:
    amazon_return_policy:list[str] = f.read().splitlines()

collection.add(
    ids=[str(uuid.uuid4()) for _ in amazon_return_policy],
    documents=amazon_return_policy,
    metadatas=[{"line": line} for line in range(len(amazon_return_policy))]
)
print('Data added to collection successfully.')

print(client.heartbeat())

