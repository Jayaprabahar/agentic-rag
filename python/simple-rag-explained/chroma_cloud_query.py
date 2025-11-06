import chromadb
import uuid
import os

chroma_client = chromadb.CloudClient(
    api_key=os.getenv("CHROMADB_API_KEY"),
    tenant=os.getenv("CHROMADB_TENANT_NAME"),
    database=os.getenv("CHROMADB_DEFAULT_DATABASE")
)

print('chroma_client', chroma_client)

collection = chroma_client.get_or_create_collection(name="amazon_return_policy",
                                                    metadata={"description": "Amazon Return Policy Document Collection"})
print('collection', collection)

results = collection.query(
    query_texts=["show me the premium condition"],
    n_results=4
)

print('Data added to collection successfully.')
print(results)
