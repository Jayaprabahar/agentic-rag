import chromadb
import uuid

chroma_client = chromadb.CloudClient(
    api_key='${CHROMADB_API_KEY}',
    tenant='${CHROMADB_TENANT_NAME}',
    database=${CHROMADB_DEFAULT_DATABASE}
)

print('chroma_client', chroma_client)

collection = chroma_client.get_or_create_collection(name="amazon_return_policy",
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
print(collection.peek())
